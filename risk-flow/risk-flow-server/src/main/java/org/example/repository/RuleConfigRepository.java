package org.example.repository;

import org.example.entity.RuleConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RuleConfigRepository extends JpaRepository<RuleConfig, Long> {

    List<RuleConfig> findByEnabledTrue();

    List<RuleConfig> findByEnabledTrueOrderByPriorityAsc();

    List<RuleConfig> findByType(String type);
}
