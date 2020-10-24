package servers

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.time.ZoneOffset
import java.time.ZonedDateTime

object ZonedDateTimeSerializer : KSerializer<ZonedDateTime> {
    override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("servers.ZonedDateTimeSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ZonedDateTime =
            ZonedDateTime.parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: ZonedDateTime) =
            encoder.encodeString(value.toString())
}

@Serializable
data class Person(
        val name: String,
        @Serializable(ZonedDateTimeSerializer::class)
        val dob: ZonedDateTime
)

class SerializationTest : DescribeSpec({

    describe("Kotlin serialization") {
        it("serialises JSON as expected") {
            Json.encodeToString(
                    Person("Mike", ZonedDateTime.of(1961, 1, 4, 9, 4, 0, 0, ZoneOffset.ofHours(10)))
            ) shouldBe """{"name":"Mike","dob":"1961-01-04T09:04+10:00"}"""
        }
        it("deserialises JSON as expected") {
            Json.decodeFromString<Person>(
                    """{"name":"Lucy","dob":"1961-01-04T09:11+10:00"}"""
            ) shouldBe Person(
                    "Lucy", ZonedDateTime.of(1961, 1, 4, 9, 11, 0, 0, ZoneOffset.ofHours(10))
            )

        }
    }

})