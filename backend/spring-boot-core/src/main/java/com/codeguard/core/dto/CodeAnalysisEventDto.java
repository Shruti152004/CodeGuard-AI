package com.codeguard.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeAnalysisEventDto {
    private String repositoryName;
    private String branch;
    private String gitHubToken;
}
