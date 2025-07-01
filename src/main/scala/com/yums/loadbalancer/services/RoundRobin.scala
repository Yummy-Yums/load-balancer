package com.yums.loadbalancer.services

import com.yums.loadbalancer.domain.{Url,Urls, UrlsRef}
import zio.* 

import scala.util.Try

trait RoundRobin[A]:
    def apply(ref: UrlsRef): ZIO[Any, Exception, A]

object RoundRobin:
    type BackendsRoundRobin     = RoundRobin[Option[Url]]
    type HealthChecksRoundRobin = RoundRobin[Url]

    def forBackends: BackendsRoundRobin  = new BackendsRoundRobin {
        override def apply(ref: UrlsRef): UIO[Option[Url]] = 
            ref.urls
                .getAndUpdate(next)
                .map(_.currentOpt)
    }

     def forHealthChecks[E]: HealthChecksRoundRobin = new HealthChecksRoundRobin {
        override def apply(ref: UrlsRef): UIO[Url] = 
            ref.urls
                .getAndUpdate(next)
                .map(_.currentUnsafe)
    }

    private def next(urls: Urls): Urls =
        Try(Urls(urls.values.tail :+ urls.values.head))
            .getOrElse(Urls.empty)

    val TestId: RoundRobin[Url] = _ => ZIO.succeed(Url("localhost:8081"))
    val LocalHost8081: RoundRobin[Option[Url]] = _ => ZIO.succeed(Some(Url("http://localhost:8081")))

