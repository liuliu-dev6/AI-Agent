package com.aiagentstudio.dto;

public class ConversationCreateRequest {
  private String title;
  private String agentCode;

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getAgentCode() { return agentCode; }
  public void setAgentCode(String agentCode) { this.agentCode = agentCode; }
}
