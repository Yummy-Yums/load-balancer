package com.yums.loadbalancer.services

import com.yums.loadbalancer.errors.parsing.InvalidUri
import zio.test.* 
import zio.Scope
import Assertion.*
import zio.http.URL

object ParseUriSpec extends ZIOSpecDefault:
    val parseUri = ParseUri.Impl
    def spec = suite("Testing the Parsing of URI")(
        test("try parsing valid URI and return Right(Uri(...))"){
            val uri      = "0.0.0.0/8080"
            val obtained = parseUri(uri)

            assert(obtained)(equalTo(URL.decode(uri)))
        },
        test("try parsing valid URI and return Left(Uri(...))"){
            val uri      = "definitely invalid uri XD"
            val obtained = parseUri(uri)

            assert(obtained)(isLeft(equalTo(InvalidUri(s"$uri"))))
        }
    )