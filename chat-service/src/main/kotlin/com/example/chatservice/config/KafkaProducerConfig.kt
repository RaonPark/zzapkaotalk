package com.example.chatservice.config

import com.chatservice.DirectChatMessageBroadcast
import com.chatservice.GroupChatMessageBroadcast
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.LongSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.DefaultTransactionIdSuffixStrategy
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
class KafkaProducerConfig {
    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.properties.schema.registry.url}")
    private lateinit var schemaRegistryUrl: String

    @Bean
    fun groupChatMessageBroadcastProducerFactory(): ProducerFactory<Long, GroupChatMessageBroadcast> {
        val config = mutableMapOf<String, Any>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.RETRIES_CONFIG to 10,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to LongSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to KafkaAvroSerializer::class.java,
            ProducerConfig.TRANSACTIONAL_ID_CONFIG to "chat-message-broadcast",
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to "true",
            ProducerConfig.BATCH_SIZE_CONFIG to 40 * 1024,
            ProducerConfig.TRANSACTION_TIMEOUT_CONFIG to 50000,
            ProducerConfig.COMPRESSION_TYPE_CONFIG to "snappy",
            KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl,
        )

        val producerFactory = DefaultKafkaProducerFactory<Long, GroupChatMessageBroadcast>(config)
        producerFactory.setTransactionIdSuffixStrategy(DefaultTransactionIdSuffixStrategy(5))

        return producerFactory
    }

    @Bean
    fun groupChatMessageBroadcastKafkaTemplate(): KafkaTemplate<Long, GroupChatMessageBroadcast> {
        return KafkaTemplate(groupChatMessageBroadcastProducerFactory())
    }

    @Bean
    fun directChatMessageBroadcastProducerFactory(): ProducerFactory<Long, DirectChatMessageBroadcast> {
        val config = mutableMapOf<String, Any>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.RETRIES_CONFIG to 10,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to LongSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to KafkaAvroSerializer::class.java,
            ProducerConfig.TRANSACTIONAL_ID_CONFIG to "chat-message-broadcast",
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to "true",
            ProducerConfig.BATCH_SIZE_CONFIG to 40 * 1024,
            ProducerConfig.TRANSACTION_TIMEOUT_CONFIG to 50000,
            ProducerConfig.COMPRESSION_TYPE_CONFIG to "snappy",
            KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl,
        )

        val producerFactory = DefaultKafkaProducerFactory<Long, DirectChatMessageBroadcast>(config)
        producerFactory.setTransactionIdSuffixStrategy(DefaultTransactionIdSuffixStrategy(5))

        return producerFactory
    }

    @Bean
    fun directChatMessageBroadcastKafkaTemplate(): KafkaTemplate<Long, DirectChatMessageBroadcast> {
        return KafkaTemplate(directChatMessageBroadcastProducerFactory())
    }
}