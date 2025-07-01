package com.yums.loadbalancer.services

import com.yums.loadbalancer.errors.parsing.InvalidUri
import zio.http.URL

trait ParseUri:
    def apply(uri: String): Either[InvalidUri, URL]

object ParseUri:
    object Impl extends ParseUri:

      override def apply(uri: String): Either[InvalidUri, URL] = 
        URL
          .decode(uri)
          .left.map(_ => InvalidUri(uri)) 