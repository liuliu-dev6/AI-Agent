package com.aiagentstudio.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 意图识别服务 — Prompt 分类 + LRU 缓存
 */
@Service
public class IntentService {

    private static final Logger log = LoggerFactory.getLogger(IntentService.class);

    private final ModelGatewayService gateway;
    private final ObjectMapper objectMapper;

    /** 简易 LRU 缓存（LinkedHashMap + access-order） */
    private final LinkedHashMap<String, IntentResult> cache;

    public IntentService(ModelGatewayService gateway, ObjectMapper objectMapper) {
        this.gateway = gateway;
        this.objectMapper = objectMapper;
        this.cache = new LinkedHashMap<>(200, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, IntentResult> eldest) {
                return size() > 500;
            }
        };
    }

    /**
     * 分类用户意图
     */
    public IntentResult classify(String userInput, String model, String apiKey) {
        if (userInput == null || userInput.isBlank()) {
            return new IntentResult("chat", 0.99);
        }

        // 1. 检查缓存
        String cacheKey = normalize(userInput);
        IntentResult cached = cache.get(cacheKey);
        if (cached != null) {
            log.debug("Intent cache hit: {} → {}", cacheKey, cached.intent());
            return cached;
        }

        // 2. 关键词匹配
        IntentResult fastResult = keywordMatch(userInput);
        if (fastResult != null && fastResult.confidence() > 0.7) {
            cache.put(cacheKey, fastResult);
            return fastResult;
        }

        // 3. 有 API Key 时使用 LLM 增强分类，否则直接降级
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                IntentResult llmResult = classifyViaLLM(userInput, model, apiKey);
                cache.put(cacheKey, llmResult);
                return llmResult;
            } catch (Exception e) {
                log.warn("Intent LLM failed, fallback: {}", e.getMessage());
            }
        }

        // 降级：关键词匹配或默认 chat
        return fastResult != null ? fastResult : new IntentResult("chat", 0.5);
    }

    private IntentResult keywordMatch(String input) {
        String lower = input.toLowerCase().trim();

        if (lower.contains("天气") || lower.contains("气温") || lower.contains("温度") || lower.contains("weather")) {
            return new IntentResult("tool_call", 0.92);
        }
        if (lower.contains("计算") || lower.contains("等于") || lower.matches(".*\\d+\\s*[+\\-*/]\\s*\\d+.*")) {
            return new IntentResult("tool_call", 0.95);
        }
        if (lower.contains("知识") || lower.contains("文档") || lower.contains("查询") || lower.contains("是什么") || lower.contains("如何")) {
            return new IntentResult("knowledge_search", 0.7);
        }
        if (lower.contains("代码") || lower.contains("bug") || lower.contains("debug") || lower.contains("程序")) {
            return new IntentResult("code_analysis", 0.80);
        }
        if (lower.contains("workflow") || lower.contains("流程") || lower.contains("编排")) {
            return new IntentResult("workflow", 0.85);
        }
        if (lower.contains("sql") || lower.contains("数据库") || lower.contains("查询表")) {
            return new IntentResult("sql_query", 0.85);
        }

        return null; // 无法通过关键词判断
    }

    private IntentResult classifyViaLLM(String input, String model, String apiKey) {
        String prompt = "You are an intent classifier. Classify the user message into ONE intent.\n" +
                "Respond ONLY with JSON: {\"intent\":\"...\",\"confidence\":0.0-1.0}\n\n" +
                "Intents:\n" +
                "- chat: casual conversation, greetings, small talk\n" +
                "- knowledge_search: factual questions, document lookup\n" +
                "- tool_call: requests to use external tools (calculator, weather)\n" +
                "- code_analysis: code review, debugging, programming\n" +
                "- workflow: multi-step task that needs orchestration\n" +
                "- sql_query: database or data questions\n\n" +
                "User: " + input + "\n" +
                "Intent:";

        List<Map<String, Object>> messages = List.of(
                Map.of("role", "user", "content", (Object) prompt)
        );

        String response = gateway.chatSync(model, apiKey, messages, null, 256, 0.1);

        try {
            String content = extractContent(response);
            // Try to extract JSON from response
            String json = content;
            int braceStart = content.indexOf('{');
            int braceEnd = content.lastIndexOf('}');
            if (braceStart >= 0 && braceEnd > braceStart) {
                json = content.substring(braceStart, braceEnd + 1);
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(json, Map.class);
            String intent = (String) result.getOrDefault("intent", "chat");
            double confidence = result.get("confidence") instanceof Number n ? n.doubleValue() : 0.7;
            return new IntentResult(intent, confidence);
        } catch (Exception e) {
            log.warn("Failed to parse intent JSON: {}", e.getMessage());
            return new IntentResult("chat", 0.6);
        }
    }

    /** 从 API 响应中提取 content */
    private String extractContent(String apiResponse) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = objectMapper.readValue(apiResponse, Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                if (message != null) {
                    return (String) message.getOrDefault("content", "");
                }
            }
        } catch (JsonProcessingException e) {
            // fall through
        }
        return "";
    }

    private String normalize(String input) {
        if (input == null) return "";
        return input.trim().toLowerCase().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fff]", " ").replaceAll("\\s+", " ").substring(0, Math.min(input.length(), 80));
    }

    /** 意图分类结果 */
    public record IntentResult(String intent, double confidence) {}
}
