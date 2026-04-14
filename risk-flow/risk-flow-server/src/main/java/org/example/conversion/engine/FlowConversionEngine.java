package org.example.conversion.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.conversion.generator.ELGenerator;
import org.example.conversion.model.ConversionResult;
import org.example.conversion.model.FlowDefinition;
import org.example.conversion.model.ValidationResult;
import org.example.conversion.validator.FlowValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 流程转换引擎 (门面模式 Facade)
 *
 * 职责：
 * 作为转换模块的唯一对外入口，整合 Parser、Validator、Analyzer、Generator。
 * 提供高内聚的转换接口，并负责顶层异常的兜底与日志记录。
 */
@Component
public class FlowConversionEngine {

    private static final Logger log = LoggerFactory.getLogger(FlowConversionEngine.class);

    private final FlowValidator validator;
    private final ELGenerator generator;
    private final ObjectMapper objectMapper;

    public FlowConversionEngine(FlowValidator validator,
                                ELGenerator generator,
                                ObjectMapper objectMapper) {
        this.validator = validator;
        this.generator = generator;
        this.objectMapper = objectMapper;
    }

    /**
     * 将 JSON 字符串转换为 LiteFlow EL 表达式
     * 优化：遵循 DRY 原则，直接复用对象处理链路
     */
    public ConversionResult convert(String json) {
        try {
            FlowDefinition flow = objectMapper.readValue(json, FlowDefinition.class);
            return convert(flow);
        } catch (JsonProcessingException e) {
            log.warn("流程引擎解析 JSON 失败: {}", e.getMessage());
            return ConversionResult.failure(
                    List.of("JSON 格式错误，无法解析为流程定义: " + e.getMessage()),
                    List.of()
            );
        }
    }

    /**
     * 将 FlowDefinition 对象转换为 LiteFlow EL 表达式
     * 优化：增加底层 AST 异常的兜底拦截，防止系统级崩溃
     */
    public ConversionResult convert(FlowDefinition flow) {
        // 1. 验证流程定义
        ValidationResult validationResult = validator.validate(flow);
        if (!validationResult.isValid()) {
            log.warn("流程定义校验未通过，流程编码: {}", flow.getCode());
            return ConversionResult.failure(
                    validationResult.getErrors(),
                    validationResult.getWarnings()
            );
        }

        // 2. 生成 EL 表达式与 XML
        try {
            String chainName = flow.getCode() != null ? flow.getCode() : "defaultChain";
            String elExpression = generator.generate(flow, chainName);
            String xmlContent = generator.generateXml(flow, chainName);

            // 构造成功结果
            ConversionResult successResult = ConversionResult.success(chainName, elExpression, xmlContent);

            // 优化：不要丢弃合法的警告信息（需要你的 ConversionResult.success 支持传入 warning，或者有 setWarnings 方法）
            successResult.getWarnings().addAll(validationResult.getWarnings());

            return successResult;

        } catch (IllegalStateException | IllegalArgumentException e) {
            // 【关键兜底】：拦截底层 AST 拓扑排序发现的“死循环”或“参数非法”等异常
            log.error("底层引擎生成表达式异常, chain: {}", flow.getCode(), e);
            return ConversionResult.failure(
                    List.of("表达式生成失败 (可能存在逻辑死循环): " + e.getMessage()),
                    validationResult.getWarnings()
            );
        } catch (Exception e) {
            // 未知异常兜底
            log.error("引擎发生未知系统异常", e);
            return ConversionResult.failure(
                    List.of("系统内部异常，请联系管理员"),
                    List.of()
            );
        }
    }

    /**
     * 仅验证流程定义（不生成 EL）
     */
    public ValidationResult validateOnly(String json) {
        try {
            FlowDefinition flow = objectMapper.readValue(json, FlowDefinition.class);
            return validateOnly(flow);
        } catch (JsonProcessingException e) {
            ValidationResult result = new ValidationResult();
            result.addError("JSON 格式错误: " + e.getMessage());
            return result;
        }
    }

    /**
     * 仅验证流程定义（不生成 EL）
     */
    public ValidationResult validateOnly(FlowDefinition flow) {
        return validator.validate(flow);
    }
}