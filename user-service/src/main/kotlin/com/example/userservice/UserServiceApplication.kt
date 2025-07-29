package com.example.userservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication(exclude = [RedisRepositoriesAutoConfiguration::class])
@EnableR2dbcRepositories(basePackages = ["com.example.userservice.repository"])
@EnableR2dbcAuditing
@EnableDiscoveryClient
class UserServiceApplication

fun main(args: Array<String>) {
	runApplication<UserServiceApplication>(*args)
}
