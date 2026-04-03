package org.example.repository;

import org.example.entity.Blacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {

    Optional<Blacklist> findByTypeAndValue(String type, String value);

    List<Blacklist> findByType(String type);

    /**
     * 判断黑名单是否存在且未过期
     * 使用 JPQL 自动处理当前时间，避免传参麻烦
     */
    @Query("SELECT COUNT(b) > 0 FROM Blacklist b " +
            "WHERE b.type = :type AND b.value = :value " +
            "AND (b.expireTime IS NULL OR b.expireTime > CURRENT_TIMESTAMP)")
    boolean existsValidBlacklist(@Param("type") String type, @Param("value") String value);
}