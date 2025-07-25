package com.example.chatservice.config

import io.r2dbc.spi.ConnectionFactory
import org.mariadb.r2dbc.MariadbConnectionConfiguration
import org.mariadb.r2dbc.MariadbConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate

@Configuration
class DBConnectionConfig: AbstractR2dbcConfiguration() {
    @Value("\${spring.datasource.username}")
    private lateinit var databaseUsername: String
    @Value("\${spring.datasource.password}")
    private lateinit var databasePassword: String
    @Value("\${spring.datasource.database}")
    private lateinit var databaseName: String
    @Value("\${spring.datasource.host}")
    private lateinit var databaseHost: String
    @Value("\${spring.datasource.port}")
    private var databasePort: Int = 3306

    @Bean
    @Order(1)
    override fun connectionFactory(): ConnectionFactory {
        val conf = MariadbConnectionConfiguration.builder()
            .host(databaseHost)
            .port(databasePort)
            .username(databaseUsername)
            .password(databasePassword)
            .database(databaseName)
            .build()
        return MariadbConnectionFactory(conf)
    }

    @Bean
    fun r2dbcTemplate(connectionFactory: ConnectionFactory): R2dbcEntityTemplate {
        return R2dbcEntityTemplate(connectionFactory)
    }
}