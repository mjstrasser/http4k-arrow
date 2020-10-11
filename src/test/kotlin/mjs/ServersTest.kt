package mjs

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

class ServersTest : DescribeSpec({

    describe("Hello server") {
        it("says Hello to the name it is given") {
            val fred = Request(Method.GET, "/").query("name", "Frederic")
            helloServer(fred) shouldBe Response(Status.OK).body("Hello, Frederic!")
        }
    }

})
