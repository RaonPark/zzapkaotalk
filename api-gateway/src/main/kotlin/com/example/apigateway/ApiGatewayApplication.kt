package com.example.apigateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@EnableR2dbcRepositories(basePackages = ["com.example.apigateway.repository"])
@SpringBootApplication(
	exclude = [RedisRepositoriesAutoConfiguration::class]
)
@EnableDiscoveryClient
class ApiGatewayApplication

fun main(args: Array<String>) {
	runApplication<ApiGatewayApplication>(*args)
}
