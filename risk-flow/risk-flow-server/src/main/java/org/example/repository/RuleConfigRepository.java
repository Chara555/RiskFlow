package org.example.repository;

import org.example.entity.RuleConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RuleConfigRepository extends JpaRepository<RuleConfig, Long> {

    /**
     * 根据规则代码查找配置
     */
    Optional<RuleConfig> findByCode(String code);

    List<RuleConfig> findByEnabledTrue();

    List<RuleConfig> findByType(String type);
}
