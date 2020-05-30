package com.geunyoung.webflux_practice.apsect

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Aspect
class LogAspect {
    private val logger: Logger = LoggerFactory.getLogger(LogAspect::class.java)
    @Around("execution(* com.geunyoung.webflux_practice..*.TestHandler.*(..))")
    fun logAdvice(pjp: ProceedingJoinPoint): Any {
        val packageName = pjp.signature.declaringTypeName
        val methodName = pjp.signature.name
        logger.info("$packageName : $methodName - start")
        val start = System.currentTimeMillis()
        val retVal = pjp.proceed()
        logger.info("$packageName : $methodName - end")
        val end = System.currentTimeMillis()
        logger.info("elapsed time: ${end - start}")

        return retVal
    }
}