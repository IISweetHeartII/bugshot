package com.bugshot.domain.error.repository;

import com.bugshot.domain.error.entity.Error;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ErrorRepository extends JpaRepository<Error, String> {

    Optional<Error> findByProjectIdAndErrorHash(String projectId, String errorHash);

    Page<Error> findByProjectId(String projectId, Pageable pageable);

    Page<Error> findByProjectIdAndStatus(String projectId, Error.ErrorStatus status, Pageable pageable);

    Page<Error> findByProjectIdAndSeverity(String projectId, Error.Severity severity, Pageable pageable);

    @Query("SELECT e FROM Error e WHERE e.project.id = :projectId " +
           "AND e.severity IN :severities " +
           "AND e.status = :status " +
           "ORDER BY e.priorityScore DESC, e.lastSeenAt DESC")
    Page<Error> findByProjectIdAndSeveritiesAndStatus(
        @Param("projectId") String projectId,
        @Param("severities") List<Error.Severity> severities,
        @Param("status") Error.ErrorStatus status,
        Pageable pageable
    );

    @Query("SELECT COUNT(e) FROM Error e WHERE e.project.id = :projectId AND e.severity = :severity")
    long countByProjectIdAndSeverity(@Param("projectId") String projectId,
                                       @Param("severity") Error.Severity severity);

    @Query("SELECT e FROM Error e WHERE e.project.id = :projectId " +
           "AND e.lastSeenAt >= :since " +
           "ORDER BY e.priorityScore DESC")
    List<Error> findRecentErrors(@Param("projectId") String projectId,
                                   @Param("since") LocalDateTime since);

    /**
     * 프로젝트의 총 영향받은 사용자 수 계산
     * <p>
     * 주의: 이 쿼리는 각 에러의 affectedUsersCount를 합산합니다.
     * 실제 유니크 사용자 수와는 다를 수 있습니다 (에러 간 중복 사용자 존재 가능).
     * </p>
     *
     * @param projectId 프로젝트 ID
     * @return 총 영향받은 사용자 수 (null이면 0 반환)
     */
    @Query("SELECT COALESCE(SUM(e.affectedUsersCount), 0) FROM Error e WHERE e.project.id = :projectId")
    long countTotalAffectedUsers(@Param("projectId") String projectId);

    // Find top errors by priority score
    List<Error> findTop10ByOrderByPriorityScoreDesc();

    // Find latest error
    Optional<Error> findTopByOrderByLastSeenAtDesc();

    /**
     * 여러 프로젝트의 심각도별 에러 개수를 한 번에 조회 (N+1 문제 해결)
     * @param projectIds 프로젝트 ID 목록
     * @return [projectId, severity, count] 형태의 결과 리스트
     */
    @Query("""
        SELECT e.project.id, e.severity, COUNT(e)
        FROM Error e
        WHERE e.project.id IN :projectIds
        GROUP BY e.project.id, e.severity
        """)
    List<Object[]> countErrorsBySeverityForProjects(@Param("projectIds") List<String> projectIds);

    /**
     * 단일 프로젝트의 심각도별 에러 개수를 한 번에 조회
     * @param projectId 프로젝트 ID
     * @return [severity, count] 형태의 결과 리스트
     */
    @Query("""
        SELECT e.severity, COUNT(e)
        FROM Error e
        WHERE e.project.id = :projectId
        GROUP BY e.severity
        """)
    List<Object[]> countErrorsBySeverityForProject(@Param("projectId") String projectId);
}
