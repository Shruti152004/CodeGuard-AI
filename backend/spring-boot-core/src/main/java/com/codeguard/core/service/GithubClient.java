package com.codeguard.core.service;

import com.codeguard.core.dto.GitHubBranchDto;
import com.codeguard.core.dto.GitHubFileDto;
import com.codeguard.core.dto.GitHubPullDto;
import com.codeguard.core.dto.GitHubRepoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class GithubClient {

    private static final Logger log = LoggerFactory.getLogger(GithubClient.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${github.token:}")
    private String githubToken;

    private static final String GITHUB_API_URL = "https://api.github.com";

    public List<GitHubRepoDto> getUserRepositories(String token) {
        String activeToken = getActiveToken(token);
        if (activeToken.isEmpty()) {
            log.info("No GitHub token provided. Falling back to mock repositories.");
            return getMockRepos();
        }

        try {
            HttpHeaders headers = buildHeaders(activeToken);
            ResponseEntity<GitHubRepoDto[]> response = restTemplate.exchange(
                    GITHUB_API_URL + "/user/repos",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    GitHubRepoDto[].class
            );
            logRateLimitDetails(response.getHeaders());
            return List.of(response.getBody() != null ? response.getBody() : new GitHubRepoDto[0]);
        } catch (Exception e) {
            log.error("Failed to fetch GitHub repositories, returning mock fallback. Error: {}", e.getMessage());
            return getMockRepos();
        }
    }

    public List<GitHubBranchDto> getBranches(String owner, String repo, String token) {
        String activeToken = getActiveToken(token);
        if (activeToken.isEmpty()) {
            return getMockBranches();
        }

        try {
            HttpHeaders headers = buildHeaders(activeToken);
            ResponseEntity<GitHubBranchDto[]> response = restTemplate.exchange(
                    GITHUB_API_URL + "/repos/" + owner + "/" + repo + "/branches",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    GitHubBranchDto[].class
            );
            logRateLimitDetails(response.getHeaders());
            return List.of(response.getBody() != null ? response.getBody() : new GitHubBranchDto[0]);
        } catch (Exception e) {
            log.error("Failed to fetch branches, returning mock fallback. Error: {}", e.getMessage());
            return getMockBranches();
        }
    }

    public List<GitHubPullDto> getPullRequests(String owner, String repo, String token) {
        String activeToken = getActiveToken(token);
        if (activeToken.isEmpty()) {
            return getMockPulls();
        }

        try {
            HttpHeaders headers = buildHeaders(activeToken);
            ResponseEntity<GitHubPullDto[]> response = restTemplate.exchange(
                    GITHUB_API_URL + "/repos/" + owner + "/" + repo + "/pulls",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    GitHubPullDto[].class
            );
            logRateLimitDetails(response.getHeaders());
            return List.of(response.getBody() != null ? response.getBody() : new GitHubPullDto[0]);
        } catch (Exception e) {
            log.error("Failed to fetch pull requests, returning mock fallback. Error: {}", e.getMessage());
            return getMockPulls();
        }
    }

    public List<GitHubFileDto> getPullRequestFiles(String owner, String repo, int pullNumber, String token) {
        String activeToken = getActiveToken(token);
        if (activeToken.isEmpty()) {
            return getMockFiles();
        }

        try {
            HttpHeaders headers = buildHeaders(activeToken);
            ResponseEntity<GitHubFileDto[]> response = restTemplate.exchange(
                    GITHUB_API_URL + "/repos/" + owner + "/" + repo + "/pulls/" + pullNumber + "/files",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    GitHubFileDto[].class
            );
            logRateLimitDetails(response.getHeaders());
            return List.of(response.getBody() != null ? response.getBody() : new GitHubFileDto[0]);
        } catch (Exception e) {
            log.error("Failed to fetch pull request files, returning mock fallback. Error: {}", e.getMessage());
            return getMockFiles();
        }
    }

    private String getActiveToken(String headerToken) {
        if (headerToken != null && !headerToken.trim().isEmpty()) {
            return headerToken;
        }
        return githubToken != null ? githubToken : "";
    }

    private HttpHeaders buildHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.valueOf("application/vnd.github+json")));
        headers.set("X-GitHub-Api-Version", "2022-11-28");
        return headers;
    }

    private void logRateLimitDetails(HttpHeaders headers) {
        String limit = headers.getFirst("X-RateLimit-Limit");
        String remaining = headers.getFirst("X-RateLimit-Remaining");
        String reset = headers.getFirst("X-RateLimit-Reset");
        log.info("GitHub API Rate Limits - Limit: {}, Remaining: {}, Reset: {}", limit, remaining, reset);
    }

    // --- MOCK FALLBACKS ---

    private List<GitHubRepoDto> getMockRepos() {
        List<GitHubRepoDto> repos = new ArrayList<>();
        GitHubRepoDto repo = new GitHubRepoDto();
        repo.setId(101L);
        repo.setName("codeguard-core-backend");
        repo.setFullName("mock-owner/codeguard-core-backend");
        repo.setDescription("CodeGuard AI Java Spring Boot core backend architecture codebase.");
        repo.setHtmlUrl("https://github.com/mock-owner/codeguard-core-backend");
        repo.setPrivate(true);
        repo.setDefaultBranch("main");
        repos.add(repo);
        return repos;
    }

    private List<GitHubBranchDto> getMockBranches() {
        List<GitHubBranchDto> branches = new ArrayList<>();
        GitHubBranchDto main = new GitHubBranchDto();
        main.setName("main");
        GitHubBranchDto dev = new GitHubBranchDto();
        dev.setName("develop");
        branches.add(main);
        branches.add(dev);
        return branches;
    }

    private List<GitHubPullDto> getMockPulls() {
        List<GitHubPullDto> pulls = new ArrayList<>();
        GitHubPullDto pr = new GitHubPullDto();
        pr.setId(501L);
        pr.setNumber(1);
        pr.setTitle("feat: implement security credentials validation filters");
        pr.setState("open");
        pr.setHtmlUrl("https://github.com/mock-owner/codeguard-core-backend/pull/1");
        pr.setCreatedAt("2026-08-09T12:00:00Z");
        pulls.add(pr);
        return pulls;
    }

    private List<GitHubFileDto> getMockFiles() {
        List<GitHubFileDto> files = new ArrayList<>();
        
        GitHubFileDto file1 = new GitHubFileDto();
        file1.setFilename("src/main/java/com/codeguard/core/security/JwtAuthenticationFilter.java");
        file1.setStatus("modified");
        file1.setAdditions(25);
        file1.setDeletions(5);
        file1.setChanges(30);
        file1.setPatch("@@ -10,6 +10,25 @@ ...");
        
        files.add(file1);
        return files;
    }
}
