package com.aiagentstudio.controller;

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
import com.aiagentstudio.service.AgentWorkspaceService;
import com.aiagentstudio.service.ModelGatewayService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent-workspace")
public class AgentWorkspaceController {
  private final AgentWorkspaceService agentWorkspaceService;
  private final ModelGatewayService modelGatewayService;
  public AgentWorkspaceController(AgentWorkspaceService agentWorkspaceService, ModelGatewayService modelGatewayService) {
    this.agentWorkspaceService = agentWorkspaceService;
    this.modelGatewayService = modelGatewayService;
  }
  @GetMapping("/agents") public List<AgentDto> listAgents() { return agentWorkspaceService.listAgents(); }
  @PostMapping("/runs") public RunResponse run(@RequestBody RunRequest request) { return agentWorkspaceService.run(request); }
  @GetMapping("/conversations") public List<ConversationDto> conversations() { return agentWorkspaceService.listConversations(); }
  @GetMapping("/conversations/{conversationId}/messages") public List<MessageDto> messages(@PathVariable String conversationId) { return agentWorkspaceService.listMessages(conversationId); }
  @GetMapping("/conversations/{conversationId}/runs") public List<RunResponse> runs(@PathVariable String conversationId) { return agentWorkspaceService.listRuns(conversationId); }
  @GetMapping("/knowledge-bases") public List<KnowledgeBaseDto> knowledgeBases() { return agentWorkspaceService.listKnowledgeBases(); }
  @GetMapping("/tools") public List<ToolDto> tools() { return agentWorkspaceService.listTools(); }
  @GetMapping("/overview") public Map<String, Object> overview() { return agentWorkspaceService.overview(); }
  @PostMapping("/conversations") public ConversationDto createConversation(@RequestBody ConversationCreateRequest request) { return agentWorkspaceService.createConversation(request); }
  @PostMapping("/knowledge-bases") public KnowledgeBaseDto createKnowledgeBase(@RequestBody KnowledgeBaseCreateRequest request) { return agentWorkspaceService.createKnowledgeBase(request); }
  @PostMapping("/tools") public ToolDto createTool(@RequestBody ToolCreateRequest request) { return agentWorkspaceService.createTool(request); }
  @DeleteMapping("/knowledge-bases/{id}") public void deleteKnowledgeBase(@PathVariable String id) { agentWorkspaceService.deleteKnowledgeBase(id); }
  @DeleteMapping("/tools/{id}") public void deleteTool(@PathVariable String id) { agentWorkspaceService.deleteTool(id); }
  @DeleteMapping("/conversations/{id}") public void deleteConversation(@PathVariable String id) { agentWorkspaceService.deleteConversation(id); }

  /** 简单聊天补全（用于 SearchDialog 等轻量场景） */
  @PostMapping("/chat/completions")
  public String chatCompletion(@RequestBody Map<String, Object> request) {
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> messages = (List<Map<String, Object>>) request.get("messages");
    String model = (String) request.getOrDefault("model", "deepseek-v3");
    String apiKey = (String) request.getOrDefault("apiKey", System.getenv().getOrDefault("LLM_API_KEY", ""));
    int maxTokens = request.get("max_tokens") instanceof Number n ? n.intValue() : 4096;
    double temperature = request.get("temperature") instanceof Number n ? n.doubleValue() : 0.7;

    return modelGatewayService.chatSync(model, apiKey, messages, null, maxTokens, temperature);
  }
}
