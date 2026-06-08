package com.aiagentstudio.service;

import com.aiagentstudio.tool.CalculatorTool;
import com.aiagentstudio.tool.WeatherTool;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 工具注册与执行服务
 */
@Service
public class ToolService {

    private static final Logger log = LoggerFactory.getLogger(ToolService.class);

    private final Map<String, ToolHandler> registry = new HashMap<>();
    private final CalculatorTool calculatorTool;
    private final WeatherTool weatherTool;

    public ToolService(CalculatorTool calculatorTool, WeatherTool weatherTool) {
        this.calculatorTool = calculatorTool;
        this.weatherTool = weatherTool;

        // 注册内置工具
        register(calculatorTool.getName(), calculatorTool.getDescription(), calculatorTool.getParameters(), calculatorTool::execute);
        register(weatherTool.getName(), weatherTool.getDescription(), weatherTool.getParameters(), weatherTool::execute);

        log.info("Registered {} tools: {}", registry.size(), registry.keySet());
    }

    public void register(String name, String description, Map<String, Object> parameters,
            java.util.function.Function<Map<String, Object>, String> executor) {
        registry.put(name, new ToolHandler(name, description, parameters, executor));
    }

    /**
     * 获取 OpenAI 格式的工具定义列表
     */
    public List<Map<String, Object>> getToolDefinitions() {
        List<Map<String, Object>> tools = new ArrayList<>();
        for (ToolHandler handler : registry.values()) {
            Map<String, Object> tool = new HashMap<>();
            Map<String, Object> function = new HashMap<>();
            function.put("name", handler.name());
            function.put("description", handler.description());
            function.put("parameters", handler.parameters());
            tool.put("type", "function");
            tool.put("function", function);
            tools.add(tool);
        }
        return tools;
    }

    /**
     * 执行单个工具调用
     */
    public String execute(String toolName, Map<String, Object> params) {
        ToolHandler handler = registry.get(toolName);
        if (handler == null) {
            return "Error: unknown tool '" + toolName + "'";
        }
        try {
            log.info("Executing tool: {} with params: {}", toolName, params);
            return handler.executor().apply(params);
        } catch (Exception e) {
            log.error("Tool execution failed for {}: {}", toolName, e.getMessage());
            return "Error executing tool: " + e.getMessage();
        }
    }

    /** 工具处理器 */
    public record ToolHandler(
            String name,
            String description,
            Map<String, Object> parameters,
            java.util.function.Function<Map<String, Object>, String> executor) {}
}
