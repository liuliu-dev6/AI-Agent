package com.aiagentstudio.repo;

import com.aiagentstudio.model.ConversationEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<ConversationEntity, Long> {
  Optional<ConversationEntity> findByConversationKey(String conversationKey);
}
