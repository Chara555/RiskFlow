package org.example.repository;

import org.example.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /** 根据用户ID查询画像 */
    Optional<UserProfile> findByUserId(String userId);

    /** 根据用户等级查询 */
    List<UserProfile> findByUserLevel(String userLevel);

    /** 根据风险等级查询 */
    List<UserProfile> findByRiskLevel(String riskLevel);

    /** 根据用户等级和风险等级查询 */
    List<UserProfile> findByUserLevelAndRiskLevel(String userLevel, String riskLevel);

    /** 更新统计信息（事件发生后调用） */
    @Modifying
    @Query("UPDATE UserProfile u SET " +
           "u.totalEvents = u.totalEvents + 1, " +
           "u.lastEventTime = CURRENT_TIMESTAMP, " +
           "u.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE u.userId = :userId")
    void incrementTotalEvents(@Param("userId") String userId);
}
