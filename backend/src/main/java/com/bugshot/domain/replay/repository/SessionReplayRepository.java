package com.bugshot.domain.replay.repository;

import com.bugshot.domain.replay.entity.SessionReplay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionReplayRepository extends JpaRepository<SessionReplay, String> {

    Optional<SessionReplay> findBySessionId(String sessionId);

    Optional<SessionReplay> findByErrorOccurrenceId(String errorOccurrenceId);

    List<SessionReplay> findByProjectId(String projectId);

    @Query("SELECT sr FROM SessionReplay sr WHERE sr.expiresAt < :now")
    List<SessionReplay> findExpiredReplays(@Param("now") LocalDateTime now);

    @Query("DELETE FROM SessionReplay sr WHERE sr.expiresAt < :now")
    void deleteExpiredReplays(@Param("now") LocalDateTime now);
}
