package com.example.chatservice

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.springframework.test.context.DynamicPropertyRegistrar
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
		val testcontainersNetwork = Network.newNetwork()
	}

	@Bean
	@ServiceConnection
	fun kafkaContainer(): ConfluentKafkaContainer {
		return ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
			.apply {
				withListener("kafka1:19092")
				withNetwork(testcontainersNetwork)
			}
	}

	@Bean
	@ServiceConnection
	fun mariaDbContainer(): MariaDBContainer<*> {
		return MariaDBContainer(DockerImageName.parse("mariadb:10.3.39"))
			.apply {
				withDatabaseName("mariadb")
				withExposedPorts(3306)
				withUsername("root")
				withPassword("1234")
			}
	}

	@Bean
	@ServiceConnection(name = "redis")
	fun redisContainer(): GenericContainer<*> {
		return GenericContainer(DockerImageName.parse("redis:latest")).withExposedPorts(6379)
	}

	@Bean
	@DependsOn("kafkaContainer")
	fun schemaRegistryContainer(kafkaContainer: ConfluentKafkaContainer): GenericContainer<*> {
		val schemaRegistryContainer = GenericContainer<Nothing>("confluentinc/cp-schema-registry:7.5.0")
			.apply {
				withExposedPorts(8085)
				withNetwork(testcontainersNetwork)
				withNetworkAliases("schema-registry")
				withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
				withEnv("SCHEMA_REGISTRY_CUB_KAFKA_MIN_BROKERS", "1")
				withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka1:19092")
				withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8085")
				waitingFor(Wait.forHttp("/subjects").forStatusCode(200))
			}

		return schemaRegistryContainer
	}

	@Bean
	fun setTestProperties(kafkaContainer: ConfluentKafkaContainer, schemaRegistryContainer: GenericContainer<*>, mariaDBContainer: MariaDBContainer<*>): DynamicPropertyRegistrar {
		return DynamicPropertyRegistrar { registry ->
			registry.add("spring.kafka.bootstrap-servers") { "localhost:${kafkaContainer.firstMappedPort}" }
			registry.add("spring.kafka.properties.schema.registry.url") { "http://localhost:${schemaRegistryContainer.firstMappedPort}" }

			registry.add("spring.datasource.username") { "root" }
			registry.add("spring.datasource.password") { "1234" }
			registry.add("spring.datasource.database") { mariaDBContainer.databaseName }
			registry.add("spring.datasource.host") { mariaDBContainer.host }
			registry.add("spring.datasource.port") { mariaDBContainer.firstMappedPort }

			registry.add("spring.kafka.properties.schema.registry.url") { "http://${schemaRegistryContainer.host}:${schemaRegistryContainer.firstMappedPort}" }
		}
	}
}
