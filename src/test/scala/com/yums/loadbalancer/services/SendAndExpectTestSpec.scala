package com.yums.loadbalancer.services


import zio.*
import zio.test.* 
import zio.test.TestAspect.*
import Assertion.*
import zio.http.*
import com.yums.loadbalancer.http.*

object SendAndExpectTestSpec extends ZIOSpecDefault:
    val localhost8080 = "http://localhost:8080"
    val backend       = URL.decode(localhost8080).right.get
    val backend2      = URL.decode("http://localhost:8081").right.get
    val emptyRequest  = Request()

    def spec = suite("Testing client sending a request and getting a repsonse")(
        test("toBackend [Success]"){
            val sendAndExpect = SendAndExpect.toBackend(HttpClient.Hello, emptyRequest)
            sendAndExpect(backend).map(obtained => assert(obtained)(equalTo("Hello"))) 
        },
        test("toBackend [Failure]"){
            val sendAndExpect = SendAndExpect.toBackend(HttpClient.RuntimeException, emptyRequest)
            val obtained      = sendAndExpect(backend)

            assertZIO(obtained)(equalTo(s"server with uri: $localhost8080 is dead"))
        },
        test("toBackendResource Not found"){
            val sendAndExpect = SendAndExpect.toBackend(HttpClient.BackendResourceNotFound, emptyRequest)
            val obtained      = sendAndExpect(backend2)

            assertZIO(obtained)(equalTo(s"resource not found"))
        },
        test("toHealthCheck [Alive]"){
            val sendAndExpect = SendAndExpect.toHealthcheck(HttpClient.Hello)
            val obtained      = sendAndExpect(backend)

            assertZIO(obtained)(equalTo(ServerHealthStatus.Alive))
        },
        test("toHealthCheck [Dead due to a timeout]"){
            for {
                sendAndExpect <- ZIO.succeed(SendAndExpect.toHealthcheck(HttpClient.TestTimeoutFailure))
                fiberObtained <- sendAndExpect(backend).fork
                _ <- TestClock.adjust(6.seconds)
                obtained <- fiberObtained.join()
            } yield assert(obtained)(equalTo(ServerHealthStatus.Dead))
            
        } @@ timeout(6.seconds),
        test("toHealthCheck [Dead due to an exception]"){
            val sendAndExpect = SendAndExpect.toHealthcheck(HttpClient.RuntimeException)
            val obtained      = sendAndExpect(backend)

            assertZIO(obtained)(equalTo(ServerHealthStatus.Dead))
        }

    )