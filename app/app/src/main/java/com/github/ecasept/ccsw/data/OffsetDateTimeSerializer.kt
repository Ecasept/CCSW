package com.github.ecasept.ccsw.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Encoder
import java.time.OffsetDateTime

/** There is no built-in serializer for OffsetDateTime,
 * so this is an implementation of it.
 * [Custom Serializers](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serializers.md#custom-serializers)
 */
object OffsetDateTimeSerializer : KSerializer<OffsetDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "com.github.ecasept.ccsw", PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: OffsetDateTime) {
        val encodedValue = value.toString()
        encoder.encodeString(encodedValue)
    }

    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): OffsetDateTime {
        val decodedValue = decoder.decodeString()
        return OffsetDateTime.parse(decodedValue)
    }
}
