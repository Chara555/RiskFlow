package org.example.conversion.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 流程转换结果
 *
 * 包含：
 * - 转换是否成功
 * - 生成的 EL 表达式
 * - 生成的 XML 内容
 * - 验证过程中的错误和警告
 */
@Data
public class ConversionResult {

    private boolean success;
    private String elExpression;
    private String xmlContent;
    private String chainName;

    //永远在声明时初始化集合，防御空指针异常
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    /**
     * 创建成功结果
     */
    public static ConversionResult success(String chainName, String elExpression, String xmlContent) {
        ConversionResult result = new ConversionResult();
        result.setSuccess(true);
        result.setChainName(chainName);
        result.setElExpression(elExpression);
        result.setXmlContent(xmlContent);
        // 注意：这里不需要再管 errors 和 warnings，因为它们已经是空 ArrayList 了
        return result;
    }

    /**
     * 创建失败结果
     */
    public static ConversionResult failure(List<String> errors, List<String> warnings) {
        ConversionResult result = new ConversionResult();
        result.setSuccess(false);
        // 防御性塞入数据，防止传入的 List 是 null
        if (errors != null) {
            result.getErrors().addAll(errors);
        }
        if (warnings != null) {
            result.getWarnings().addAll(warnings);
        }
        return result;
    }
}