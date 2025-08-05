package com.example.apigateway.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.messaging.rsocket.DefaultMetadataExtractor
import org.springframework.messaging.rsocket.MetadataExtractor
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity
import org.springframework.security.config.annotation.rsocket.RSocketSecurity
import org.springframework.security.messaging.handler.invocation.reactive.AuthenticationPrincipalArgumentResolver
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor
import org.springframework.web.util.pattern.PathPatternRouteMatcher

@Configuration
@EnableRSocketSecurity
@EnableReactiveMethodSecurity
class RSocketConfig {
    @Value("\${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private lateinit var issuerUri: String

    @Bean
    fun rsocketInterceptor(rsocket: RSocketSecurity): PayloadSocketAcceptorInterceptor {
        rsocket
            .authorizePayload { authorize ->
                authorize
                    .setup().authenticated()
                    .anyRequest().authenticated()
                    .anyExchange().permitAll()
            }
            .jwt {

            }

        return rsocket.build()
    }

    @Bean
    fun rsocketMessageHandler(): RSocketMessageHandler {
        val handler = RSocketMessageHandler()

        handler.rSocketStrategies = rsocketStrategies()
        handler.argumentResolverConfigurer.addCustomResolver(AuthenticationPrincipalArgumentResolver())

        return handler
    }

    @Bean
    fun rsocketStrategies(): RSocketStrategies {
        return RSocketStrategies.builder()
            .encoders {
                it.add(Jackson2JsonEncoder())
            }
            .decoders {
                it.add(Jackson2JsonDecoder())
            }
            .routeMatcher(PathPatternRouteMatcher())
            .build()
    }

    @Bean
    fun metadataExtractor(): MetadataExtractor {
        return DefaultMetadataExtractor().apply {

        }
    }

    @Bean
    fun jwtDecoder(): NimbusReactiveJwtDecoder {
        return NimbusReactiveJwtDecoder.withIssuerLocation(issuerUri)
            .jwsAlgorithm(SignatureAlgorithm.RS256)
            .build()
    }
}