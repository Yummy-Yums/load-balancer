package com.yums.loadbalancer.services


import com.yums.loadbalancer.domain.* 
import com.yums.loadbalancer.domain.UrlsRef.* 
import com.yums.loadbalancer.services.RoundRobin.BackendsRoundRobin
import zio.http.*
import zio.*
import com.yums.loadbalancer.domain.Urls.empty
import com.yums.loadbalancer.errors.parsing.InvalidUri
import com.yums.loadbalancer.http.HttpClientError

object LoadBalancer:
    def from(
        backends: Backends,
        sendAndExpectResponse: Request => SendAndExpect[String],
        parseUri: ParseUri,
        addRequestPathToBackendUrl: AddRequestPathToBackendUrl,
        backendsRoundRobin: BackendsRoundRobin
    ) =
        handler { (request: Request) =>
            backendsRoundRobin(backends).flatMap {
                    _.fold(ZIO.succeed(Response.text("All backends are inactive"))){ backendUrl =>
                        val url = addRequestPathToBackendUrl(backendUrl.value, request)
                    Response.
                        (for
                            parsedUrl <- ZIO.fromEither(parseUri(url)).tap(u => ZIO.logInfo(s"Parsed URI: $u"))
                            _ <- Console.printLine(parsedUrl)
                            response <- sendAndExpectResponse(request)(parsedUrl).tap(r => ZIO.logInfo(s"Response from backend: $r"))
                            result = Response(body = Body.fromString(response))
                        yield result).catchAll{
                            case err: InvalidUri => ZIO.succeed(Response.badRequest(s"${err}"))
                            case err: HttpClientError => ZIO.succeed(Response.badRequest(s"$err"))
                        }
                    }
                }.catchAll(_ => ZIO.succeed(Response.notFound))
        }.toRoutes
