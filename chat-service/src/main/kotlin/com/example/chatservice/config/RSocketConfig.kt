package com.example.chatservice.config

import io.rsocket.metadata.WellKnownMimeType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.messaging.rsocket.DefaultMetadataExtractor
import org.springframework.messaging.rsocket.MetadataExtractor
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler
import org.springframework.messaging.rsocket.connectWebSocketAndAwait
import org.springframework.util.MimeTypeUtils
import org.springframework.web.util.pattern.PathPatternRouteMatcher
import reactor.util.retry.Retry
import java.net.URI
import java.time.Duration

@Configuration
class RSocketConfig {
    @Bean
    fun rSocketMessageHandler(): RSocketMessageHandler {
        val handler = RSocketMessageHandler()
        handler.rSocketStrategies = rsocketStrategies()
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
        return DefaultMetadataExtractor()
    }
}