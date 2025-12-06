package com.bugshot.domain.project.repository;

import com.bugshot.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {

    List<Project> findByUserId(String userId);

    Optional<Project> findByApiKey(String apiKey);

    boolean existsByApiKey(String apiKey);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.user.id = :userId")
    long countByUserId(@Param("userId") String userId);

    @Query("SELECT p FROM Project p WHERE p.user.id = :userId AND p.id = :projectId")
    Optional<Project> findByUserIdAndProjectId(@Param("userId") String userId,
                                                 @Param("projectId") String projectId);
}
