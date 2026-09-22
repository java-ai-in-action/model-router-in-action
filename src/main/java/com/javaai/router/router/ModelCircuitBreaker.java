package com.javaai.router.router;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 按「模型维度」注册的熔断器。
 *
 * <p><b>为什么必须按模型维度？</b> 因为「某个模型挂了」和「我们自己的网络挂了」是两回事。
 * 按模型隔离，才能做到「只切那一个，其他照常」。
 */
@Component
public class ModelCircuitBreaker {

    private final CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(
            CircuitBreakerConfig.custom()
                    .slidingWindowSize(10)                              // 统计窗口
                    .minimumNumberOfCalls(5)                            // 至少 5 次才计算失败率
                    .failureRateThreshold(50)                           // 失败率过半 → 打开
                    .waitDurationInOpenState(Duration.ofSeconds(30))    // 打开后 30s 再试探
                    .build());

    public CircuitBreaker of(String modelId) {
        return registry.circuitBreaker(modelId);
    }

    /** 调用前判断：熔断打开则不再尝试（避免白白等一次超时） */
    public boolean isAvailable(String modelId) {
        return of(modelId).tryAcquirePermission();
    }

    public void recordFailure(String modelId) {
        of(modelId).onError(0, TimeUnit.MILLISECONDS,
                new RuntimeException("model call failed"));
    }

    public void recordSuccess(String modelId) {
        of(modelId).onSuccess(0, TimeUnit.MILLISECONDS);
    }
}
