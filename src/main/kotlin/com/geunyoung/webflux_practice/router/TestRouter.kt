package com.geunyoung.webflux_practice.router

import com.geunyoung.webflux_practice.handler.TestHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.router

@Component
class TestRouter(
        @Autowired private val testHandler: TestHandler
) {
    @Bean
    fun testRoutes(): RouterFunction<*> = router {
        "/test".nest {
            GET("/", testHandler::test)
        }
    }
}