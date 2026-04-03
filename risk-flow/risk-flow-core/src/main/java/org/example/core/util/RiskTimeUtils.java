package org.example.core.util;

import java.time.Clock;
import java.time.Instant;

/**
 * 风险引擎全局时间基准
 */
public class RiskTimeUtils {
    // 默认使用系统 UTC 时钟
    private static Clock clock = Clock.systemUTC();

    /**
     * 获取当前绝对毫秒时间戳 (所有算子必须调这个方法，严禁自己调 System.currentTimeMillis)
     */
    public static long nowMs() {
        return clock.millis();
    }

    /**
     * 获取当前绝对瞬间
     */
    public static Instant now() {
        return clock.instant();
    }

    /**
     * 架构师后门：为了单元测试，强行拨动系统时间（Time Travel）
     */
    public static void useMockTime(Instant mockTime) {
        clock = Clock.fixed(mockTime, clock.getZone());
    }

    /**
     * 测试结束后，恢复真实时间
     */
    public static void reset() {
        clock = Clock.systemUTC();
    }
}

