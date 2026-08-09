package com.codeguard.core.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "issues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id")
    private Analysis analysis;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 50)
    private String category; // BUG, SECURITY, PERFORMANCE, CODE_SMELL, MAINTAINABILITY, DUPLICATION, STYLE, ARCHITECTURE

    @Column(nullable = false, length = 50)
    private String severity; // CRITICAL, HIGH, MEDIUM, LOW, INFO

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String impact;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    @Column(name = "suggested_fix", columnDefinition = "TEXT")
    private String suggestedFix;

    @Column(nullable = false, length = 50)
    private String source; // STATIC_ANALYSIS, AI_ANALYSIS, LANGUAGE_ANALYZER
}
