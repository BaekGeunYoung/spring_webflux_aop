package com.geunyoung.webflux_practice.service

import reactor.core.publisher.Mono

interface TestService {
    fun test(): Mono<Void>
}