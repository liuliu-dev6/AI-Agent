package com.aiagentstudio.repo;

import com.aiagentstudio.model.MessageEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
  List<MessageEntity> findByConversationKeyOrderByCreatedAtAsc(String conversationKey);
}
