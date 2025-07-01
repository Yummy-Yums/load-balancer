package com.yums.loadbalancer.domain

import zio.test.* 
import Assertion.*

object UrlsTestSpec extends ZIOSpecDefault:
    private def sequentialUrls(from: Int, ends: Int): Urls = Urls {
        (from to ends)
            .map(i => Url(s"url$i"))
            .toVector
    }

    def spec = suite("Testing Urls domain Type and it's associated functionality")(
        test("Urls(url1, url2, ...).currentOpt must return Some(url1)"){
            val urls = sequentialUrls(1, 5)
            val obtained = urls.currentOpt.map(_.value)
            val expected = Some("url1")

            assert(obtained)(isSome && equalTo(expected))
        },
        test("Urls.empty.currentOpt must return None"){
            val obtained = Urls.empty.currentOpt.map(_.value)
            assert(obtained)(isNone)
        },
        test("Urls(url1, url2, ...).currentUnsafe must return url1"){
            val urls = sequentialUrls(1, 5)
            val obtained = urls.currentUnsafe.value
            val expected = "url1"

            assert(obtained)(equalTo(expected))
        },
        test("Urls.empty.currentUnsafe should throw NoSuchElementException (based on Vector Implementation)"){
            assert(Urls.empty.currentUnsafe)(
                throwsA[NoSuchElementException]
            )
        },
        test("Urls(url1, urls2, ...).remove should drop url1"){
            val urls     = sequentialUrls(1, 5)
            val getUrl   = urls.remove(Url("url1"))
            val expected = sequentialUrls(2, 5)

            assert(getUrl)(equalTo(expected))
        },
        test("Urls(url1, urls2, ...).add should append url1 to the end of the Vector"){
            val urls     = sequentialUrls(2, 5)
            val getUrl   = urls.add(Url("url1"))
            val expected = Urls(urls.values :+ Url("url1"))
    
            assert(getUrl)(equalTo(expected)) 
        }

    )
