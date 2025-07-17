package com.example.userservice

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<UserServiceApplication>().with(TestcontainersConfiguration::class).run(*args)
}
