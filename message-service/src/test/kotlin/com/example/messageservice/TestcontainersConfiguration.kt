package com.example.messageservice

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
    companion object {
        val messageServiceNetwork = Network.newNetwork()
    }

    @Bean
    @ServiceConnection
    fun kafkaContainer(): ConfluentKafkaContainer {
        return ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0")).apply {
            withReuse(false)
            withListener("kafka1:19092")
            withNetwork(messageServiceNetwork)
        }
    }

    @Bean
    @DependsOn("kafkaContainer")
    fun schemaRegistryContainer(kafkaContainer: ConfluentKafkaContainer): GenericContainer<Nothing> {
        val schemaRegistryContainer = GenericContainer<Nothing>("confluentinc/cp-schema-registry:7.5.0")
            .apply {
                withExposedPorts(8085)
                withNetwork(messageServiceNetwork)
                withNetworkAliases("schema-registry")
                withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
                withEnv("SCHEMA_REGISTRY_CUB_KAFKA_MIN_BROKERS", "1")
                withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:19092")
                withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8085")
                waitingFor(Wait.forHttp("/subjects").forStatusCode(200))
            }

        return schemaRegistryContainer
    }

    @Bean
    @ServiceConnection
    fun mariaDbContainer(): MariaDBContainer<*> {
        return MariaDBContainer(DockerImageName.parse("mariadb:12.0.1"))
            .apply {
                withReuse(false)
                withNetwork(messageServiceNetwork)
                withExposedPorts(3306)
            }
    }

    @Bean
    @ServiceConnection(name = "redis")
    fun redisContainer(): GenericContainer<*> {
        return GenericContainer(DockerImageName.parse("redis:8.0"))
            .withExposedPorts(6379)
    }

}
