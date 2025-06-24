package com.example.chatservice.bdd

import io.github.oshai.kotlinlogging.KotlinLogging

interface GivenScope
interface WhenScope
interface ThenScope

object BDDSyntax {
    val log = KotlinLogging.logger {}
    fun Given(description: String, invoker: GivenScope.() -> Unit) {
        log.info { "Given Scope : $description" }
        val givenScope = object : GivenScope { }
        givenScope.invoker()
    }

    fun GivenScope.When(description: String, invoker: WhenScope.() -> Unit) {
        log.info { "When Scope : $description" }
        val whenScope = object : WhenScope { }
        whenScope.invoker()
    }

    fun WhenScope.Then(description: String, invoker: ThenScope.() -> Unit) {
        log.info { "Then Scope : $description" }
        val thenScope = object : ThenScope { }
        thenScope.invoker()
    }
}