package org.example.conversion.generator;

import org.example.conversion.analyzer.FlowAnalyzer;
import org.example.conversion.analyzer.FlowAnalyzer.ExecutableNode;
import org.example.conversion.model.FlowDefinition;
import org.example.core.util.BizException;
import org.example.core.util.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * LiteFlow EL 表达式生成器
 *
 * <p>职责：
 *   将流程定义对象（FlowDefinition）转换为 LiteFlow 可执行的 EL 表达式字符串。
 *
 * <p>架构说明：
 *   本生成器基于 FlowAnalyzer 的 AST（抽象语法树）分析能力，支持 THEN 串行执行和 WHEN 并行执行语义。
 *   通过调用 {@link FlowAnalyzer#analyzeToAST(FlowDefinition)} 获取 AST 根节点，
 *   再调用 {@link ExecutableNode#toEL()} 生成最终的表达式片段。
 *
 * <p>支持的 EL 类型：
 *   - THEN：串行执行组
 *   - WHEN：并行执行组
 *   - ComponentNode：叶子节点（具体业务算子）
 *
 * <p>生成规则：
 *   - 单组件直接返回 componentId
 *   - 多组件串行使用 THEN() 包裹
 *   - 可并行组件使用 WHEN() 包裹
 *   - 最终表达式包装在 &lt;chain name="xxx"&gt;...&lt;/chain&gt; 中
 *
 * <p>使用示例：
 *   <pre>
 *   FlowDefinition flow = ...;
 *   String el = elGenerator.generate(flow, "mainChain");
 *   // 输出：&lt;chain name="mainChain"&gt;
 *   //         THEN(nodeA, WHEN(nodeB, nodeC), nodeD);
 *   //       &lt;/chain&gt;
 *   </pre>
 */
@Component
public class ELGenerator {

    /**
     * 默认链名称
     */
    private static final String DEFAULT_CHAIN_NAME = "mainChain";

    /**
     * 空流程时的默认表达式
     */
    private static final String EMPTY_CHAIN_EXPRESSION = "";

    private final FlowAnalyzer analyzer;

    @Autowired
    public ELGenerator(FlowAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    /**
     * 生成 LiteFlow EL 表达式（使用默认链名称 mainChain）
     *
     * @param flow 已校验的流程定义对象
     * @return 完整的 EL 表达式字符串，包装在 chain 标签内
     */
    public String generate(FlowDefinition flow) {
        return generate(flow, DEFAULT_CHAIN_NAME);
    }

    /**
     * 生成 LiteFlow EL 表达式（指定链名称）
     *
     * <p>处理流程：
     *   1. 判空防御：如果 flow 为空，返回空链结构
     *   2. 调用 FlowAnalyzer 生成 AST 根节点
     *   3. 调用 AST 节点的 toEL() 方法生成表达式
     *   4. 将表达式包装在 &lt;chain name="xxx"&gt;...&lt;/chain&gt; 中
     *
     * @param flow      已校验的流程定义对象，包含节点和边信息
     * @param chainName 链名称，用于标识该流程在 LiteFlow 中的执行链
     * @return 完整的 EL 表达式字符串，格式为 &lt;chain name="xxx"&gt;...&lt;/chain&gt;
     *         如果流程为空，返回空字符串
     * @throws BizException 如果 chainName 为空或空白
     */
    public String generate(FlowDefinition flow, String chainName) {
        if (!StringUtils.hasText(chainName)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "链名称");
        }

        // 判空防御：流程定义为空时返回空表达式
        if (flow == null) {
            return EMPTY_CHAIN_EXPRESSION;
        }

        // 调用 AST 分析器生成语法树根节点
        ExecutableNode astRoot = analyzer.analyzeToAST(flow);

        // 生成 EL 表达式片段
        String elExpression = astRoot.toEL();

        // 如果 AST 为空（无节点），返回空表达式
        if (!StringUtils.hasText(elExpression)) {
            return EMPTY_CHAIN_EXPRESSION;
        }

        // 格式化封装：外层包装 chain 标签
        return wrapWithChainTag(chainName, elExpression);
    }

    /**
     * 生成 XML 格式的完整规则文件内容
     *
     * <p>适用于需要生成完整规则文件内容的场景，包含 XML 声明和 flow 根标签。
     *
     * @param flow      已校验的流程定义对象
     * @param chainName 链名称
     * @return 完整的 XML 规则文件内容
     */
    public String generateXml(FlowDefinition flow, String chainName) {
        if (!StringUtils.hasText(chainName)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "链名称");
        }

        if (flow == null) {
            return generateEmptyXml(chainName);
        }

        ExecutableNode astRoot = analyzer.analyzeToAST(flow);
        String elExpression = astRoot.toEL();

        if (!StringUtils.hasText(elExpression)) {
            return generateEmptyXml(chainName);
        }

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<flow>\n" +
                "    <chain name=\"" + escapeXml(chainName) + "\">\n" +
                "        " + elExpression + ";\n" +
                "    </chain>\n" +
                "</flow>";
    }

    /**
     * 将 EL 表达式包装在 chain 标签内
     *
     * @param chainName     链名称
     * @param elExpression  EL 表达式
     * @return 包装后的字符串
     */
    private String wrapWithChainTag(String chainName, String elExpression) {
        return "<chain name=\"" + escapeXml(chainName) + "\">\n    " + elExpression + ";\n</chain>";
    }

    /**
     * 生成空的 XML 结构
     *
     * @param chainName 链名称
     * @return 包含空 chain 的 XML 内容
     */
    private String generateEmptyXml(String chainName) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<flow>\n" +
               "    <chain name=\"" + escapeXml(chainName) + "\"></chain>\n" +
               "</flow>";
    }

    /**
     * 对字符串进行 XML 转义，防止特殊字符破坏 XML 结构
     *
     * @param input 原始字符串
     * @return 转义后的字符串
     */
    private String escapeXml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
    }
}
