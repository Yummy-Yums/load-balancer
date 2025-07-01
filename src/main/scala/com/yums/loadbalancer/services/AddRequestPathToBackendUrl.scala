package com.yums.loadbalancer.services

import zio.* 
import zio.http.Request

trait AddRequestPathToBackendUrl:
    def apply(backendUrl: String, request: Request): String

object AddRequestPathToBackendUrl:
    object Impl extends AddRequestPathToBackendUrl:
        override def apply(backendUrl: String, request: Request): String =
            val requestPath = request.url.path.toString().dropWhile(_ != '/')

            backendUrl concat requestPath
