package com.error.monitor.domain.error;

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

    @Query("SELECT COUNT(DISTINCT e.affectedUsersCount) FROM Error e WHERE e.project.id = :projectId")
    long countTotalAffectedUsers(@Param("projectId") String projectId);

    // Find top errors by priority score
    List<Error> findTop10ByOrderByPriorityScoreDesc();

    // Find latest error
    Optional<Error> findTopByOrderByLastSeenAtDesc();
}
