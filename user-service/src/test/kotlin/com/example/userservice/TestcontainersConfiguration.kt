package com.example.userservice

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.test.context.DynamicPropertyRegistrar
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	fun mariaDbContainer(): MariaDBContainer<*> {
		return MariaDBContainer(DockerImageName.parse("mariadb:latest"))
			.apply {
				withDatabaseName("zzapkaotalk")
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
	fun setProperties(mariaDB: MariaDBContainer<*>): DynamicPropertyRegistrar {
		return DynamicPropertyRegistrar { registry ->
			registry.add("spring.datasource.username") { "root" }
			registry.add("spring.datasource.password") { "1234" }
			registry.add("spring.datasource.database") { mariaDB.databaseName }
			registry.add("spring.datasource.host") { mariaDB.host }
			registry.add("spring.datasource.port") { mariaDB.firstMappedPort }
		}
	}
}
