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
@Table(name = "agent_run")
public class AgentRunEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String runKey;

  @Column(nullable = false)
  private String conversationKey;

  @Column(nullable = false)
  private String agentCode;

  @Column(nullable = false)
  private String intent;

  @Column(nullable = false)
  private String routeName;

  @Lob
  @Column(columnDefinition = "MEDIUMTEXT")
  private String traceJson;

  @Lob
  @Column(columnDefinition = "MEDIUMTEXT")
  private String metricsJson;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getRunKey() { return runKey; }
  public void setRunKey(String runKey) { this.runKey = runKey; }
  public String getConversationKey() { return conversationKey; }
  public void setConversationKey(String conversationKey) { this.conversationKey = conversationKey; }
  public String getAgentCode() { return agentCode; }
  public void setAgentCode(String agentCode) { this.agentCode = agentCode; }
  public String getIntent() { return intent; }
  public void setIntent(String intent) { this.intent = intent; }
  public String getRouteName() { return routeName; }
  public void setRouteName(String routeName) { this.routeName = routeName; }
  public String getTraceJson() { return traceJson; }
  public void setTraceJson(String traceJson) { this.traceJson = traceJson; }
  public String getMetricsJson() { return metricsJson; }
  public void setMetricsJson(String metricsJson) { this.metricsJson = metricsJson; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
