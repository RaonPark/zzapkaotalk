package com.example.userservice.service

import com.example.userservice.dto.LoginRequest
import com.example.userservice.dto.LoginResponse
import com.example.userservice.dto.RegisterRequest
import com.example.userservice.dto.RegisterResponse
import com.example.userservice.entity.User
import com.example.userservice.entity.keycloak.KeycloakCredential
import com.example.userservice.entity.keycloak.KeycloakUser
import com.example.userservice.repository.UserRepository
import com.example.userservice.repository.VerificationCodeRepository
import com.example.userservice.support.KeycloakException
import com.example.userservice.support.MachineIdGenerator
import com.example.userservice.support.SnowflakeIdGenerator
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.ResponseCookie
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.server.ServerWebExchange
import java.time.Duration
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userRedisOperations: ReactiveRedisOperations<String, User>,
    private val webClient: WebClient,
    private val mailService: MailService,
    private val verificationCodeRepository: VerificationCodeRepository,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) {
    companion object {
        val snowflakeIdGenerator = SnowflakeIdGenerator(MachineIdGenerator.machineId())
        val log = LoggerFactory.getLogger(UserService::class.java)
    }

    suspend fun doLogin(loginRequest: LoginRequest, serverWebExchange: ServerWebExchange): LoginResponse {
        val user = userRepository.findByEmail(loginRequest.email)

        var success = false
        if(passwordEncoder.matches(loginRequest.password, user.password)) {
            success = true
        }

        userRedisOperations.opsForValue().set("user:${user.id}", user)
            .awaitSingle()

        if(success) {

        }

        return LoginResponse(
            success = success,
            timestamp = LocalDateTime.now().toString(),
        )
    }

    fun doLogout(httpServerWebExchange: ServerWebExchange) {
    }

    suspend fun doLoginWithKeycloak(oAuth2User: OAuth2User): LoginResponse {
        val user = userRepository.findByEmail(oAuth2User.getAttribute<String>("email")
            ?: throw KeycloakException("Email Not Found"))

        userRedisOperations.opsForValue().set("user:${user.id}", user).awaitSingle()

        return LoginResponse(
            success = true,
            timestamp = LocalDateTime.now().toString(),
        )
    }

    suspend fun getToken(): String {
        val map = LinkedMultiValueMap<String, String>()
        map["grant_type"] = "client_credentials"
        map["client_id"] = "oauth2-client"
        map["client_secret"] = "H85Of0PKMGApj1b8DALnmCXaVGqTizGE"

        val token = webClient.post()
            .uri("http://localhost:8090/realms/zzapkaotalk/protocol/openid-connect/token")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(BodyInserters.fromValue(map))
            .retrieve()
            .bodyToMono(KeyCloakToken::class.java)
            .awaitSingle()
        return token.accessToken
    }

    class KeyCloakToken(@JsonProperty("access_token") val accessToken: String) {}

    suspend fun doRegister(registerRequest: RegisterRequest): RegisterResponse {
        var success = false

        val createUserStatusCode = createUserInKeycloak(registerRequest)

        if(createUserStatusCode == HttpStatus.CREATED) {
            log.info("User ${registerRequest.email} registered successfully")
            success = true
        }

        r2dbcEntityTemplate.insert(User(
            id = snowflakeIdGenerator.nextId(),
            email = registerRequest.email,
            password = passwordEncoder.encode(registerRequest.password),
            nickname = registerRequest.nickname,
            profileImage = registerRequest.profileImage,
            isVerified = false
        )).awaitSingle()

        return RegisterResponse(
            success = success,
            timestamp = LocalDateTime.now().toString(),
        )
    }

    private suspend fun createUserInKeycloak(registerRequest: RegisterRequest): HttpStatusCode {
        val accessToken = getToken()

        val credentials = KeycloakCredential(
            type = "password",
            value = registerRequest.password,
            temporary = false,
        )

        val userRepresentation = KeycloakUser(
            email = registerRequest.email,
            username = registerRequest.nickname,
            enabled = true,
            credentials = listOf(credentials)
        )

        return webClient.post()
            .uri("http://localhost:8090/admin/realms/zzapkaotalk/users")
            .header("Authorization", "Bearer $accessToken")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(userRepresentation))
            .retrieve()
            .awaitBodilessEntity()
            .statusCode
    }

    suspend fun getUserInfo(serverWebExchange: ServerWebExchange): String {
        val accessToken = serverWebExchange.request.cookies["access_token"] ?: throw KeycloakException("Access token Not Found")

        log.info("access token = {}", accessToken.firstOrNull()?.value)

        val userInfo = webClient.get()
            .uri("http://localhost:8090/realms/zzapkaotalk/protocol/openid-connect/userinfo")
            .header("Authorization", "Bearer ${accessToken.first().value}")
            .retrieve()
            .awaitBody<String>()

        return userInfo
    }
}