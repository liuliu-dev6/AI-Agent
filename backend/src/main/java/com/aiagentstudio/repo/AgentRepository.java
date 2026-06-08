package com.aiagentstudio.repo;

import com.aiagentstudio.model.AgentEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<AgentEntity, Long> {
  Optional<AgentEntity> findByCode(String code);
}
