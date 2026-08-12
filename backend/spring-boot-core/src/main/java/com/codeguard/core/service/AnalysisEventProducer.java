package com.codeguard.core.service;

import com.codeguard.core.config.KafkaConfig;
import com.codeguard.core.dto.AnalysisResultEventDto;
import com.codeguard.core.model.Analysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalysisEventProducer {

    private static final Logger log = LoggerFactory.getLogger(AnalysisEventProducer.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publishAnalysisResult(Analysis analysis) {
        AnalysisResultEventDto event = AnalysisResultEventDto.builder()
                .analysisId(analysis.getId())
                .repositoryName(analysis.getRepositoryName())
                .branch(analysis.getBranch())
                .status(analysis.getStatus())
                .overallScore(analysis.getOverallScore())
                .technicalDebtHours(analysis.getTechnicalDebtHours())
                .build();

        log.info("Publishing analysis result event for repository: {}, run: {}", analysis.getRepositoryName(), analysis.getId());
        kafkaTemplate.send(KafkaConfig.ANALYSIS_RESULTS_TOPIC, analysis.getRepositoryName(), event);
    }
}
