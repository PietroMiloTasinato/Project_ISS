package main.kotlin.serializer

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import main.kotlin.Container
import main.kotlin.Hold
import main.kotlin.IContainer
import main.kotlin.IHold
import main.kotlin.IPoint
import main.kotlin.ISlot
import main.kotlin.Point
import main.kotlin.Slot
import java.lang.reflect.Type

val gson: Gson = GsonBuilder()
    .registerTypeAdapter(Hold::class.java, HoldDeserializer())
    .create()

class HoldDeserializer : JsonDeserializer<IHold> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): IHold? = runCatching {
        val map = mutableMapOf<String, ISlot>()
        lateinit var home: IPoint
        lateinit var slot5: ISlot
        lateinit var ioPort: ISlot
        json!!.asJsonObject.entrySet().forEach { set ->
            val pointJson = set.value.asJsonObject
            val point = Point(
                pointJson.get("x").asInt,
                pointJson.get("y").asInt,
                pointJson.get("direction").asString
            )
            when (set.key) {
                "slot5" -> slot5 = Slot(set.key, ISlot.Status.FREE, point)
                "io_port" -> ioPort = Slot(set.key, ISlot.Status.FREE, point)
                "home" -> home = point
                else -> {
                    val slot = Slot(set.key, ISlot.Status.FREE, point)
                    map[slot.id] = slot
                }
            }
        }
        Hold(map, ioPort, slot5, home)
    }.getOrNull()

}