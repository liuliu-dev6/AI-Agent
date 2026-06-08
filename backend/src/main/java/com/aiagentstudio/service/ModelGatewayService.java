package com.aiagentstudio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * 多模型统一网关 — 使用 HttpURLConnection（比 JDK HttpClient 更可靠）
 */
@Service
public class ModelGatewayService {

    private static final Logger log = LoggerFactory.getLogger(ModelGatewayService.class);
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final Map<String, String> providerBaseUrls;

    public ModelGatewayService(ObjectMapper objectMapper,
            @Value("${model-gateway.providers.openai.base-url:https://api.openai.com/v1}") String openaiUrl,
            @Value("${model-gateway.providers.deepseek.base-url:https://api.deepseek.com/v1}") String deepseekUrl,
            @Value("${model-gateway.providers.qwen.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}") String qwenUrl,
            @Value("${model-gateway.providers.zhipu.base-url:https://open.bigmodel.cn/api/paas/v4}") String zhipuUrl,
            @Value("${model-gateway.providers.doubao.base-url:https://ark.cn-beijing.volces.com/api/v3}") String doubaoUrl) {

        this.objectMapper = objectMapper;

        // HttpURLConnection — 最可靠的 HTTP 客户端
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(120));

        this.restClient = RestClient.builder().requestFactory(factory).build();

        this.providerBaseUrls = Map.of(
                "openai", openaiUrl,
                "deepseek", deepseekUrl,
                "qwen", qwenUrl,
                "zhipu", zhipuUrl,
                "doubao", doubaoUrl
        );
    }

    private String resolveProvider(String model) {
        if (model == null) return "doubao";
        if (model.contains("gpt") || model.contains("o1") || model.contains("o3")) return "openai";
        if (model.contains("deepseek")) return "deepseek";
        if (model.contains("qwen")) return "qwen";
        if (model.contains("glm")) return "zhipu";
        if (model.contains("doubao")) return "doubao";
        return "doubao";
    }

    /**
     * 调用 LLM（同步），返回原始 JSON 字符串
     */
    public String chatSync(String model, String apiKey, List<Map<String, Object>> messages,
            List<Map<String, Object>> tools, int maxTokens, double temperature) {

        String provider = resolveProvider(model);
        String baseUrl = providerBaseUrls.getOrDefault(provider, providerBaseUrls.get("doubao"));
        String url = baseUrl + "/chat/completions";

        Map<String, Object> body = new java.util.HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("stream", false);
        body.put("max_tokens", maxTokens);
        body.put("temperature", temperature);

        if (tools != null && !tools.isEmpty()) {
            body.put("tools", tools);
            body.put("tool_choice", "auto");
        }

        log.info("LLM call: model={} url={}", model, url);
        log.debug("Request body: {}", body);

        try {
            String response = restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            log.debug("LLM response length: {}", response != null ? response.length() : 0);
            return response;
        } catch (Exception e) {
            log.error("LLM call failed: model={} error={}", model, e.toString());
            throw new RuntimeException("LLM API error: " + e.getMessage(), e);
        }
    }
}
