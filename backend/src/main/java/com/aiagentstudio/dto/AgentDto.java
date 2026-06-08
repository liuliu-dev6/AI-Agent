package com.aiagentstudio.dto;

public class AgentDto {
  private String id;
  private String name;
  private String mode;
  private String prompt;
  private String status;

  public AgentDto() {}

  public AgentDto(String id, String name, String mode, String prompt, String status) {
    this.id = id;
    this.name = name;
    this.mode = mode;
    this.prompt = prompt;
    this.status = status;
  }

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getMode() { return mode; }
  public void setMode(String mode) { this.mode = mode; }
  public String getPrompt() { return prompt; }
  public void setPrompt(String prompt) { this.prompt = prompt; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
