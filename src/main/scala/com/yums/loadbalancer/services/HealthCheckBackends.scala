package com.yums.loadbalancer.services

import com.yums.loadbalancer.domain.*
import com.yums.loadbalancer.domain.UrlsRef.*
import com.yums.loadbalancer.http.ServerHealthStatus
import com.yums.loadbalancer.services.RoundRobin.HealthChecksRoundRobin
import zio.*
import com.yums.loadbalancer.http.HttpClientError

object HealthCheckBackends:
    def periodically(
        healthChecks: HealthChecks,
        backends: Backends,
        parseUri: ParseUri,
        updateBackendsAndGet: UpdateBackendsAndGet,
        healthChecksRoundRobin: HealthChecksRoundRobin,
        sendAndExpectStatus: SendAndExpect[ServerHealthStatus],
        healthCheckInterval: HealthCheckInterval,
    ) = checkHealthAndUpdateBackends(
        healthChecks,
        backends,
        parseUri,
        updateBackendsAndGet,
        healthChecksRoundRobin,
        sendAndExpectStatus
    )
    .tap(res => ZIO.logInfo(s"$res"))
    .tapError(res => ZIO.logError(s">>>$res"))
    .repeat(Schedule.spaced(healthCheckInterval.value.seconds)).unit

    private[services] def checkHealthAndUpdateBackends(
        healthchecks: HealthChecks,
        backends: Backends,
        parseUri: ParseUri,
        updateBackendsAndGet: UpdateBackendsAndGet,
        healthChecksRoundRobin: HealthChecksRoundRobin,
        sendAndExpectStatus: SendAndExpect[ServerHealthStatus],
    ): ZIO[Any, Exception, Urls] = 
        for
            currentUrl <- healthChecksRoundRobin(healthchecks)
            uri        <- ZIO.fromEither(parseUri(currentUrl.value)).orDie
            status     <- sendAndExpectStatus(uri).mapError(e => new Exception(s"$e"))
            updated    <- updateBackendsAndGet(backends, currentUrl, status)
        yield updated
