package servers

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.and
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.kotest.haveBody
import org.http4k.kotest.haveHeader
import org.http4k.kotest.haveStatus
import org.http4k.kotest.shouldNotHaveHeader
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue
import kotlin.time.seconds

class ServersTest : DescribeSpec({

    val animals = setOf("dog", "cat", "horse", "cow", "wombat", "wallaby", "dunnart", "antechinus", "echidna",
            "thylacine", "eel", "spider", "panther", "lion", "whale", "shark", "emu", "eagle", "hawk", "magpie")

    describe("helloServer") {
        it("says Hello to the name it is given") {
            val fred = Request(Method.GET, "/").query("name", "Frederic")
            helloServer(fred) should (haveStatus(OK) and haveBody("Hello, Frederic!"))
        }
    }

    describe("String.oddOrNull() extension function") {
        it("returns odd integers") {
            (1..99 step 2).forEach { odd -> odd.toString().oddOrNull() shouldBe odd }
        }
        it("returns null for even integers") {
            (0..98 step 2).forEach { even -> even.toString().oddOrNull() shouldBe null }
        }
        it("returns null for anything else") {
            animals.forEach { animal -> animal.oddOrNull() shouldBe null }
        }
    }

    describe("onlyOddServer") {
        fun numberResponse(numString: String) =
                onlyOddServer(Request(Method.GET, "/").query("number", numString))
        it("returns 200 OK when given an odd integer") {
            (1..99 step 2).forEach {
                numberResponse(it.toString()) should (haveStatus(OK) and haveBody("$it"))
            }
        }
        it("returns 400 Bad Request when given an even integer") {
            (0..100 step 2).forEach {
                numberResponse(it.toString()) should (haveStatus(BAD_REQUEST) and haveBody("bad"))
            }
        }
        it("returns 400 Bad Request when given a non-integer") {
            animals.forEach {
                numberResponse(it) should (haveStatus(BAD_REQUEST) and haveBody("bad"))
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    describe("slowServer") {
        it("takes as least as many seconds specified") {
            measureTimedValue {
                slowServer(Request(Method.GET, "/").query("delay", "2"))
            }.duration shouldBeGreaterThanOrEqualTo 2.seconds
        }
    }

    describe("failServer") {
        it("returns 500 Internal Server Error") {
            failServer(Request(Method.GET, "/")) should
                    (haveStatus(Status.INTERNAL_SERVER_ERROR) and haveBody("error"))
        }
    }

    describe("requestIdFilter") {
        it("ignores $REQUEST_ID_HEADER if not present in request") {
            helloServer(Request(Method.GET, "/")) shouldNotHaveHeader REQUEST_ID_HEADER
        }
        it("applies the value of $REQUEST_ID_HEADER from header to response") {
            val animal = animals.random()
            helloServer(
                    Request(Method.GET, "/").header(REQUEST_ID_HEADER, animal)
            ) should haveHeader(REQUEST_ID_HEADER, animal)
        }
    }
})
