package com.yums.loadbalancer.services

import zio.test.*
import zio.http.{Request, URL}
import Assertion.*

object AddRequestPathToBackendUrlTest extends ZIOSpecDefault:
    val impl       = AddRequestPathToBackendUrl.Impl
    val backendUrl = "http://localhost:8082"

    def spec = suite("Test adding request path to the load balancer backend")(
        test("add '/items/1' to backendUri"){
            val obtained = impl(backendUrl = backendUrl,  Request(url=URL.decode("http://localhost:8080/items/1").toOption.get))
            val expected = "http://localhost:8082/items/1"

            assert(obtained)(equalTo(expected))
        },
        test("since request doesn't have path just return backendUrl"){
            val obtained = impl(backendUrl = backendUrl,  Request(url=URL.decode("http://localhost:8080").toOption.get))
            val expected = backendUrl

            assert(obtained)(equalTo(expected))
        
        }
    )


