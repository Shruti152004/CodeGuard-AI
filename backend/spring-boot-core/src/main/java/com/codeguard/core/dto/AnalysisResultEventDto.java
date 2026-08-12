package com.codeguard.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResultEventDto {
    private Long analysisId;
    private String repositoryName;
    private String branch;
    private String status;
    private Integer overallScore;
    private Double technicalDebtHours;
}
