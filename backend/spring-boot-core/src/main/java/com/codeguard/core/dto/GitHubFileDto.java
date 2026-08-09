package com.codeguard.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GitHubFileDto {
    private String filename;
    private String status;
    private int additions;
    private int deletions;
    private int changes;
    @JsonProperty("raw_url")
    private String rawUrl;
    private String patch;
}
