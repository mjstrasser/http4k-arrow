package servers

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

class ServersTest : DescribeSpec({

    val animals = setOf("dog", "cat", "horse", "cow", "wombat", "wallaby", "dunnart", "antechinus", "echidna",
            "thylacine", "eel", "spider", "panther", "lion", "whale", "shark", "emu", "eagle", "hawk", "magpie")

    describe("helloServer") {
        it("says Hello to the name it is given") {
            val fred = Request(Method.GET, "/").query("name", "Frederic")
            helloServer(fred) shouldBe Response(Status.OK).body("Hello, Frederic!")
        }
    }

    describe("String.oddOrNull() function") {
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
        it("returns 200 OK with empty body when given an odd integer") {
            (1..99 step 2).forEach { numberResponse(it.toString()) shouldBe Response(Status.OK) }
        }
        it("returns 400 Bad Request when given an even integer") {
            (0..100 step 2).forEach { numberResponse(it.toString()) shouldBe Response(Status.BAD_REQUEST) }
        }
        it("returns 400 Bad Request when given a non-integer") {
            animals.forEach { numberResponse(it) shouldBe Response(Status.BAD_REQUEST) }
        }
    }

    describe("slowServer") {
        fun executionMillis(block: () -> Unit): Long {
            val start = System.currentTimeMillis()
            block()
            return System.currentTimeMillis() - start
        }
        it("takes as least as many seconds specified") {
            executionMillis {
                slowServer(Request(Method.GET, "/").query("delay", "2"))
            } shouldBeGreaterThanOrEqualTo 2000
        }
    }

    describe("failServer") {
        it("returns 500 Internal Server Error") {
            failServer(Request(Method.GET, "/")) shouldBe Response(Status.INTERNAL_SERVER_ERROR)
        }
    }
})
