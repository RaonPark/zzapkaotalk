package com.example.userservice.config

import com.example.userservice.support.OAuth2AuthenticationSuccessHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(

) {
    @Bean
    fun securityWebFilterChain(
        http: ServerHttpSecurity,
        oAuth2AuthenticationSuccessHandler: OAuth2AuthenticationSuccessHandler,
    ): SecurityWebFilterChain {
        return http {
            cors { disable() }
            csrf { disable() }
            authorizeExchange {
                authorize("/login", permitAll)
                authorize("/register", permitAll)
                authorize("/callback", permitAll)
                authorize("/logout", permitAll)
                authorize(anyExchange, authenticated)
            }
            oauth2Login {
                authenticationSuccessHandler = oAuth2AuthenticationSuccessHandler
            }
            oauth2Client {

            }
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}