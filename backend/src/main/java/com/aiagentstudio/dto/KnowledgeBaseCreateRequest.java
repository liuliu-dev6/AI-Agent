package com.aiagentstudio.dto;

public class KnowledgeBaseCreateRequest {
  private String name;
  private String description;
  private String status;

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
