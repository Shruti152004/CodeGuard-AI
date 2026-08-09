package com.codeguard.core.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "analyses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repository_name", nullable = false, length = 150)
    private String repositoryName;

    @Column(nullable = false, length = 100)
    private String branch;

    @Column(nullable = false, length = 50)
    private String status; // PENDING, RUNNING, COMPLETED, FAILED

    @Column(name = "overall_score")
    private Integer overallScore;

    @Column(name = "security_score")
    private Integer securityScore;

    @Column(name = "reliability_score")
    private Integer reliabilityScore;

    @Column(name = "maintainability_score")
    private Integer maintainabilityScore;

    @Column(name = "performance_score")
    private Integer performanceScore;

    @Column(name = "code_quality_score")
    private Integer codeQualityScore;

    @Column(name = "technical_debt_hours")
    private Double technicalDebtHours;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
