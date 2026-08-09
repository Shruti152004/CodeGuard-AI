package com.codeguard.core.controller;

import com.codeguard.core.dto.GitHubBranchDto;
import com.codeguard.core.dto.GitHubFileDto;
import com.codeguard.core.dto.GitHubPullDto;
import com.codeguard.core.dto.GitHubRepoDto;
import com.codeguard.core.service.GithubClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/github")
@CrossOrigin(origins = "*")
public class GitHubController {

    @Autowired
    private GithubClient githubClient;

    @GetMapping("/repos")
    public ResponseEntity<List<GitHubRepoDto>> getRepositories(
            @RequestHeader(value = "X-GitHub-Token", required = false) String gitHubToken) {
        List<GitHubRepoDto> repos = githubClient.getUserRepositories(gitHubToken);
        return ResponseEntity.ok(repos);
    }

    @GetMapping("/repos/{owner}/{repo}/branches")
    public ResponseEntity<List<GitHubBranchDto>> getBranches(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestHeader(value = "X-GitHub-Token", required = false) String gitHubToken) {
        List<GitHubBranchDto> branches = githubClient.getBranches(owner, repo, gitHubToken);
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/repos/{owner}/{repo}/pulls")
    public ResponseEntity<List<GitHubPullDto>> getPullRequests(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestHeader(value = "X-GitHub-Token", required = false) String gitHubToken) {
        List<GitHubPullDto> pulls = githubClient.getPullRequests(owner, repo, gitHubToken);
        return ResponseEntity.ok(pulls);
    }

    @GetMapping("/repos/{owner}/{repo}/pulls/{number}/files")
    public ResponseEntity<List<GitHubFileDto>> getPullRequestFiles(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable int number,
            @RequestHeader(value = "X-GitHub-Token", required = false) String gitHubToken) {
        List<GitHubFileDto> files = githubClient.getPullRequestFiles(owner, repo, number, gitHubToken);
        return ResponseEntity.ok(files);
    }
}
