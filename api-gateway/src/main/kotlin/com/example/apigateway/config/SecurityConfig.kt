package com.example.apigateway.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler
import org.springframework.web.cors.reactive.CorsWebFilter
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val reactiveClientRegistrationRepository: ReactiveClientRegistrationRepository,
    private val oAuth2AuthenticationSuccessHandler: OAuth2AuthenticationSuccessHandler,
    private val corsWebFilter: CorsWebFilter
) {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http {
            csrf { disable() }
            cors { corsWebFilter }
            authorizeExchange {
                authorize("/login", permitAll)
                authorize("/logout", permitAll)
                authorize(anyExchange, authenticated)
            }
            oauth2Login {
                authenticationSuccessHandler = oAuth2AuthenticationSuccessHandler
            }
            oauth2Client {

            }
            logout {
                logoutSuccessHandler = oAuth2LogoutSuccessHandler()
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
    fun oAuth2LogoutSuccessHandler(): ServerLogoutSuccessHandler {
        val handler = OidcClientInitiatedServerLogoutSuccessHandler(reactiveClientRegistrationRepository)

        println("logout successful")

        handler.setPostLogoutRedirectUri("{baseUrl}/login")

        return handler
    }
}