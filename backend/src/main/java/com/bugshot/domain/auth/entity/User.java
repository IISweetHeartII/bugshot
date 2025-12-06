package com.bugshot.domain.auth.entity;

import com.bugshot.domain.common.BaseEntity;
import com.bugshot.domain.project.entity.Project;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 100)
    private String name;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    // OAuth
    @Column(name = "github_id", unique = true, length = 100)
    private String githubId;

    @Column(name = "google_id", unique = true, length = 100)
    private String googleId;

    // Plan
    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", length = 20)
    @Builder.Default
    private PlanType planType = PlanType.FREE;

    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Project> projects = new ArrayList<>();

    @PrePersist
    public void generateId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    // Business Methods
    public boolean canCreateProject() {
        return switch (planType) {
            case FREE -> projects.size() < 3;
            case PRO -> projects.size() < 10;
            case TEAM -> projects.size() < 50;
        };
    }

    public void upgradePlan(PlanType newPlan) {
        if (newPlan.ordinal() > this.planType.ordinal()) {
            this.planType = newPlan;
        }
    }

    public enum PlanType {
        FREE,
        PRO,
        TEAM
    }
}
