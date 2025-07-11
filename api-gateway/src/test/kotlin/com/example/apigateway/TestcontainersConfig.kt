package com.example.apigateway

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
@Testcontainers
class TestcontainersConfig {
    @Bean
    @ServiceConnection(name = "redis")
    fun redisContainer(): GenericContainer<*> {
        return GenericContainer(DockerImageName.parse("redis:latest")).withExposedPorts(6379)
    }
}