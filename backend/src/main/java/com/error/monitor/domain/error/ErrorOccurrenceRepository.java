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
public interface ErrorOccurrenceRepository extends JpaRepository<ErrorOccurrence, String> {

    Page<ErrorOccurrence> findByErrorId(String errorId, Pageable pageable);

    List<ErrorOccurrence> findByErrorIdOrderByOccurredAtDesc(String errorId);

    Optional<ErrorOccurrence> findFirstByErrorIdOrderByOccurredAtDesc(String errorId);

    @Query("SELECT COUNT(DISTINCT eo.userIdentifier) FROM ErrorOccurrence eo WHERE eo.error.id = :errorId")
    long countDistinctUsersByErrorId(@Param("errorId") String errorId);

    @Query("SELECT eo FROM ErrorOccurrence eo WHERE eo.error.id = :errorId " +
           "AND eo.occurredAt >= :since " +
           "ORDER BY eo.occurredAt DESC")
    List<ErrorOccurrence> findRecentOccurrences(@Param("errorId") String errorId,
                                                  @Param("since") LocalDateTime since);

    List<ErrorOccurrence> findBySessionId(String sessionId);
}
