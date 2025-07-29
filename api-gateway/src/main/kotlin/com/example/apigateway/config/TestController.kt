package com.example.apigateway.config

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {
    @GetMapping("/loginPage")
    fun loginPage(@AuthenticationPrincipal oidcUser: OidcUser): String {
        return oidcUser.toString()
    }

    @GetMapping("/")
    fun defaultPage(@AuthenticationPrincipal user: OidcUser): String {
        return user.toString()
    }
}