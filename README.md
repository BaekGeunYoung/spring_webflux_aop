# spring_webflux_aop

## spring webflux

책 "[코틀린 마이크로 서비스 개발](http://acornpub.co.kr/book/microservices-kotlin)"을 통해 spring webflux에 대해 알게 되었고, 이후 이를 [쿠팡 클론 코딩](https://github.com/Leejunhyuck/zzappang) 프로젝트에 적용하여 진행을 하고 있다. spring webflux가 가진 뛰어난 동시성에 대해 간단한 테스트를 진행해보았다.

### spring webflux가 작동하는 방식

spring webflux는 blocking I/O를 이용하는 기존의 spring MVC와는 다르게, node와 비슷하게 event loop을 기반으로 한 nonblocking I/O 방식으로 http 요청을 처리한다.

![event_loop](./images/event_loop.png)

그래서 spring webflux를 이용하면 적은 스레드 수로도 많은 양의 request를 감당할 수 있고, 높은 동시성을 가진 어플리케이션을 개발할 수 있다.

### 테스트 코드
```kotlin
    @Test
    fun blocking() {
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
```

테스트 방식은 단순하다. 같은 주소로 반복해서 다수의 요청을 보내보는 것이다. 이 api가 하는 일은 아래와 같이, 100만번동안 루프를 돌면서 list에 값을 추가하는 것이다.

```kotlin
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
```
```
17:39:44.921 [Test worker] DEBUG org.springframework.web.client.RestTemplate - HTTP GET http://localhost:8080/test
17:39:44.921 [Test worker] DEBUG org.springframework.web.client.RestTemplate - Accept=[text/plain, application/json, application/*+json, */*]
17:39:44.924 [Test worker] DEBUG org.springframework.web.client.RestTemplate - Response 204 NO_CONTENT
1.4330942
```

해당 api 요청을 100번 수행하는 데 1.43초 정도가 걸렸다. 이에 반해 기존의 spring MVC를 이용해 같은 작업을 수행하면 2분이 넘는 시간이 걸린다. 또한 요청을 수행하면서 사용하게 되는 스레드의 수 또한 blocking I/O 방식에서는 10개의 스레드를 쓰는 것을 확인하였고, nonblocking 방식에서는 1개의 스레드만을 사용하는 것을 확인할 수 있었다.

## spring AOP

AOP란 Aspect Oriented Programming (관점 지향 프로그래밍)의 약자로, 애플리케이션 내에서 실행되는 로직을 핵심적인 관점과 부수적인 관점으로 나누어 비즈니스 로직을 명시적으로 분리하려는 목적을 가지고 있다.

### AOP의 주요 개념

- **Aspect** : 위에서 설명한 흩어진 관심사를 모듈화 한 것. 주로 부가기능을 모듈화함.
- **Target** : Aspect를 적용하는 곳 (클래스, 메서드 .. )
- **Advice** : 실질적으로 어떤 일을 해야할 지에 대한 것, 실질적인 부가기능을 담은 구현체
- **JoinPoint** : Advice가 적용될 위치, 끼어들 수 있는 지점. 메서드 진입 지점, 생성자 호출 시점, 필드에서 값을 꺼내올 때 등 다양한 시점에 적용가능
- **PointCut** : JoinPoint의 상세한 스펙을 정의한 것. 'A란 메서드의 진입 시점에 호출할 것'과 같이 더욱 구체적으로 Advice가 실행될 지점을 정할 수 있음

### LogAspect

가장 대표적인 부가기능 중 하나인 로깅 작업을 AOP로 분리하는 LogAspect라는 클래스를 정의했다.

```kotlin
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
```

이 advice는 TestHandler 내의 메소드를 실행할 때마다 실행되며, 메소드의 시작과 종료, 실행 시간 등을 로깅한다.

```
2020-05-30 17:39:43.991  INFO 19728 --- [ctor-http-nio-2] c.g.webflux_practice.apsect.LogAspect    : com.geunyoung.webflux_practice.handler.TestHandler : test - start
2020-05-30 17:39:44.009  INFO 19728 --- [ctor-http-nio-2] c.g.webflux_practice.apsect.LogAspect    : com.geunyoung.webflux_practice.handler.TestHandler : test - end
2020-05-30 17:39:44.009  INFO 19728 --- [ctor-http-nio-2] c.g.webflux_practice.apsect.LogAspect    : elapsed time: 17
```

이전까지 spring boot로 애플리케이션을 개발할 때는 컨트롤러에 존재하는 모든 메소드의 앞뒤에 명시적으로 로그를 남겼었는데, 위와 같이 AOP를 통해 비즈니스 로직에 더 집중할 수 있게 되었고, 불필요하게 재사용되는 코드 또한 줄일 수 있게 되었다.