package com.example.userservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(

) {
    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private var issuerUri: String = ""

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http {
            cors { disable() }
            csrf { disable() }
            authorizeExchange {
                authorize("/register", permitAll)
                authorize(anyExchange, authenticated)
            }
            sessionManagement {
                SessionCreationPolicy.STATELESS
            }
            oauth2ResourceServer {
                jwt {
                    jwtDecoder = jwtDecoder()
                    jwkSetUri = "$issuerUri/protocol/openid-connect/certs"
                }
            }
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun jwtDecoder(): NimbusReactiveJwtDecoder {
        return NimbusReactiveJwtDecoder.withIssuerLocation(issuerUri)
            .jwsAlgorithm(SignatureAlgorithm.RS256)
            .build()
    }
}