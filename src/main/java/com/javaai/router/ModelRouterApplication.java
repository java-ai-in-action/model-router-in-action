package com.javaai.router;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 多模型路由与故障转移示例入口。
 *
 * <p>配套文章：篇7《别把 Agent 绑死在一个模型上：Spring AI Alibaba 多模型路由与故障转移实战》
 */
@SpringBootApplication
public class ModelRouterApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModelRouterApplication.class, args);
    }
}
