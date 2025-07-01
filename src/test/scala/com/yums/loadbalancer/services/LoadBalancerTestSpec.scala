package com.yums.loadbalancer.services

import com.yums.loadbalancer.domain.{Urls, Url}
import com.yums.loadbalancer.domain.UrlsRef.Backends
import com.yums.loadbalancer.http.HttpClient

import zio.test.* 
import zio.test.TestAspect.*
import Assertion.*
import zio.*
import zio.http.*

object LoadBalancerTestSpec extends ZIOSpecDefault:

    def spec = suite("Testing LoadBalancer Functionality")(
        test("All backends are inactive because Urls is empty"){
            val obtained = { for
                backends <- Ref.make(Urls.empty)
                loadbalancer = LoadBalancer.from(
                    Backends(backends),
                    _ => SendAndExpect.BackendSucessTest,
                    ParseUri.Impl,
                    AddRequestPathToBackendUrl.Impl,
                    RoundRobin.forBackends
                )
                result <- loadbalancer.run(Request())
            yield result.body.asString}.flatten

            assertZIO(obtained)(equalTo("All backends are inactive"))
        },
        test("Success case"){
            val obtained = {
                for {
                    backends <- Ref.make(Urls(Vector("http://localhost:8081", "http://localhost:8082").map(Url.apply)))
                    loadbalancer = LoadBalancer.from(
                        Backends(backends),
                        _ => SendAndExpect.BackendSucessTest,
                        ParseUri.Impl,
                        AddRequestPathToBackendUrl.Impl,
                        RoundRobin.LocalHost8081
                    )
                    result <- loadbalancer.run(Request(url = URL.decode("http://localhost:8080/items/1").right.get))
                    body <- result.body.asString
                } yield body
            }
        
            assertZIO(obtained)(equalTo("Success"))
        },
         test("Resource not found (404) case"){
            val obtained = {
                for {
                    backends <- Ref.make(Urls(Vector("http://localhost:8081", "http://localhost:8082").map(Url.apply)))
                    emptyRequest = Request()
                    loadbalancer = LoadBalancer.from(
                        Backends(backends),
                        _ => SendAndExpect.toBackend(HttpClient.BackendResourceNotFound, emptyRequest),
                        ParseUri.Impl,
                        AddRequestPathToBackendUrl.Impl,
                        RoundRobin.forBackends
                    )
                    result <- loadbalancer.run(Request(url = URL.decode("http://localhost:8080/items/1").right.get))
                    body <- result.body.asString
                } yield body
            }
        
            assertZIO(obtained)(equalTo("resource not found"))
        },

    )