package com.example.apigateway.config

import io.rsocket.metadata.WellKnownMimeType
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.messaging.rsocket.*
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity
import org.springframework.security.config.annotation.rsocket.RSocketSecurity
import org.springframework.security.messaging.handler.invocation.reactive.AuthenticationPrincipalArgumentResolver
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor
import org.springframework.security.rsocket.metadata.BearerTokenAuthenticationEncoder
import org.springframework.util.MimeTypeUtils
import org.springframework.web.util.pattern.PathPatternRouteMatcher
import reactor.util.retry.Retry
import java.net.URI
import java.time.Duration

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
                    .route("chat.direct").authenticated()
                    .anyRequest().authenticated()
                    .anyExchange().permitAll()
            }
            .jwt(Customizer.withDefaults())

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
                it.add(BearerTokenAuthenticationEncoder())
            }
            .decoders {
                it.add(Jackson2JsonDecoder())
            }
            .routeMatcher(PathPatternRouteMatcher())
            .build()
    }

    @Bean
    fun metadataExtractor(): MetadataExtractor {
        return DefaultMetadataExtractor()
    }

    @Bean
    fun jwtDecoder(): NimbusReactiveJwtDecoder {
        return NimbusReactiveJwtDecoder.withIssuerLocation(issuerUri)
            .jwsAlgorithm(SignatureAlgorithm.RS256)
            .build()
    }

}