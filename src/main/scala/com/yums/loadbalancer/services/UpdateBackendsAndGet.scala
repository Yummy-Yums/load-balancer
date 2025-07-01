package com.yums.loadbalancer.services

import com.yums.loadbalancer.domain.* 
import com.yums.loadbalancer.domain.{Urls, UrlsRef}
import com.yums.loadbalancer.domain.UrlsRef.Backends
import com.yums.loadbalancer.http.ServerHealthStatus
import zio.*
import com.yums.loadbalancer.http.HttpClientError

trait UpdateBackendsAndGet:
    def apply(backends: Backends, url: Url, status: ServerHealthStatus): IO[Exception, Urls]

object UpdateBackendsAndGet:
    object Impl extends UpdateBackendsAndGet:

        override def apply(backends: Backends, url: Url, status: ServerHealthStatus): IO[Exception, Urls] = 
            backends.urls.updateAndGet { urls =>
                status match
                    case ServerHealthStatus.Alive => urls.add(url)
                    case ServerHealthStatus.Dead  => urls.remove(url)
            }