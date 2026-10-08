package main.kotlin.serializer

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

val gson = GsonBuilder()
    .registerTypeAdapter(Hold::class.java, HoldDeserializer())
    .create()

class HoldDeserializer : JsonDeserializer<IHold> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): IHold? = runCatching {
        val map = mutableMapOf<IPoint, ISlot>()
        var home: IPoint? = null
        var marker: IPoint? = null
        var ioPort: Pair<IPoint, IContainer>? = null
        json!!.asJsonObject.entrySet().forEach { set ->
            val pointJson = set.value.asJsonObject
            val point = Point(pointJson.get("x").asInt, pointJson.get("y").asInt)
            when (set.key) {
                "marker" -> marker = point
                "io_port" -> ioPort = point to Container()
                "home" -> home = point
                else -> {
                    val slot = Slot(set.key, null)
                    map[point] = slot
                }
            }
        }
        Hold(map, home!!, marker!!, ioPort!!)
    }.getOrNull()

}