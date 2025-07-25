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

    @PostMapping("/loginRequest")
    suspend fun login(@RequestBody loginRequest: LoginRequest, serverWebExchange: ServerWebExchange): LoginResponse {
        val response = userService.doLogin(loginRequest, serverWebExchange)


        return response
    }

//    @GetMapping("/login")
//    fun login(serverHttpResponse: ServerHttpResponse): Mono<Void> {
//        serverHttpResponse.statusCode = HttpStatus.PERMANENT_REDIRECT
//        serverHttpResponse.headers.location = URI.create(
//            "http://localhost:8090/realms/zzapkaotalk/protocol/openid-connect/auth?client_id=oauth2-client&response_type=code&redirect_uri=http://localhost:8083/callback&scope=openid"
//        )
//        return serverHttpResponse.setComplete()
//    }

    @GetMapping("/auth/logout")
    suspend fun logout(httpServerWebExchange: ServerWebExchange) {
        // TODO(토큰을 삭제하기도 해야함)
        return userService.doLogout(httpServerWebExchange)
    }

    @PostMapping("/register")
    suspend fun register(@RequestBody registerRequest: RegisterRequest): RegisterResponse {
        return userService.doRegister(registerRequest)
    }

//    @GetMapping("/verifyEmail")
//    suspend fun verifyEmail(@RequestParam("email") email: String,
//                            @RequestParam("verificationCode") verificationCode: String): VerifyEmailResponse {
//        return userService.verifyEmail(email, verificationCode)
//    }

    @GetMapping("/callback")
    fun keycloakLoginCallback(@AuthenticationPrincipal oAuth2User: OAuth2User?): String {
        log.info("User ${oAuth2User?.getAttribute<String>("email")}")

        return "Hello! ${oAuth2User?.attributes}"
    }

    @GetMapping("/loginPage")
    suspend fun loginPage(@AuthenticationPrincipal oAuth2User: OAuth2User): String {
        log.info("Here is User Info : ${oAuth2User.attributes} and authorities : ${oAuth2User.authorities}")

        userService.doLoginWithKeycloak(oAuth2User)

        return "loginPage"
    }

    @GetMapping("/getUserInfo")
    suspend fun getUserInfo(serverWebExchange: ServerWebExchange): String {
        return userService.getUserInfo(serverWebExchange)
    }
}