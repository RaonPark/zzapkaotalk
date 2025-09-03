package com.example.apigateway.config

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository
import org.springframework.web.cors.reactive.CorsWebFilter
import reactor.core.publisher.Mono
import java.time.Duration

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
                authorize("/checkLogin", permitAll)
                authorize("/login", permitAll)
                authorize("/logout", permitAll)
                authorize(anyExchange, authenticated)
            }
            securityContextRepository = WebSessionServerSecurityContextRepository()
            oauth2Login {
                authenticationSuccessHandler = oAuth2AuthenticationSuccessHandler
            }
            oauth2Client {

            }
//            exceptionHandling {
//                authenticationEntryPoint = RedirectServerAuthenticationEntryPoint("/login")
//            }
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

    @Bean
    fun oAuth2AuthorizedClientManager(
        reactiveClientRegistrationRepository: ReactiveClientRegistrationRepository,
        oAuth2AuthorizedClientRepository: ServerOAuth2AuthorizedClientRepository
    ): DefaultReactiveOAuth2AuthorizedClientManager {
        val provider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
            .authorizationCode()
            .clientCredentials()
            .refreshToken {
                refreshTokenGrantBuilder -> refreshTokenGrantBuilder.clockSkew(Duration.ofHours(3))
            }
            .build()

        val manager = DefaultReactiveOAuth2AuthorizedClientManager(
            reactiveClientRegistrationRepository, oAuth2AuthorizedClientRepository
        )
        manager.setAuthorizedClientProvider(provider)

        return manager
    }
}