package com.geunyoung.webflux_practice

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.util.StopWatch
import org.springframework.web.client.RestTemplate

class BasicTest {
    @Test
    fun blocking(): Unit {
        val restTemplate = RestTemplate()
        val stopWatch = StopWatch()
        stopWatch.start()
        for (i in 0 until 100) {
            val response: ResponseEntity<String>
                    = restTemplate.exchange<String>("http://localhost:8080/test", HttpMethod.GET, HttpEntity.EMPTY, String::class.java)
        }
        stopWatch.stop()
        println(stopWatch.totalTimeSeconds)
    }
}