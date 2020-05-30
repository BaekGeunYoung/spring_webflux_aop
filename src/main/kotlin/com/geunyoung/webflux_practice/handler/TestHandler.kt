package com.geunyoung.webflux_practice.handler

import com.geunyoung.webflux_practice.service.TestService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.status
import reactor.core.publisher.Mono

@Component
class TestHandler(
        @Autowired private val testService: TestService
) {
    fun test(serverRequest: ServerRequest): Mono<ServerResponse> {
        return status(HttpStatus.NO_CONTENT).build()
    }
}