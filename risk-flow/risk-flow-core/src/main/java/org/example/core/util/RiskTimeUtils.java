package org.example.core.util;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 风险引擎全局时间基准与清洗网关
 */
public class RiskTimeUtils {
    // 默认使用系统 UTC 时钟
    private static Clock clock = Clock.systemUTC();

    // 预定义东八区，方便日志打印等特殊需求
    public static final ZoneId ZONE_BEIJING = ZoneId.of("Asia/Shanghai");

    /**
     * 获取当前绝对毫秒时间戳
     */
    public static long nowMs() {
        return clock.millis();
    }

    /**
     * 获取当前绝对瞬间 (推荐引擎内部流转统一用 Instant)
     */
    public static Instant now() {
        return clock.instant();
    }

    /**
     * 清洗外部带时区的标准时间戳 (如 AbuseIPDB, 阿里云)
     * 将 "2018-12-20T20:55:14+00:00" 或 "2026-04-11T12:00:00+08:00"
     * 统一洗成无视时区的绝对时间点 (Instant)
     */
    public static Instant parseExternalTime(String isoDateTimeStr) {
        if (isoDateTimeStr == null || isoDateTimeStr.isBlank()) {
            return null;
        }
        return OffsetDateTime.parse(isoDateTimeStr).toInstant();
    }

    /**
     * 清洗外部不带时区的“裸时间” (如某些老旧系统只发 "2026-04-11 12:00:00")
     * 必须强制指定该系统所在的源时区，避免时间错乱
     */
    public static Instant parseRawTimeWithZone(String rawTimeStr, DateTimeFormatter formatter, ZoneOffset sourceOffset) {
        if (rawTimeStr == null || rawTimeStr.isBlank()) {
            return null;
        }
        // 解析成没有时区的本地时间，再打上它原本的时区戳，最后转为绝对时间 Instant
        return java.time.LocalDateTime.parse(rawTimeStr, formatter).toInstant(sourceOffset);
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