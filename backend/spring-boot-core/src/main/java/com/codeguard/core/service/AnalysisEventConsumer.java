package com.codeguard.core.service;

import com.codeguard.core.config.KafkaConfig;
import com.codeguard.core.dto.CodeAnalysisEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AnalysisEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AnalysisEventConsumer.class);

    @Autowired
    private AnalysisService analysisService;

    @KafkaListener(topics = KafkaConfig.CODE_ANALYSIS_TOPIC, groupId = "codeguard-group")
    public void consumeCodeAnalysisEvent(CodeAnalysisEventDto event) {
        log.info("Received code analysis trigger event from Kafka broker for repository: {}, branch: {}", 
                event.getRepositoryName(), event.getBranch());
        
        try {
            analysisService.startAnalysis(event.getRepositoryName(), event.getBranch(), event.getGitHubToken());
            log.info("Successfully completed asynchronous event analysis run for: {}", event.getRepositoryName());
        } catch (Exception e) {
            log.error("Failed to execute asynchronous analysis for: {}, error: {}", event.getRepositoryName(), e.getMessage());
            throw e; // Throw to trigger DLQ recovery retry loop
        }
    }
}
