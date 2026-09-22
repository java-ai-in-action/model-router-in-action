package com.javaai.router.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.dashscope.DashScopeApi;
import org.springframework.ai.dashscope.DashScopeChatModel;
import org.springframework.ai.dashscope.DashScopeChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 多模型 Bean 配置。
 *
 * <p>Spring AI 最舒服的一点：所有模型都实现同一个 {@link ChatModel} 接口，
 * 所以「多模型」本质上就是「多个 Bean」。
 */
@Configuration
public class ChatModelConfig {

    /** 通义千问（主力：中文对话 + 工具调用） */
    @Bean("qwen")
    public ChatModel qwenModel(DashScopeApi dashScopeApi) {
        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .build();
    }

    /** DeepSeek（推理：复杂问题、代码），走 OpenAI 兼容协议 */
    @Bean("deepseek")
    public ChatModel deepseekModel() {
        return OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl("https://api.deepseek.com")
                        .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                        .build())
                .build();
    }

    /** 便宜的小模型（兜底：简单任务、高并发） */
    @Bean("qwen-turbo")
    public ChatModel turboModel(DashScopeApi dashScopeApi) {
        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(DashScopeChatOptions.builder()
                        .withModel("qwen-turbo")
                        .build())
                .build();
    }

    /**
     * 默认注入的 ChatModel = 路由器。
     *
     * <p>这样业务代码注入的 {@code ChatModel} 就是 {@link com.javaai.router.router.ModelRouter}，
     * 完全感知不到「背后有多个模型」。
     */
    @Primary
    @Bean
    public ChatModel chatModel(com.javaai.router.router.ModelRouter router) {
        return router;
    }
}
