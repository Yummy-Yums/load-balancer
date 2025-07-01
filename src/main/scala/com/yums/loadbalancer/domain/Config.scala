package com.yums.loadbalancer.domain

import zio.Config
import zio.config.magnolia.*
import zio.config.typesafe.*

final case class AppConfig(
    port: Int,
    host: String,
    backends: Urls,
    healthCheckInterval: HealthCheckInterval
)

object AppConfig:
    given Config[Url]  = Config.string.map(url => Url(url))
    // given Config[Urls] = Config.vectorOf("backends", summon[Config[Url]]).map(Urls(_))
    // given Config[HealthCheckInterval] = Config.int("health-check-interval").map(HealthCheckInterval(_))
    //val config: Config[AppConfig] = deriveConfig[AppConfig].nested("Application")

    val config: Config[AppConfig] = 
    (Config.int("port") ++
     Config.string("host") ++
     Config.vectorOf("backends", summon[Config[Url]]).map(Urls(_)) ++
     Config.int("health-check-interval").map(HealthCheckInterval(_))
    ).nested("Application").map { case (p, h, b, hci) =>
      AppConfig(p, h, b, hci)
    }
        

    def load() =
        TypesafeConfigProvider.fromResourcePath()
         .load(config)
