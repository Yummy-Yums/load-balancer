import com.yums.loadbalancer.domain.AppConfig
import zio.config.typesafe.TypesafeConfigProvider._
import zio.config.magnolia._
import zio._
import AppConfig.config
import zio.config.typesafe.*
import com.yums.loadbalancer.errors.config.InvalidConfig
import zio.http.*
import com.yums.loadbalancer.http.HttpServer
import com.yums.loadbalancer.domain.*
import com.yums.loadbalancer.domain.UrlsRef.*
import com.yums.loadbalancer.services.*
import zio.http.netty.NettyConfig

object Main extends ZIOAppDefault:

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] = {
    for {
      // Load config using the given instances from AppConfig
      config <- AppConfig.load()
      backendUrls = config.backends
      backends <- Ref.make(backendUrls)
      healthChecks <- Ref.make(backendUrls)
      _ <- ZIO.debug(s"Starting server on Host: ${config.host}, Port: ${config.port}")
      _ <- HttpServer.start(
        UrlsRef.Backends(backends),
        HealthChecks(healthChecks),
        config.port,
        config.host,
        config.healthCheckInterval
      ).provide(
        Scope.default,
        Client.default,
        ZLayer.succeed(UpdateBackendsAndGet.Impl),
        ZLayer.succeed(ParseUri.Impl),
        ZLayer.succeed(RoundRobin.forBackends),
        ZLayer.succeed(RoundRobin.forHealthChecks),
      )
      
    } yield ()
  }