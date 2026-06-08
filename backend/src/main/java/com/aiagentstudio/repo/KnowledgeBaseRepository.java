package com.aiagentstudio.repo;

import com.aiagentstudio.model.KnowledgeBaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBaseEntity, Long> {}
