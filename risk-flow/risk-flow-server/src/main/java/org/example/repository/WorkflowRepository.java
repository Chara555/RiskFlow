package org.example.repository;

import org.example.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {

    Optional<Workflow> findByCode(String code);

    Optional<Workflow> findByCodeAndStatus(String code, String status);
}
