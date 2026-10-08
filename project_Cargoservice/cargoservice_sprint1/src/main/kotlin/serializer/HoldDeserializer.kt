package main.kotlin.serializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import main.kotlin.Hold
import main.kotlin.IHold
import main.kotlin.IPoint
import main.kotlin.ISlot
import main.kotlin.Point
import main.kotlin.Slot
import java.lang.reflect.Type

class HoldSerializer : JsonDeserializer<IHold> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): IHold? = (json?.asJsonObject)?.let { j ->
        val map = mutableMapOf<IPoint, ISlot>()
        j.entrySet().forEach { set ->
            val slot = Slot(set.key, null)
            val pointJson = set.value.asJsonObject
            val point = Point(pointJson.get("x").asInt, pointJson.get("y").asInt)
            map[point] = slot
        }
        Hold(map)
    }

}