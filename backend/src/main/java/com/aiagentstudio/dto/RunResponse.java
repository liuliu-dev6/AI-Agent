package com.aiagentstudio.dto;

import java.util.List;
import java.util.Map;

public class RunResponse {
  private String runId;
  private String conversationId;
  private String agentId;
  private String intent;
  private String route;
  private String assistantMessage;
  private String reasoning;
  private List<Map<String, Object>> tools;
  private List<Map<String, Object>> knowledgeHits;
  private Map<String, Object> workflow;
  private List<Map<String, Object>> memory;
  private Map<String, Object> metrics;

  public String getRunId() { return runId; }
  public void setRunId(String runId) { this.runId = runId; }
  public String getConversationId() { return conversationId; }
  public void setConversationId(String conversationId) { this.conversationId = conversationId; }
  public String getAgentId() { return agentId; }
  public void setAgentId(String agentId) { this.agentId = agentId; }
  public String getIntent() { return intent; }
  public void setIntent(String intent) { this.intent = intent; }
  public String getRoute() { return route; }
  public void setRoute(String route) { this.route = route; }
  public String getAssistantMessage() { return assistantMessage; }
  public void setAssistantMessage(String assistantMessage) { this.assistantMessage = assistantMessage; }
  public String getReasoning() { return reasoning; }
  public void setReasoning(String reasoning) { this.reasoning = reasoning; }
  public List<Map<String, Object>> getTools() { return tools; }
  public void setTools(List<Map<String, Object>> tools) { this.tools = tools; }
  public List<Map<String, Object>> getKnowledgeHits() { return knowledgeHits; }
  public void setKnowledgeHits(List<Map<String, Object>> knowledgeHits) { this.knowledgeHits = knowledgeHits; }
  public Map<String, Object> getWorkflow() { return workflow; }
  public void setWorkflow(Map<String, Object> workflow) { this.workflow = workflow; }
  public List<Map<String, Object>> getMemory() { return memory; }
  public void setMemory(List<Map<String, Object>> memory) { this.memory = memory; }
  public Map<String, Object> getMetrics() { return metrics; }
  public void setMetrics(Map<String, Object> metrics) { this.metrics = metrics; }
}
