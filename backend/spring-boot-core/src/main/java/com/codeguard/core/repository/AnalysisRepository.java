package com.codeguard.core.repository;

import com.codeguard.core.model.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    List<Analysis> findByRepositoryNameOrderByCreatedAtDesc(String repositoryName);
}
