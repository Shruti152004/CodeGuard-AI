package com.codeguard.core.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnalysisRequest {

    @NotBlank
    private String repositoryName;

    @NotBlank
    private String branch;
}
