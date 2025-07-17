package org.sayandev.sayanvanish.api.storage.serializers.gson

import com.google.gson.*
import java.lang.reflect.Type
import java.util.*

class UUIDJsonAdapter : JsonSerializer<UUID>, JsonDeserializer<UUID> {
    override fun serialize(src: UUID, typeOfSrc: Type, context: JsonSerializationContext): JsonObject {
        return JsonObject().apply {
            this.addProperty("value", src.toString())
        }
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): UUID {
        return json.asJsonObject.get("value")?.asString?.let { UUID.fromString(it) }
            ?: throw IllegalArgumentException("Invalid UUID format in JSON: $json")
    }
}