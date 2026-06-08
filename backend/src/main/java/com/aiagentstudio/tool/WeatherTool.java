package com.aiagentstudio.tool;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 天气查询工具 — 使用 wttr.in 免费 API
 */
@Component
public class WeatherTool {

    private static final Logger log = LoggerFactory.getLogger(WeatherTool.class);

    private final RestClient restClient;

    public WeatherTool() {
        this.restClient = RestClient.create();
    }

    public String getName() { return "get_weather"; }

    public String getDescription() {
        return "Get current weather information for a city. Returns temperature, conditions, humidity, and wind speed.";
    }

    public Map<String, Object> getParameters() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "city", Map.of(
                                "type", "string",
                                "description", "City name, e.g. 'Beijing', 'Shanghai', 'Tokyo'"
                        )
                ),
                "required", java.util.List.of("city")
        );
    }

    /**
     * 执行天气查询
     */
    public String execute(Map<String, Object> params) {
        String city = (String) params.get("city");
        if (city == null || city.isBlank()) {
            return "Error: city name is required";
        }

        try {
            // wttr.in 免费天气 API（返回纯文本）
            String url = "https://wttr.in/" + city + "?format=j1&lang=zh";
            log.info("Querying weather for: {}", city);

            String response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            // 解析 JSON 提取关键信息
            return parseWeatherResponse(response, city);

        } catch (Exception e) {
            log.warn("Weather query failed for {}: {}", city, e.getMessage());

            // Fallback: 返回简单格式的天气
            try {
                String fallback = restClient.get()
                        .uri("https://wttr.in/" + city + "?format=3&lang=zh")
                        .retrieve()
                        .body(String.class);
                return "当前" + fallback.trim();
            } catch (Exception e2) {
                return "抱歉，无法获取 " + city + " 的天气信息。请检查城市名称是否正确。";
            }
        }
    }

    private String parseWeatherResponse(String json, String city) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> root = mapper.readValue(json, Map.class);

            // wttr.in: current_condition 是数组，取第一个元素
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> conditionList =
                (java.util.List<Map<String, Object>>) root.get("current_condition");

            if (conditionList == null || conditionList.isEmpty()) {
                return "未找到 " + city + " 的天气数据";
            }

            Map<String, Object> weather = conditionList.get(0);

            String tempC = String.valueOf(weather.getOrDefault("temp_C", "N/A"));
            String feelsLike = String.valueOf(weather.getOrDefault("FeelsLikeC", "N/A"));
            String humidity = String.valueOf(weather.getOrDefault("humidity", "N/A"));
            String windSpeed = String.valueOf(weather.getOrDefault("windspeedKmph", "N/A"));
            String windDir = String.valueOf(weather.getOrDefault("winddir16Point", "N/A"));

            String weatherDesc = "未知";
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> descList = (java.util.List<Map<String, Object>>) weather.get("weatherDesc");
            if (descList != null && !descList.isEmpty()) {
                weatherDesc = String.valueOf(descList.get(0).getOrDefault("value", "未知"));
            }

            String visibility = String.valueOf(weather.getOrDefault("visibility", "N/A"));
            String pressure = String.valueOf(weather.getOrDefault("pressure", "N/A"));
            String uvIndex = String.valueOf(weather.getOrDefault("uvIndex", "N/A"));

            return String.format(
                    "城市: %s\n天气: %s\n温度: %s°C (体感 %s°C)\n湿度: %s%%\n风速: %s km/h (%s)\n能见度: %s km\n气压: %s hPa\nUV 指数: %s",
                    city, weatherDesc, tempC, feelsLike, humidity,
                    windSpeed, windDir, visibility, pressure, uvIndex
            );
        } catch (Exception e) {
            log.warn("Failed to parse weather JSON: {}", e.getMessage());
            return "查询到 " + city + " 的天气数据，但解析失败: " + e.getMessage();
        }
    }
}
