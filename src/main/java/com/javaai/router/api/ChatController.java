package com.javaai.router.api;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 演示入口：POST /api/chat
 *
 * <p>注意：这里注入的 {@code ChatModel} 实际上是 {@link com.javaai.router.router.ModelRouter}，
 * 但业务代码完全感知不到「背后有多个模型」——这正是路由层设计的意义。
 */
@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatModel chatModel;

    public ChatController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody ChatRequest req) {
        String answer = chatModel.call(new Prompt(req.message()))
                .getResult()
                .getOutput()
                .getText();
        return Map.of("answer", answer);
    }

    public record ChatRequest(String message) {
    }
}
