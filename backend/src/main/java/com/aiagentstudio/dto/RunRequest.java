package com.aiagentstudio.dto;

import java.util.List;
import java.util.Map;

public class RunRequest {
  private String conversationId;
  private String agentId;
  private String apiKey;
  private String model;
  private MessagePayload message;
  private List<MessagePayload> history;

  public String getConversationId() { return conversationId; }
  public void setConversationId(String conversationId) { this.conversationId = conversationId; }
  public String getAgentId() { return agentId; }
  public void setAgentId(String agentId) { this.agentId = agentId; }
  public String getApiKey() { return apiKey; }
  public void setApiKey(String apiKey) { this.apiKey = apiKey; }
  public String getModel() { return model; }
  public void setModel(String model) { this.model = model; }
  public MessagePayload getMessage() { return message; }
  public void setMessage(MessagePayload message) { this.message = message; }
  public List<MessagePayload> getHistory() { return history; }
  public void setHistory(List<MessagePayload> history) { this.history = history; }

  public static class MessagePayload {
    private String role;
    private String content;
    private String text;     // 兼容前端 ChatInput 发送的 { text, files } 格式
    private String reasoningContent;
    private List<Map<String, Object>> files;

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content != null ? content : text; }
    public void setContent(String content) { this.content = content; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getReasoningContent() { return reasoningContent; }
    public void setReasoningContent(String reasoningContent) { this.reasoningContent = reasoningContent; }
    public List<Map<String, Object>> getFiles() { return files; }
    public void setFiles(List<Map<String, Object>> files) { this.files = files; }
  }
}
