package com.codeguard.core.repository;

import com.codeguard.core.model.AnalysisFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnalysisFileRepository extends JpaRepository<AnalysisFile, Long> {
    List<AnalysisFile> findByAnalysisId(Long analysisId);
}
