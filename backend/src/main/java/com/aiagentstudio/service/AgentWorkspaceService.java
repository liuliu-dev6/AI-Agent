package com.aiagentstudio.service;

import com.aiagentstudio.dto.AgentDto;
import com.aiagentstudio.dto.ConversationCreateRequest;
import com.aiagentstudio.dto.ConversationDto;
import com.aiagentstudio.dto.KnowledgeBaseCreateRequest;
import com.aiagentstudio.dto.KnowledgeBaseDto;
import com.aiagentstudio.dto.MessageDto;
import com.aiagentstudio.dto.RunRequest;
import com.aiagentstudio.dto.RunResponse;
import com.aiagentstudio.dto.ToolCreateRequest;
import com.aiagentstudio.dto.ToolDto;
import com.aiagentstudio.model.AgentEntity;
import com.aiagentstudio.model.AgentRunEntity;
import com.aiagentstudio.model.ConversationEntity;
import com.aiagentstudio.model.KnowledgeBaseEntity;
import com.aiagentstudio.model.MessageEntity;
import com.aiagentstudio.model.ToolEntity;
import com.aiagentstudio.repo.AgentRepository;
import com.aiagentstudio.repo.AgentRunRepository;
import com.aiagentstudio.repo.ConversationRepository;
import com.aiagentstudio.repo.KnowledgeBaseRepository;
import com.aiagentstudio.repo.MessageRepository;
import com.aiagentstudio.repo.ToolRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgentWorkspaceService {
  private static final Logger log = LoggerFactory.getLogger(AgentWorkspaceService.class);

  private final AgentRepository agentRepository;
  private final ConversationRepository conversationRepository;
  private final MessageRepository messageRepository;
  private final AgentRunRepository agentRunRepository;
  private final KnowledgeBaseRepository knowledgeBaseRepository;
  private final ToolRepository toolRepository;
  private final ObjectMapper objectMapper;
  private final ModelGatewayService modelGateway;
  private final IntentService intentService;
  private final ToolService toolService;

  public AgentWorkspaceService(
      AgentRepository agentRepository,
      ConversationRepository conversationRepository,
      MessageRepository messageRepository,
      AgentRunRepository agentRunRepository,
      KnowledgeBaseRepository knowledgeBaseRepository,
      ToolRepository toolRepository,
      ObjectMapper objectMapper,
      ModelGatewayService modelGateway,
      IntentService intentService,
      ToolService toolService) {
    this.agentRepository = agentRepository;
    this.conversationRepository = conversationRepository;
    this.messageRepository = messageRepository;
    this.agentRunRepository = agentRunRepository;
    this.knowledgeBaseRepository = knowledgeBaseRepository;
    this.toolRepository = toolRepository;
    this.objectMapper = objectMapper;
    this.modelGateway = modelGateway;
    this.intentService = intentService;
    this.toolService = toolService;
  }

  public List<KnowledgeBaseDto> listKnowledgeBases() {
    ensureSeedData();
    return knowledgeBaseRepository.findAll().stream().map(kb -> {
      KnowledgeBaseDto dto = new KnowledgeBaseDto();
      dto.setId(kb.getKbKey());
      dto.setName(kb.getName());
      dto.setDescription(kb.getDescription());
      dto.setStatus(kb.getStatus());
      return dto;
    }).toList();
  }

  public List<ToolDto> listTools() {
    ensureSeedData();
    return toolRepository.findAll().stream().map(tool -> {
      ToolDto dto = new ToolDto();
      dto.setId(tool.getToolKey());
      dto.setName(tool.getName());
      dto.setType(tool.getType());
      dto.setStatus(tool.getStatus());
      return dto;
    }).toList();
  }

  public Map<String, Object> overview() {
    ensureSeedData();
    return Map.of(
        "agents", agentRepository.count(),
        "knowledgeBases", knowledgeBaseRepository.count(),
        "tools", toolRepository.count(),
        "status", "ready");
  }

  public List<AgentDto> listAgents() {
    ensureSeedData();
    return agentRepository.findAll().stream()
        .map(agent -> new AgentDto(agent.getCode(), agent.getName(), agent.getMode(), agent.getPrompt(), agent.getStatus()))
        .toList();
  }

  public List<ConversationDto> listConversations() {
    ensureSeedData();
    return conversationRepository.findAll().stream().map(conv -> {
      ConversationDto dto = new ConversationDto();
      dto.setId(conv.getConversationKey());
      dto.setTitle(conv.getTitle());
      dto.setAgentCode(conv.getAgentCode());
      dto.setCreatedAt(conv.getCreatedAt().toEpochSecond(ZoneOffset.UTC));
      dto.setUpdatedAt(conv.getUpdatedAt().toEpochSecond(ZoneOffset.UTC));
      return dto;
    }).toList();
  }

  public List<MessageDto> listMessages(String conversationId) {
    return messageRepository.findByConversationKeyOrderByCreatedAtAsc(conversationId).stream().map(msg -> {
      MessageDto dto = new MessageDto();
      dto.setId(String.valueOf(msg.getId()));
      dto.setConversationId(msg.getConversationKey());
      dto.setRole(msg.getRole());
      dto.setContent(msg.getContent());
      dto.setReasoningContent(msg.getReasoningContent());
      dto.setCreatedAt(msg.getCreatedAt().toEpochSecond(ZoneOffset.UTC));
      return dto;
    }).toList();
  }

  public List<RunResponse> listRuns(String conversationId) {
    return agentRunRepository.findByConversationKeyOrderByCreatedAtDesc(conversationId).stream().map(this::toRunResponse).toList();
  }

  @Transactional
  public ConversationDto createConversation(ConversationCreateRequest request) {
    ensureSeedData();
    ConversationEntity conversation = new ConversationEntity();
    conversation.setConversationKey(UUID.randomUUID().toString());
    conversation.setTitle(request.getTitle() == null || request.getTitle().isBlank() ? "新 Agent 会话" : request.getTitle());
    conversation.setAgentCode(request.getAgentCode() == null || request.getAgentCode().isBlank() ? "general-agent" : request.getAgentCode());
    conversation.setCreatedAt(LocalDateTime.now());
    conversation.setUpdatedAt(LocalDateTime.now());
    ConversationEntity saved = conversationRepository.save(conversation);
    return toConversationDto(saved);
  }

  @Transactional
  public KnowledgeBaseDto createKnowledgeBase(KnowledgeBaseCreateRequest request) {
    ensureSeedData();
    KnowledgeBaseEntity kb = new KnowledgeBaseEntity();
    kb.setKbKey(UUID.randomUUID().toString());
    kb.setName(request.getName());
    kb.setDescription(request.getDescription());
    kb.setStatus(request.getStatus() == null ? "online" : request.getStatus());
    kb.setCreatedAt(LocalDateTime.now());
    kb.setUpdatedAt(LocalDateTime.now());
    KnowledgeBaseEntity saved = knowledgeBaseRepository.save(kb);
    return toKnowledgeBaseDto(saved);
  }

  @Transactional
  public ToolDto createTool(ToolCreateRequest request) {
    ensureSeedData();
    ToolEntity tool = new ToolEntity();
    tool.setToolKey(UUID.randomUUID().toString());
    tool.setName(request.getName());
    tool.setType(request.getType());
    tool.setStatus(request.getStatus() == null ? "enabled" : request.getStatus());
    tool.setCreatedAt(LocalDateTime.now());
    tool.setUpdatedAt(LocalDateTime.now());
    ToolEntity saved = toolRepository.save(tool);
    return toToolDto(saved);
  }

  @Transactional
  public void deleteKnowledgeBase(String id) {
    knowledgeBaseRepository.findAll().stream().filter(item -> id.equals(item.getKbKey())).findFirst().ifPresent(knowledgeBaseRepository::delete);
  }

  @Transactional
  public void deleteTool(String id) {
    toolRepository.findAll().stream().filter(item -> id.equals(item.getToolKey())).findFirst().ifPresent(toolRepository::delete);
  }

  @Transactional
  public void deleteConversation(String id) {
    conversationRepository.findAll().stream().filter(item -> id.equals(item.getConversationKey())).findFirst().ifPresent(conversationRepository::delete);
  }

  public RunResponse run(RunRequest request) {
    ensureSeedData();

    // 1. 基础信息
    AgentEntity agent = agentRepository.findByCode(request.getAgentId())
        .orElseGet(() -> agentRepository.findByCode("general-agent").orElseThrow());
    String conversationKey = request.getConversationId();
    if (conversationKey == null || conversationKey.isBlank()) conversationKey = UUID.randomUUID().toString();
    ensureConversation(conversationKey, agent.getCode());

    String userText = extractUserText(request);
    if (userText.isBlank()) userText = "Hello";

    String apiKey = request.getApiKey() != null && !request.getApiKey().isBlank()
        ? request.getApiKey()
        : "";
    String model = request.getModel() != null && !request.getModel().isBlank()
        ? request.getModel()
        : "doubao-seed-2-0-pro-260215";

    log.info("Run start: model={} userText={} hasApiKey={}", model, userText, !apiKey.isBlank());
    long startTime = System.currentTimeMillis();

    // 2. 意图识别（仅关键词，不调 LLM — 传空 apiKey 跳过 LLM 分类）
    IntentService.IntentResult intentResult = intentService.classify(userText, model, "");
    String intent = intentResult.intent();

    // 3. 构建消息
    List<Map<String, Object>> messages = buildMessages(request, agent, userText);
    List<Map<String, Object>> toolDefs = toolService.getToolDefinitions();

    // 4. 单次 LLM 调用（带工具定义）+ 工具调用循环
    int maxRounds = 5;
    int promptTokens = 0;
    int completionTokens = 0;
    String assistantMessage = "";
    String reasoning = "";
    List<Map<String, Object>> toolsUsed = new ArrayList<>();

    try {
      for (int round = 0; round < maxRounds; round++) {
        String llmResponse = modelGateway.chatSync(model, apiKey, messages, toolDefs, 4096, 0.7);

        // 解析
        Map<String, Object> parsed = parseLLMResponse(llmResponse);
        if (parsed.isEmpty()) {
          // 解析失败，可能是 API 返回了错误
          String errorMsg = extractError(llmResponse);
          assistantMessage = errorMsg.isEmpty() ? "模型返回异常，请检查 API Key 和模型名称是否正确。" : errorMsg;
          break;
        }

        List<Map<String, Object>> choices = getList(parsed, "choices");
        Map<String, Object> usage = getMap(parsed, "usage");
        promptTokens += ((Number) usage.getOrDefault("prompt_tokens", 0)).intValue();
        completionTokens += ((Number) usage.getOrDefault("completion_tokens", 0)).intValue();

        if (choices == null || choices.isEmpty()) {
          assistantMessage = "模型未返回有效回复，请重试。";
          break;
        }

        Map<String, Object> choice = choices.get(0);
        Map<String, Object> messageObj = getMap(choice, "message");
        if (messageObj == null) {
          assistantMessage = "回复格式异常，请重试。";
          break;
        }

        String finishReason = (String) choice.getOrDefault("finish_reason", "stop");
        String content = (String) messageObj.getOrDefault("content", "");
        assistantMessage = content != null ? content : "";
        reasoning = (String) messageObj.getOrDefault("reasoning_content", "");

        // 检查 tool_calls
        List<Map<String, Object>> toolCalls = getList(messageObj, "tool_calls");
        if (toolCalls != null && !toolCalls.isEmpty() && "tool_calls".equals(finishReason)) {
          log.info("Tool calls: {}", toolCalls.size());
          messages.add(Map.of("role", "assistant", "content", content != null ? content : "",
              "tool_calls", (Object) toolCalls));

          for (Map<String, Object> tc : toolCalls) {
            String toolCallId = (String) tc.getOrDefault("id", UUID.randomUUID().toString());
            @SuppressWarnings("unchecked")
            Map<String, Object> function = (Map<String, Object>) tc.get("function");
            String toolName = (String) function.get("name");
            String argsStr = (String) function.get("arguments");

            Map<String, Object> args = new HashMap<>();
            try {
              if (argsStr != null && !argsStr.isBlank()) {
                args = objectMapper.readValue(argsStr, Map.class);
              }
            } catch (Exception e) {
              log.warn("Bad tool args: {}", argsStr);
            }

            String toolResult = toolService.execute(toolName, args);
            toolsUsed.add(Map.of("name", toolName, "status", "success", "result", toolResult));

            messages.add(Map.of("role", "tool", "tool_call_id", toolCallId, "name", toolName, "content", toolResult));
          }
          continue; // 继续循环
        }

        break; // 正常回复
      }
    } catch (Exception e) {
      log.error("Agent error: {}", e.getMessage(), e);
      assistantMessage = "模型调用失败：" + e.getMessage();
    }

    long latencyMs = System.currentTimeMillis() - startTime;

    if (assistantMessage == null || assistantMessage.isBlank()) {
      assistantMessage = "未能获取有效回复，请检查网络连接和 API 配置。";
    }
    if (reasoning == null) reasoning = "";

    // 5. 持久化（截断过长内容，防止 DB 字段溢出）
    try {
      saveMessage(conversationKey, "user", truncate(userText, 10000), null);
      saveMessage(conversationKey, "assistant", truncate(assistantMessage, 10000), truncate(reasoning, 5000));
    } catch (Exception e) {
      log.warn("Failed to save messages: {}", e.getMessage());
    }

    RunResponse response = new RunResponse();
    response.setRunId(UUID.randomUUID().toString());
    response.setConversationId(conversationKey);
    response.setAgentId(agent.getCode());
    response.setIntent(intent);
    response.setRoute(agent.getName());
    response.setAssistantMessage(assistantMessage);
    response.setReasoning(reasoning);
    response.setTools(toolsUsed);
    response.setKnowledgeHits(List.of());
    response.setWorkflow(null);
    response.setMemory(List.of());
    response.setMetrics(Map.of("promptTokens", promptTokens, "completionTokens", completionTokens, "latencyMs", latencyMs));
    try {
      persistRun(response);
      touchConversation(conversationKey, userText);
    } catch (Exception e) {
      log.warn("Failed to persist run: {}", e.getMessage());
    }
    return response;
  }

  private String truncate(String s, int max) {
    if (s == null) return null;
    return s.length() > max ? s.substring(0, max) : s;
  }

  /** 从 LLM 错误响应中提取错误信息 */
  private String extractError(String json) {
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> map = objectMapper.readValue(json, Map.class);
      @SuppressWarnings("unchecked")
      Map<String, Object> error = (Map<String, Object>) map.get("error");
      if (error != null) {
        return "API 错误: " + error.getOrDefault("message", error.toString());
      }
    } catch (Exception ignored) {}
    return "";
  }

  /** 从 RunRequest 中提取用户文本（兼容 text 和 content 两种字段） */
  private String extractUserText(RunRequest request) {
    if (request.getMessage() == null) return "";
    // 优先使用 content 字段
    String content = request.getMessage().getContent();
    if (content != null && !content.isBlank()) return content;
    // 回退到 text 字段（前端 ChatInput 发送的格式）
    return request.getMessage().getText() != null ? request.getMessage().getText() : "";
  }

  /** 构建 LLM 用的消息列表 */
  private List<Map<String, Object>> buildMessages(RunRequest request, AgentEntity agent, String userText) {
    List<Map<String, Object>> messages = new ArrayList<>();

    // 系统提示词
    String systemPrompt = agent.getPrompt() + "\n\n"
        + "你是一个企业级 AI Agent。请根据用户意图选择合适的能力响应。\n"
        + "如果需要计算，使用 calculator 工具。如果需要查询天气，使用 get_weather 工具。\n"
        + "当前时间: " + java.time.LocalDateTime.now().toString();
    messages.add(Map.of("role", "system", "content", (Object) systemPrompt));

    // 历史消息（最多保留最近 20 条）
    if (request.getHistory() != null) {
      int skip = Math.max(0, request.getHistory().size() - 20);
      request.getHistory().stream().skip(skip).forEach(msg -> {
        String role = msg.getRole() != null ? msg.getRole() : "user";
        String msgContent = msg.getContent() != null ? msg.getContent() : (msg.getText() != null ? msg.getText() : "");
        if (!msgContent.isBlank()) {
          messages.add(Map.of("role", role, "content", (Object) msgContent));
        }
      });
    }

    // 当前用户输入
    messages.add(Map.of("role", "user", "content", (Object) userText));

    return messages;
  }

  /** 解析 LLM 返回的 JSON */
  @SuppressWarnings("unchecked")
  private Map<String, Object> parseLLMResponse(String json) {
    try {
      return objectMapper.readValue(json, Map.class);
    } catch (Exception e) {
      log.warn("Failed to parse LLM response: {}", e.getMessage());
      return Map.of();
    }
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> getList(Map<String, Object> map, String key) {
    Object val = map.get(key);
    return val instanceof List ? (List<Map<String, Object>>) val : null;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getMap(Map<String, Object> map, String key) {
    Object val = map.get(key);
    return val instanceof Map ? (Map<String, Object>) val : Map.of();
  }

  private void ensureSeedData() {
    if (agentRepository.count() == 0) {
      seedAgent("general-agent", "General Agent", "chat + tools", "你是一个企业级 Agent，优先识别意图并路由到正确能力。");
      seedAgent("knowledge-agent", "Knowledge Agent", "rag + search", "优先基于知识库回答，必要时发起检索和重排。");
      seedAgent("workflow-agent", "Workflow Agent", "automation + mcp", "将复杂任务分解为可执行工作流，并可调用外部系统。");
    }
    if (knowledgeBaseRepository.count() == 0) {
      seedKnowledgeBase("kb-default", "Default KB", "默认企业知识库", "online");
    }
    if (toolRepository.count() == 0) {
      seedTool("tool-weather", "Weather API", "http", "enabled");
      seedTool("tool-mysql", "MySQL Query", "database", "connected");
    }
  }

  private void seedAgent(String code, String name, String mode, String prompt) {
    AgentEntity agent = new AgentEntity();
    agent.setCode(code); agent.setName(name); agent.setMode(mode); agent.setPrompt(prompt); agent.setStatus("online"); agent.setCreatedAt(LocalDateTime.now()); agent.setUpdatedAt(LocalDateTime.now()); agentRepository.save(agent);
  }

  private void seedKnowledgeBase(String key, String name, String description, String status) {
    KnowledgeBaseEntity kb = new KnowledgeBaseEntity();
    kb.setKbKey(key); kb.setName(name); kb.setDescription(description); kb.setStatus(status); kb.setCreatedAt(LocalDateTime.now()); kb.setUpdatedAt(LocalDateTime.now()); knowledgeBaseRepository.save(kb);
  }

  private void seedTool(String key, String name, String type, String status) {
    ToolEntity tool = new ToolEntity();
    tool.setToolKey(key); tool.setName(name); tool.setType(type); tool.setStatus(status); tool.setCreatedAt(LocalDateTime.now()); tool.setUpdatedAt(LocalDateTime.now()); toolRepository.save(tool);
  }

  private ConversationDto toConversationDto(ConversationEntity conv) {
    ConversationDto dto = new ConversationDto();
    dto.setId(conv.getConversationKey()); dto.setTitle(conv.getTitle()); dto.setAgentCode(conv.getAgentCode()); dto.setCreatedAt(conv.getCreatedAt().toEpochSecond(ZoneOffset.UTC)); dto.setUpdatedAt(conv.getUpdatedAt().toEpochSecond(ZoneOffset.UTC)); return dto;
  }

  private KnowledgeBaseDto toKnowledgeBaseDto(KnowledgeBaseEntity kb) {
    KnowledgeBaseDto dto = new KnowledgeBaseDto();
    dto.setId(kb.getKbKey()); dto.setName(kb.getName()); dto.setDescription(kb.getDescription()); dto.setStatus(kb.getStatus()); return dto;
  }

  private ToolDto toToolDto(ToolEntity tool) {
    ToolDto dto = new ToolDto();
    dto.setId(tool.getToolKey()); dto.setName(tool.getName()); dto.setType(tool.getType()); dto.setStatus(tool.getStatus()); return dto;
  }

  private void ensureConversation(String conversationKey, String agentCode) {
    conversationRepository.findByConversationKey(conversationKey).orElseGet(() -> {
      ConversationEntity conversation = new ConversationEntity();
      conversation.setConversationKey(conversationKey);
      conversation.setTitle("新 Agent 会话");
      conversation.setAgentCode(agentCode);
      conversation.setCreatedAt(LocalDateTime.now());
      conversation.setUpdatedAt(LocalDateTime.now());
      return conversationRepository.save(conversation);
    });
  }

  private void saveMessage(String conversationKey, String role, String content, String reasoning) {
    MessageEntity entity = new MessageEntity();
    entity.setConversationKey(conversationKey); entity.setRole(role); entity.setContent(content); entity.setReasoningContent(reasoning); entity.setCreatedAt(LocalDateTime.now()); messageRepository.save(entity);
  }

  private void touchConversation(String conversationKey, String titleSeed) {
    conversationRepository.findByConversationKey(conversationKey).ifPresent(conversation -> {
      if ("新 Agent 会话".equals(conversation.getTitle()) && titleSeed != null && !titleSeed.isBlank()) conversation.setTitle(titleSeed.length() > 12 ? titleSeed.substring(0, 12) : titleSeed);
      conversation.setUpdatedAt(LocalDateTime.now());
      conversationRepository.save(conversation);
    });
  }

  private void persistRun(RunResponse response) {
    AgentRunEntity entity = new AgentRunEntity();
    entity.setRunKey(response.getRunId());
    entity.setConversationKey(response.getConversationId());
    entity.setAgentCode(response.getAgentId());
    entity.setIntent(response.getIntent());
    entity.setRouteName(response.getRoute());

    Map<String, Object> trace = new HashMap<>();
    trace.put("tools", response.getTools() != null ? response.getTools() : List.of());
    trace.put("knowledgeHits", response.getKnowledgeHits() != null ? response.getKnowledgeHits() : List.of());
    trace.put("workflow", response.getWorkflow());
    trace.put("memory", response.getMemory() != null ? response.getMemory() : List.of());
    entity.setTraceJson(writeJson(trace));

    entity.setMetricsJson(writeJson(response.getMetrics() != null ? response.getMetrics() : Map.of()));
    entity.setCreatedAt(LocalDateTime.now());
    agentRunRepository.save(entity);
  }

  private RunResponse toRunResponse(AgentRunEntity run) {
    RunResponse dto = new RunResponse();
    dto.setRunId(run.getRunKey()); dto.setConversationId(run.getConversationKey()); dto.setAgentId(run.getAgentCode()); dto.setIntent(run.getIntent()); dto.setRoute(run.getRouteName()); dto.setMetrics(readMap(run.getMetricsJson())); Map<String, Object> trace = readMap(run.getTraceJson()); dto.setTools((List<Map<String, Object>>) trace.getOrDefault("tools", List.of())); dto.setKnowledgeHits((List<Map<String, Object>>) trace.getOrDefault("knowledgeHits", List.of())); dto.setWorkflow((Map<String, Object>) trace.getOrDefault("workflow", null)); dto.setMemory((List<Map<String, Object>>) trace.getOrDefault("memory", List.of())); return dto;
  }

  private Map<String, Object> readMap(String json) {
    try { if (json == null || json.isBlank()) return Map.of(); return objectMapper.readValue(json, Map.class); } catch (Exception e) { return Map.of(); }
  }

  private String writeJson(Object value) {
    try { return objectMapper.writeValueAsString(value); } catch (JsonProcessingException e) { return "{}"; }
  }
}
