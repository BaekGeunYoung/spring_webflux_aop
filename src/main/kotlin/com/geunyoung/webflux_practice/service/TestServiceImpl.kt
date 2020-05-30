package com.geunyoung.webflux_practice.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class TestServiceImpl: TestService {
    override fun test(): Mono<Void> {
        val numbers = mutableListOf<Int>()

        for (i in (0 until 10000000)) {
            numbers.add(i)
        }

        return Mono.empty()
    }
}