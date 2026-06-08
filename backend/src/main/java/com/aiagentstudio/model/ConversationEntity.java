package com.aiagentstudio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversation")
public class ConversationEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String conversationKey;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String agentCode;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getConversationKey() { return conversationKey; }
  public void setConversationKey(String conversationKey) { this.conversationKey = conversationKey; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getAgentCode() { return agentCode; }
  public void setAgentCode(String agentCode) { this.agentCode = agentCode; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
  public LocalDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
