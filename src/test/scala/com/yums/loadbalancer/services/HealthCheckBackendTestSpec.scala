package com.yums.loadbalancer.services

import com.yums.loadbalancer.domain.*
import com.yums.loadbalancer.domain.UrlsRef.*
import com.yums.loadbalancer.http.ServerHealthStatus
import com.yums.loadbalancer.services.RoundRobin.HealthChecksRoundRobin
import com.yums.loadbalancer.http.HttpClient

import zio.test.* 
import zio.test.TestAspect.*
import Assertion.*
import zio.*


object HealthCheckBackendTestSpec extends ZIOSpecDefault:
    def spec = suite("Testing Backends HealthCheck Functionality")(
        test("add backend url to the Backends as soon as health check returns success"){
            val healthChecks = Urls(Vector("http://localhost:8081", "http://localhost:8082").map(Url.apply))
            val obtained = for 
                backends     <- Ref.make(Urls(Vector(Url("http://localhost:8082"))))
                healthChecks <- Ref.make(healthChecks)
                result       <- HealthCheckBackends.checkHealthAndUpdateBackends(
                    HealthChecks(healthChecks),
                    Backends(backends),
                    ParseUri.Impl,
                    UpdateBackendsAndGet.Impl,
                    RoundRobin.forHealthChecks,
                    SendAndExpect.toHealthcheck(HttpClient.Hello)
                )
            yield result

            assertZIO(obtained)(equalTo(Urls(Vector("http://localhost:8082", "http://localhost:8081").map(Url.apply))))

        },
        test("remove backend url from the Backends as soon as health check returns failure"){
            val urls = Urls(Vector("http://localhost:8081", "http://localhost:8082").map(Url.apply))
            val obtained = for 
                backends     <- Ref.make(urls)
                healthChecks <- Ref.make(urls)
                result       <- HealthCheckBackends.checkHealthAndUpdateBackends(
                    HealthChecks(healthChecks),
                    Backends(backends),
                    ParseUri.Impl,
                    UpdateBackendsAndGet.Impl,
                    RoundRobin.forHealthChecks,
                    SendAndExpect.toHealthcheck(HttpClient.TestTimeoutFailure)
                ).fork
                _ <- TestClock.adjust(6.seconds)
                results <- result.join()
            yield results

            assertZIO(obtained)(equalTo(Urls(Vector("http://localhost:8082").map(Url.apply))))
        }
    )