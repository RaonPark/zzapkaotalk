package com.example.chatservice.config

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.TopicConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaTopicConfig {
    @Bean
    fun chatMessageBroadcastTopic(): NewTopic {
        return TopicBuilder.name("chat-message-broadcast")
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "producer")
            .partitions(10)
            .replicas(2)
            .build()
    }
}