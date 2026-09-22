package com.javaai.router.router;

import org.springframework.ai.chat.prompt.Prompt;

/**
 * 任务分类器：判断一个请求属于哪类任务，从而决定候选模型池。
 *
 * <p>这里为了示例简洁，用关键词规则实现。生产环境更稳的做法：
 * <ul>
 *   <li>用便宜的小模型做一次「零成本分类」（比强模型便宜 1~2 个数量级）</li>
 *   <li>或用向量相似度匹配历史任务类型</li>
 * </ul>
 */
public class TaskClassifier {

    public enum TaskType {
        /** 推理 / 代码 / 数学：需要强模型 */
        REASONING,
        /** 普通对话、客服问答：通用模型即可 */
        CHAT,
        /** 简单结构化任务：小模型足够 */
        SIMPLE
    }

    private static final String[] REASONING_HINTS = {
            "推导", "证明", "算法", "复杂度", "代码", "重构", "debug", "排查", "为什么"
    };

    private static final String[] SIMPLE_HINTS = {
            "查询", "订单状态", "翻译", "格式化", "提取", "分类"
    };

    public static TaskType classify(Prompt prompt) {
        String text = prompt.getInstructions().toString();

        for (String h : REASONING_HINTS) {
            if (text.contains(h)) return TaskType.REASONING;
        }
        for (String h : SIMPLE_HINTS) {
            if (text.contains(h)) return TaskType.SIMPLE;
        }
        return TaskType.CHAT;
    }
}
