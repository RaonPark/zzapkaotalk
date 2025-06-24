package com.example.chatservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EnableJpaRepositories(basePackages = ["com.example.chatservice.repository"])
@EnableJpaAuditing
@SpringBootApplication
class ChatServiceApplication

fun main(args: Array<String>) {
	runApplication<ChatServiceApplication>(*args)
}
