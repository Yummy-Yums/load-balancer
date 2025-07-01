package com.yums.loadbalancer.http


import com.yums.loadbalancer.domain.*
import com.yums.loadbalancer.domain.UrlsRef.{Backends, HealthChecks}
import com.yums.loadbalancer.services.RoundRobin.{BackendsRoundRobin, HealthChecksRoundRobin}
import com.yums.loadbalancer.services.{
  AddRequestPathToBackendUrl,
  HealthCheckBackends,
  LoadBalancer,
  ParseUri,
  SendAndExpect,
  UpdateBackendsAndGet,
}
import zio.*
import zio.http.*

object HttpServer:
    def start(
        backends: Backends, // state references
        healthChecks: HealthChecks,
        port: Int, // configs
        host: String,
        healthCheckInterval: HealthCheckInterval,
    ) = (
        for 
            client <- ZIO.service[Client]
            parseUri <- ZIO.service[ParseUri]
            backendsRoundRobin     <- ZIO.service[BackendsRoundRobin]
            updateBackendsAndGet   <- ZIO.service[UpdateBackendsAndGet]
            healthChecksRoundRobin <- ZIO.service[HealthChecksRoundRobin]
            httpClient = HttpClient.of(client)
             _ <- HealthCheckBackends.periodically(
                healthChecks,
                backends,
                parseUri,
                updateBackendsAndGet,
                healthChecksRoundRobin,
                SendAndExpect.toHealthcheck(httpClient),
                healthCheckInterval
            ).forkDaemon
            httpApp    = LoadBalancer.from(
                backends,
                SendAndExpect.toBackend(httpClient, _),
                parseUri,
                AddRequestPathToBackendUrl.Impl,
                backendsRoundRobin   
            ) @@ Middleware.debug
             config = Server.Config.default.binding(host, port)
            _ <- Server.serve(httpApp).provide(ZLayer.succeed(config), Server.live)
        yield ()
    )