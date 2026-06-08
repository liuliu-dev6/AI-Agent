package com.aiagentstudio.dto;

public class ConversationDto {
  private String id;
  private String title;
  private String agentCode;
  private Long createdAt;
  private Long updatedAt;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getAgentCode() { return agentCode; }
  public void setAgentCode(String agentCode) { this.agentCode = agentCode; }
  public Long getCreatedAt() { return createdAt; }
  public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
  public Long getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
}
