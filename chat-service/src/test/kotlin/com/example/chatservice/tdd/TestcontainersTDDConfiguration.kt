package com.example.chatservice.tdd

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
@Testcontainers
class TestcontainersTDDConfiguration {
    @Bean
    @ServiceConnection
    fun mariaDbContainer(): MariaDBContainer<*> {
        return MariaDBContainer(DockerImageName.parse("mariadb:latest"))
    }
}