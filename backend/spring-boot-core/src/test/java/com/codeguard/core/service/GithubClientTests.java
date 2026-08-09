package com.codeguard.core.service;

import com.codeguard.core.dto.GitHubRepoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GithubClientTests {

    private GithubClient githubClient;

    @BeforeEach
    void setUp() {
        githubClient = new GithubClient();
    }

    @Test
    void testGetRepositoriesMockFallback() {
        List<GitHubRepoDto> repos = githubClient.getUserRepositories(null);
        assertNotNull(repos);
        assertFalse(repos.isEmpty());
        assertEquals("codeguard-core-backend", repos.get(0).getName());
    }
}
