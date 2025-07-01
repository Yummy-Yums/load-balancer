package com.yums.loadbalancer.http

import zio.http.*
import zio.*

enum HttpClientError:
    case UnexpectedStatus(status: Status, requestMethod: Method, reqestUri: URL)
    case ServiceDead
    case NetworkError(cause: Throwable)

trait HttpClient:
    def sendAndReceive(uri: URL, requestOpt: Option[Request]): ZIO[Any, HttpClientError, String]

object HttpClient:
    def of(client: Client): HttpClient = new HttpClient {
      override def sendAndReceive(url: URL, requestOpt: Option[Request]) = 
            (requestOpt match
                case None          => ZIO.logInfo("I'm up") *> client.batched(Request.get(url))

                case Some(request) => ZIO.logInfo("I'm down") *> ZIO.logInfo(s"this is the url ${request.copy(url = url) } ++ ${url}") *> client.batched(request = request.copy(url = url))
            )
            .flatMap { response =>
              if response.status.isSuccess then
                response.body.asString
              else
                response.body.asString.flatMap { body =>
                    val method = requestOpt.map(_.method).getOrElse(Method.GET) // Safe access
                    ZIO.fail(HttpClientError.UnexpectedStatus(response.status, method, url))
                }
            }
            .tap(msg => ZIO.logInfo(s"received the following $msg"))
            .tapError(error => ZIO.logError(s"Failed to connect to $url ,  $error is returned"))
            .mapError{
                case e: Throwable => HttpClientError.NetworkError(e)
            }
    }

    val Hello: HttpClient                   = (_, _) => ZIO.succeed("Hello")
    val RuntimeException: HttpClient        = (_, _) => ZIO.fail(HttpClientError.NetworkError(new RuntimeException("Server is dead")))
    val TestTimeoutFailure: HttpClient      = (_, _) => Clock.sleep(6.seconds).as("")
    val BackendResourceNotFound: HttpClient = (_, _) =>
        ZIO.fail {
            HttpClientError.UnexpectedStatus(
                Status.NotFound,
                Method.GET,
                URL.decode("http://localhost:8081").right.get
            )
        }