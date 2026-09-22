package com.javaai.router.router;

import com.javaai.router.router.TaskClassifier.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 多模型路由核心。
 *
 * <p>关键设计：它<b>自己也实现 {@link ChatModel} 接口</b>，因此对上层完全透明——
 * 业务代码注入的 {@code ChatModel} 就是它，一行都不用改。
 */
@Component
public class ModelRouter implements ChatModel {

    private static final Logger log = LoggerFactory.getLogger(ModelRouter.class);

    /** 所有候选模型（Bean 名 → ChatModel） */
    private final Map<String, ChatModel> models;
    private final ModelCircuitBreaker breaker;

    public ModelRouter(Map<String, ChatModel> models, ModelCircuitBreaker breaker) {
        // 注意：排除自己，避免自引用
        this.models = models;
        this.models.remove("chatModel");
        this.breaker = breaker;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        for (String id : route(prompt)) {                 // 按优先级排序的候选列表
            if (!breaker.isAvailable(id)) {               // 熔断打开的，直接跳过
                log.debug("模型[{}]熔断中，跳过", id);
                continue;
            }
            try {
                ChatResponse resp = models.get(id).call(prompt);
                breaker.recordSuccess(id);
                return resp;
            } catch (Exception e) {
                breaker.recordFailure(id);
                log.warn("模型[{}]调用失败，降级下一个：{}", id, e.getMessage());
            }
        }
        throw new IllegalStateException("所有候选模型均不可用");
    }

    /** 路由策略：按任务 → 按成本 → 按可用性（返回候选优先级列表） */
    private List<String> route(Prompt prompt) {
        TaskType type = TaskClassifier.classify(prompt);
        return switch (type) {
            case REASONING -> List.of("deepseek", "qwen", "qwen-turbo");
            case SIMPLE    -> List.of("qwen-turbo", "qwen", "deepseek");
            case CHAT      -> List.of("qwen", "deepseek", "qwen-turbo");
        };
    }
}
