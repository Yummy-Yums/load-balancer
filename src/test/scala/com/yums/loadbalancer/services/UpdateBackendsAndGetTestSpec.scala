package com.yums.loadbalancer.services

import com.yums.loadbalancer.domain.{Urls, Url}
import com.yums.loadbalancer.domain.UrlsRef.Backends
import com.yums.loadbalancer.http.ServerHealthStatus
import zio.test.* 
import zio.test.TestAspect.*
import Assertion.*
import zio.*

object UpdateBackendsAndGetTestSpec extends ZIOSpecDefault:
    val updateBackendsAndGet = UpdateBackendsAndGet.Impl
    val localhost8083 = "localhost:8083"
    val initialUrls = Vector("localhost:8081", "localhost:8082").map(Url.apply)

     def spec = suite("Testing Updating Backends Round Robin Logic")(
        test("Add the passed url to the Backends when the server status is Alive"){
            val urls = Urls(initialUrls)
            val obtained = for 
                ref     <- Ref.make(urls)
                updated <- updateBackendsAndGet(Backends(ref), Url(localhost8083), ServerHealthStatus.Alive)
            yield updated

            assertZIO(obtained)(equalTo(Urls(initialUrls :+ Url(localhost8083))))
        },
        test("Add the passed url to the Backends when the server status is Dead"){
            val urls     = Urls(initialUrls )
            val obtained = for 
                ref     <- Ref.make(urls)
                updated <- updateBackendsAndGet(Backends(ref), Url(localhost8083), ServerHealthStatus.Dead)
            yield updated

            assertZIO(obtained)(equalTo(Urls(initialUrls)))
        }
     )

