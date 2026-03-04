package org.example.repository;

import org.example.entity.Blacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {

    Optional<Blacklist> findByTypeAndValue(String type, String value);

    List<Blacklist> findByType(String type);

    boolean existsByTypeAndValueAndExpireTimeIsNullOrExpireTimeAfter(String type, String value);
}
