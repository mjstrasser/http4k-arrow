package servers

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Person(val name: String, val age: Int)

class SerializationTest : DescribeSpec({

    describe("Kotlin serialization") {
        it("serialises JSON as expected") {
            Json.encodeToString(Person("Mike", 21)) shouldBe """{"name":"Mike","age":21}"""
        }
    }

})