package com.aiagentstudio.dto;

public class MessageDto {
  private String id;
  private String conversationId;
  private String role;
  private String content;
  private String reasoningContent;
  private Long createdAt;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getConversationId() { return conversationId; }
  public void setConversationId(String conversationId) { this.conversationId = conversationId; }
  public String getRole() { return role; }
  public void setRole(String role) { this.role = role; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public String getReasoningContent() { return reasoningContent; }
  public void setReasoningContent(String reasoningContent) { this.reasoningContent = reasoningContent; }
  public Long getCreatedAt() { return createdAt; }
  public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
}
