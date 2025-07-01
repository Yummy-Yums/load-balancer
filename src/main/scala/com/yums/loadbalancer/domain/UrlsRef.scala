package com.yums.loadbalancer.domain


import zio.*

enum UrlsRef(val urls: Ref[Urls]):
    case Backends(override val urls: Ref[Urls]) extends UrlsRef(urls)
    case HealthChecks(override val urls: Ref[Urls]) extends UrlsRef(urls)
