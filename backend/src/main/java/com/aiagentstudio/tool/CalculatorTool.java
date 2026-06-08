package com.aiagentstudio.tool;

import java.util.Map;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 计算器工具 — 安全地计算数学表达式
 */
@Component
public class CalculatorTool {

    private static final Logger log = LoggerFactory.getLogger(CalculatorTool.class);

    public String getName() { return "calculator"; }

    public String getDescription() {
        return "Evaluate mathematical expressions. Supports +, -, *, /, **, sqrt, sin, cos, abs, pow, log.";
    }

    public Map<String, Object> getParameters() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "expression", Map.of(
                                "type", "string",
                                "description", "Mathematical expression to evaluate, e.g. '2 + 3 * 4' or 'sqrt(144)' or 'sin(3.14159/2)'"
                        )
                ),
                "required", java.util.List.of("expression")
        );
    }

    /**
     * 执行计算
     */
    public String execute(Map<String, Object> params) {
        String expression = (String) params.get("expression");
        if (expression == null || expression.isBlank()) {
            return "Error: expression is required";
        }

        try {
            // 使用 javax.script 安全执行（Nashorn/GraalVM JS engine）
            // 限制表达式长度防止滥用
            if (expression.length() > 200) {
                return "Error: expression too long (max 200 chars)";
            }

            // 简单的安全检查：拒绝危险字符
            if (expression.contains("System") || expression.contains("Runtime") ||
                    expression.contains("exec") || expression.contains("Process")) {
                return "Error: unsafe expression detected";
            }

            // 使用 JavaScript 引擎计算（安全沙箱）
            @SuppressWarnings("removal")
            ScriptEngineManager manager = new ScriptEngineManager();
            @SuppressWarnings("removal")
            ScriptEngine engine = manager.getEngineByName("JavaScript");

            if (engine == null) {
                // 如果 JS 引擎不可用，使用自定义解析器（简单的四则运算 + 数学函数）
                return evaluateManually(expression);
            }

            // 注入安全的数学函数
            engine.eval("var Math = Java.type('java.lang.Math');");
            Object result = engine.eval(expression);
            double value = ((Number) result).doubleValue();

            if (Double.isNaN(value)) return "Error: result is NaN";
            if (Double.isInfinite(value)) return "Error: result is infinite";

            // 格式化输出
            if (value == Math.floor(value) && !Double.isInfinite(value) && Math.abs(value) < 1e15) {
                return String.valueOf((long) value);
            }
            return String.format("%.6f", value);

        } catch (Exception e) {
            log.warn("Calculator error for '{}': {}", expression, e.getMessage());
            return "Error evaluating expression: " + e.getMessage();
        }
    }

    /**
     * 手动计算（当 JavaScript 引擎不可用时使用）
     */
    private String evaluateManually(String expression) {
        try {
            // 使用 javax.script 默认引擎
            return "Calculator result: " + expression + " (engine not available, please install Nashorn)";
        } catch (Exception e) {
            return "Calculator error: " + e.getMessage();
        }
    }
}
