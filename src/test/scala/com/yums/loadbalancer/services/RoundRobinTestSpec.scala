package com.yums.loadbalancer.services

import zio.*
import zio.test.* 
import zio.test.TestAspect.*
import Assertion.*
import zio.http.*
import com.yums.loadbalancer.domain.{Url, Urls}
import com.yums.loadbalancer.domain.UrlsRef.*

object RoundRobinTestSpec extends ZIOSpecDefault:

     def spec = suite("Testing Round Robin Logic")(
        test("forBackends [Some, one url]"){
            val roundRobin = RoundRobin.forBackends
            val assertion = for 
                ref <- Ref.make(Urls(Vector(Url("localhost:8082"))))
                backends = Backends(ref)
                assertion1 <- roundRobin(backends)
                    .map(_.exists(_.value == "localhost:8082"))
                assertion2 <- roundRobin(backends)
                    .map(_.exists(_.value == "localhost:8082"))
            yield assertion1 && assertion2

            assertZIO(assertion)(isTrue)
        },
        test("forBackends [Some, multiple urls]"){
            val roundRobin = RoundRobin.forBackends
            val urls = Urls(Vector("localhost:8081", "localhost:8082").map(Url.apply))
            val assertion = for 
                ref <- Ref.make(urls)
                backends = Backends(ref)
                assertion1 <- roundRobin(backends)
                    .map(_.exists(_.value == "localhost:8081"))
                assertion2 <-ref.get
                    .map(_.values.map(_.value) == Vector("localhost:8082", "localhost:8081"))
                assertion3 <- roundRobin(backends)
                    .map(_.exists(_.value == "localhost:8082"))
                assertion4 <- ref.get.map(_.values.map(_.value) == Vector("localhost:8081", "localhost:8082"))
            yield List(assertion1, assertion2, assertion3, assertion4).reduce(_ && _)

            assertZIO(assertion)(isTrue)
        },
        test("forBackends [None]"){
            val roundRobin = RoundRobin.forBackends
            val assertion = for 
                ref <- Ref.make(Urls.empty)
                result <- roundRobin(Backends(ref))
            yield result.isEmpty

            assertZIO(assertion)(isTrue)
        },
        test("forHealthChecks [Some, one url]"){
            val roundRobin = RoundRobin.forHealthChecks
            val assertion = for 
                ref <- Ref.make(Urls(Vector(Url("localhost:8082"))))
                result <- roundRobin(HealthChecks(ref))
            yield result.value == "localhost:8082"

            assertZIO(assertion)(isTrue)
        },
        test("forBackends [Some, multiple urls]"){
            val roundRobin = RoundRobin.forHealthChecks
            val urls = Urls(Vector("localhost:8081", "localhost:8082").map(Url.apply))
            val assertion = for 
                ref <- Ref.make(urls)
                healthchecks = HealthChecks(ref)
                assertion1 <- roundRobin(healthchecks)
                    .map(_.value == "localhost:8081")
                assertion2 <-ref.get
                    .map(_.values.map(_.value) == Vector("localhost:8082", "localhost:8081"))
                assertion3 <- roundRobin(healthchecks)
                    .map(_.value == "localhost:8082")
                assertion4 <- ref.get
                    .map(_.values.map(_.value) == Vector("localhost:8081", "localhost:8082"))
            yield List(assertion1, assertion2, assertion3, assertion4).reduce(_ && _)

            assertZIO(assertion)(isTrue)
        },
        test("forHealthChecks [Exception, empty urls]"){
            val assertion = for 
                ref    <- Ref.make(Urls(Vector.empty))
                result <- RoundRobin.forHealthChecks(HealthChecks(ref))
                     .as(false)
                     .catchAllDefect {
                        case _: NoSuchElementException => ZIO.succeed(true)
                        case _ => ZIO.succeed(false)
                      }
            yield result

            assertZIO(assertion)(isTrue)
        },
        test("forBackends [Some, with stateful Ref updates]"){
            val roundRobin = RoundRobin.forBackends
            val urls = Urls(Vector("localhost:8081", "localhost:8082").map(Url.apply))
            val assertion = for 
                ref <- Ref.make(urls)
                backends = Backends(ref)

                assertion1 <- roundRobin(backends)
                    .map(_.exists(_.value == "localhost:8081"))
                _ <- ref.getAndUpdate { urls =>
                    Urls(urls.values :+ Url("localhost:8083"))
                }

                assertion2 <- roundRobin(backends)
                    .map(_.exists(_.value == "localhost:8082"))

                assertion3 <- ref.get.map{ urls =>
                    println(urls)
                    urls.values.map(_.value) == Vector("localhost:8081", "localhost:8083", "localhost:8082")
                }

                assertion4 <- roundRobin(backends)
                    .map(_.exists(_.value == "localhost:8081"))

                assertion5 <- roundRobin(backends)
                    .map(_.exists(_.value == "localhost:8083"))
            yield {
                List(assertion1, assertion2, assertion3, assertion4).reduce(_ && _)
            }

            assertZIO(assertion)(isTrue)
        }

     )