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
        val bornWhen: ZonedDateTime
)

class SerializationTest : DescribeSpec({

    describe("Kotlin serialization") {
        it("serialises JSON as expected") {
            val mikeBornWhen = ZonedDateTime.of(1961, 1, 4, 9, 4, 0, 0, ZoneOffset.ofHours(10))
            Json.encodeToString(Person("Mike", mikeBornWhen)) shouldBe
                    """{"name":"Mike","bornWhen":"1961-01-04T09:04+10:00"}"""
        }
        it("deserialises JSON as expected") {
            val lucyBornWhen = ZonedDateTime.of(1961, 1, 4, 9, 15, 0, 0, ZoneOffset.ofHours(10))
            Json.decodeFromString<Person>("""{"name":"Lucy","bornWhen":"1961-01-04T09:15+10:00"}""") shouldBe
                    Person("Lucy", lucyBornWhen)

        }
    }

})