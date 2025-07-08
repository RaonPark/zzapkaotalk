package com.example.chatservice.config

import com.chatservice.DirectChatMessageBroadcast
import com.chatservice.GroupChatMessageBroadcast
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.LongDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties

@Configuration
class KafkaConsumerConfig {
    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.properties.schema.registry.url}")
    private lateinit var schemaRegistryUrl: String

    @Bean
    fun groupChatMessageBroadcastConsumerFactory(): ConsumerFactory<Long, GroupChatMessageBroadcast> {
        val configMap = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to "group-chat-message-broadcast",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to LongDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to KafkaAvroDeserializer::class.java,
            ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG to "false",
            ConsumerConfig.ISOLATION_LEVEL_CONFIG to "read_committed",
            KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl,
            KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to true,
        )

        return DefaultKafkaConsumerFactory(configMap)
    }

    @Bean
    fun groupChatMessageBroadcastKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<Long, GroupChatMessageBroadcast> {
        val factory = ConcurrentKafkaListenerContainerFactory<Long, GroupChatMessageBroadcast>()
        factory.consumerFactory = groupChatMessageBroadcastConsumerFactory()
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        factory.containerProperties.eosMode = ContainerProperties.EOSMode.V2
        return factory
    }

    @Bean
    fun directChatMessageBroadcastConsumerFactory(): ConsumerFactory<Long, DirectChatMessageBroadcast> {
        val configMap = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to "direct-chat-message-broadcast",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to LongDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to KafkaAvroDeserializer::class.java,
            ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG to "false",
            ConsumerConfig.ISOLATION_LEVEL_CONFIG to "read_committed",
            KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl,
            KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to true,
        )

        return DefaultKafkaConsumerFactory(configMap)
    }

    @Bean
    fun directChatMessageBroadcastKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<Long, DirectChatMessageBroadcast> {
        val factory = ConcurrentKafkaListenerContainerFactory<Long, DirectChatMessageBroadcast>()
        factory.consumerFactory = directChatMessageBroadcastConsumerFactory()
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        factory.containerProperties.eosMode = ContainerProperties.EOSMode.V2
        return factory
    }
}