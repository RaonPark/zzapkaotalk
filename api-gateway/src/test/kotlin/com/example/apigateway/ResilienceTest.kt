package com.example.apigateway

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock
import org.wiremock.spring.InjectWireMock

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@EnableWireMock(
    ConfigureWireMock(
        name = "localhost",
        port = 8092
    )
)
@Import(TestcontainersConfig::class)
class ResilienceTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @InjectWireMock("localhost")
    private lateinit var wireMockServer: WireMockServer

    @Test
    fun `throw 429 error when too many requests`() = runTest {
        wireMockServer.stubFor(
            get(urlPathEqualTo("/mockApi"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""{"mock": "Success!"}""")
        ))

        val tryRequests = 1000

        val deferred = (1..tryRequests).map {
            async {
                webTestClient
                    .get()
                    .uri("/chat-service/mockApi")
                    .exchange()
                    .returnResult(String::class.java)
            }
        }

        val response = deferred.awaitAll()

        val statusCodes = response.map { it.status.value() }

        println("Status Codes: ${statusCodes.groupingBy { it }.eachCount()}")
    }
}