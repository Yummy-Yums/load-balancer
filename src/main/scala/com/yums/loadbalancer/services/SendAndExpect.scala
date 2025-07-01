package com.yums.loadbalancer.services

import java.net.URI
import zio.*
import ch.qos.logback.classic.Logger
import zio.http.Request
import com.yums.loadbalancer.http.{HttpClient, ServerHealthStatus}
import zio.http.URL
import zio.http.Response
import zio.http.Status
import com.yums.loadbalancer.http.HttpClientError.*
import com.yums.loadbalancer.http.HttpClientError
import scala.concurrent.duration.DurationInt

trait SendAndExpect[A]:
    def apply(uri: URL): ZIO[Any, HttpClientError, A]

object SendAndExpect:
    def toBackend(httpClient: HttpClient, req: Request): SendAndExpect[String] =
        new SendAndExpect[String] {
            override def apply(uri: URL) = 
                ZIO.logInfo(s"[LOAD-BALANCER] sending request to $uri with $req") *>
                    httpClient
                        .sendAndReceive(uri, Some(req))
                        .tap(response => ZIO.logInfo(s"[LOAD-BALANCER] received response: $response"))
                        .catchSome {
                            case UnexpectedStatus(status, requestMethod, reqestUri) => 
                                ZIO.succeed("resource not found")
                                    .tap(msg => ZIO.logError(msg))
                            case _ => 
                                ZIO.succeed(s"server with uri: $uri is dead")
                                    .tap(msg => ZIO.logError(msg))
                        }
        }

    
    def toHealthcheck(httpClient: HttpClient): SendAndExpect[ServerHealthStatus] =
        new SendAndExpect[ServerHealthStatus] {
            override def apply(uri: URL) = 
                ZIO.logInfo(s"[HEALTH-CHECK] checking $uri health") *>
                    httpClient
                        .sendAndReceive(uri, None)
                        .as(ServerHealthStatus.Alive)
                        .timeout(5.seconds)
                        .map(e => e.getOrElse(ServerHealthStatus.Dead))
                        .tap(_ => ZIO.logInfo(s"$uri is alive"))
                        .catchSome(_ => ZIO.logWarning(s"$uri is dead") *> ZIO.succeed(ServerHealthStatus.Dead))
        }

    val BackendSucessTest: SendAndExpect[String] = _ => ZIO.succeed("Success")