package org.example.dto;

import lombok.Data;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 流程响应体
 */
@Data
public class FlowResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer version;

    /**
     * 流程状态 (建议前端或业务层通过枚举映射，如: DRAFT, PUBLISHED)
     */
    private String status;

    private String flowData;
    private String elExpression;
    private String createdBy;

    // 保持 Instant，输出标准 ISO-8601 UTC 时间，让前端根据浏览器时区自行 render (国际化最佳实践)
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * 转换错误信息（强制初始化，防御前端读取 null.length 导致白屏报错）
     */
    private List<String> errors = new ArrayList<>();

    /**
     * 转换警告信息（强制初始化）
     */
    private List<String> warnings = new ArrayList<>();
}