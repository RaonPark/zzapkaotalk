package com.example.apigateway.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {
    @Value("\${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private val oauth2ResourceServerJwk: String? = null

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http {
            csrf { disable() }
            cors { disable() }
            authorizeExchange {
                authorize(antPattern = "/login", permitAll)
                authorize(anyExchange, authenticated)
            }
            sessionManagement {
                SessionCreationPolicy.STATELESS
            }
            oauth2ResourceServer {
                jwt {
                    jwtDecoder = jwtDecoder()
                }
            }
        }
    }

    @Bean
    fun userKeyResolver(): KeyResolver {
        return KeyResolver { exchange ->
            Mono.just(exchange.request.remoteAddress!!.address.hostAddress)
        }
    }

    @Bean
    fun jwtDecoder(): NimbusReactiveJwtDecoder {
        return NimbusReactiveJwtDecoder
            .withJwkSetUri(oauth2ResourceServerJwk)
            .jwsAlgorithm(SignatureAlgorithm.RS256)
            .build()
    }
}