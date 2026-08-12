package com.codeguard.core.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    public static final String CODE_ANALYSIS_TOPIC = "code-analysis-events";
    public static final String ANALYSIS_RESULTS_TOPIC = "analysis-results-events";

    @Bean
    public NewTopic codeAnalysisEventsTopic() {
        return TopicBuilder.name(CODE_ANALYSIS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic analysisResultsEventsTopic() {
        return TopicBuilder.name(ANALYSIS_RESULTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
        // Send to DLT after 3 failed attempts, waiting 2 seconds between retries
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);
        return new DefaultErrorHandler(recoverer, new FixedBackOff(2000L, 2));
    }
}
