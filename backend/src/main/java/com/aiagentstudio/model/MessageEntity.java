package com.aiagentstudio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "message")
public class MessageEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String conversationKey;

  @Column(nullable = false)
  private String role;

  @Lob
  @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
  private String content;

  @Lob
  @Column(columnDefinition = "MEDIUMTEXT")
  private String reasoningContent;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getConversationKey() { return conversationKey; }
  public void setConversationKey(String conversationKey) { this.conversationKey = conversationKey; }
  public String getRole() { return role; }
  public void setRole(String role) { this.role = role; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public String getReasoningContent() { return reasoningContent; }
  public void setReasoningContent(String reasoningContent) { this.reasoningContent = reasoningContent; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
