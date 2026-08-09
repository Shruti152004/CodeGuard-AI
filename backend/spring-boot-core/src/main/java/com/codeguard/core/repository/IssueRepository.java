package com.codeguard.core.repository;

import com.codeguard.core.model.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {
    List<Issue> findByAnalysisId(Long analysisId);
    List<Issue> findByAnalysisIdAndCategory(Long analysisId, String category);
}
