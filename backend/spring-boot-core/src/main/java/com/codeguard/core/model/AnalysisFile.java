package com.codeguard.core.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "analysis_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id")
    private Analysis analysis;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(length = 50)
    private String language;
}
