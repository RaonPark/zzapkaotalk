package com.example.userservice.controller

import com.example.userservice.dto.LoginRequest
import com.example.userservice.dto.LoginResponse
import com.example.userservice.dto.RegisterRequest
import com.example.userservice.dto.RegisterResponse
import com.example.userservice.service.UserService
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.net.URI

@RestController
class UserController(
    private val userService: UserService
) {
    companion object {
        @JvmStatic
        private val log = LoggerFactory.getLogger(UserController::class.java)
    }

    @GetMapping("/auth/logout")
    suspend fun logout(httpServerWebExchange: ServerWebExchange) {
        // TODO(토큰을 삭제하기도 해야함)
        return userService.doLogout(httpServerWebExchange)
    }

    @PostMapping("/register")
    suspend fun register(@RequestBody registerRequest: RegisterRequest): RegisterResponse {
        return userService.doRegister(registerRequest)
    }

    @GetMapping("/getUserInfo")
    suspend fun getUserInfo(@AuthenticationPrincipal jwt: Jwt): String {
        return userService.getUserInfo(jwt)
    }

    @PostMapping("/requestEmailVerification")
    suspend fun requestEmailVerification(@AuthenticationPrincipal oAuth2User: OAuth2User): String {
        return "hello"
    }

    @GetMapping("/isTokenRelay")
    suspend fun isTokenRelay(@AuthenticationPrincipal jwt: Jwt, serverWebExchange: ServerWebExchange): String {
        log.info("There is token! = ${jwt.tokenValue}")
        log.info("From Header Authorization = ${serverWebExchange.request.headers["Authorization"]}")

        return "isTokenRelay = ${jwt.issuer}"
    }
}