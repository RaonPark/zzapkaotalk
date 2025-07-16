package com.example.chatservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.cbor.Jackson2CborDecoder
import org.springframework.http.codec.cbor.Jackson2CborEncoder
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.messaging.rsocket.DefaultMetadataExtractor
import org.springframework.messaging.rsocket.MetadataExtractor
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler
import org.springframework.web.util.pattern.PathPatternRouteMatcher

@Configuration
class RSocketConfig {
    @Bean
    fun rsocketMessageHandler(): RSocketMessageHandler {
        return RSocketMessageHandler()
            .apply {
                rSocketStrategies = rsocketStrategies()
            }
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
}