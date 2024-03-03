import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import android.location.Location

@Serializer(forClass = Location::class)
object LocationSerializer : KSerializer<Location?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Location", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Location?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeString("${value.latitude},${value.longitude}")
        }
    }

    override fun deserialize(decoder: Decoder): Location? {
        val coordinates = decoder.decodeString().split(',')
        return if (coordinates.size == 2) {
            Location("").apply {
                latitude = coordinates[0].toDouble()
                longitude = coordinates[1].toDouble()
            }
        } else {
            null
        }
    }
}
