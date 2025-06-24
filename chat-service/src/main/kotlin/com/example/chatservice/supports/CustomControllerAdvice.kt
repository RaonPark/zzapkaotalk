package com.example.chatservice.supports

import com.example.chatservice.dto.ErrorResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@ControllerAdvice
class CustomControllerAdvice {
    companion object {
        val log = KotlinLogging.logger { }
    }

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(
        e: EntityNotFoundException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        log.info { "entity not found: ${e.message}" }

        return Mono.just(
            ResponseEntity.internalServerError().body(
                ErrorResponse(
                    errorCode = "EntityNotFound",
                    statusCode = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
                    message = e.message!!,
                    path = exchange.request.path.toString()
                )
            )
        )
    }
}