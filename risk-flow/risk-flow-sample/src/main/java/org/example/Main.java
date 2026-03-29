package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RiskFlow Sample 应用入口
 * 
 * 本示例展示两种调用方式：
 * 1. SDK 嵌入模式 - 直接调用 risk-flow-core
 * 2. REST API 模式 - 通过 HTTP 调用 risk-flow-server
 */
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}