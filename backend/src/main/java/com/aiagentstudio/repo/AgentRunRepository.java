package com.aiagentstudio.repo;

import com.aiagentstudio.model.AgentRunEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRunRepository extends JpaRepository<AgentRunEntity, Long> {
  List<AgentRunEntity> findTop10ByOrderByCreatedAtDesc();
  List<AgentRunEntity> findByConversationKeyOrderByCreatedAtDesc(String conversationKey);
}
