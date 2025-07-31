package com.example.apigateway.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {
    companion object {
        val log = KotlinLogging.logger { }
    }

    @GetMapping("/loginPage")
    fun loginPage(@AuthenticationPrincipal oidcUser: OidcUser): String {
        return oidcUser.toString()
    }

    @GetMapping("/")
    fun defaultPage(@AuthenticationPrincipal user: OidcUser): String {
        return user.toString()
    }

    @GetMapping("/checkLogin")
    fun checkLogin(@AuthenticationPrincipal user: OidcUser?): ResponseEntity<String> =
        if(user == null) {
            log.info { "User not found" }
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized")
        } else {
            log.info { "User found = $user" }
            ResponseEntity.ok("Authenticated")
        }

}