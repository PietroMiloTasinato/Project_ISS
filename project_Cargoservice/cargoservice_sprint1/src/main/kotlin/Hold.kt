package main.kotlin

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import main.kotlin.serializer.HoldDeserializer
import main.kotlin.serializer.gson
import java.io.File

class Hold(
    private val slotMap: Map<String, ISlot>,
    override val ioPort: Pair<IPoint, IContainer?>,
    home: IPoint,
    slot5: IPoint,
): IHold{

    override val slotNumber: Int = slotMap.size

    private val pointMap = slotMap.mapValues{ (key, value) ->
        value.point
    } + mapOf(
        "home" to home,
        "slot5" to slot5,
        "ioport" to ioPort.first
    )

    override fun getFreeSlot() = slotMap.values.firstOrNull { it.container == null }
    override fun getPoint(id: String) = pointMap[id]
    override fun markSlot(id: String, code: Int?){
        slotMap[id]?.container = Container(code)
    }

    companion object{

        val DEFAULT_CONFIG_FILE = File("src\\main\\resources\\HoldMap.json")

        fun createHoldFromConfig(file: File = DEFAULT_CONFIG_FILE): Hold {
            val json = file.readText()
            val hold = gson.fromJson(json, Hold::class.java)
            requireNotNull(hold) { "Error while deserializing Hold json" }
            return hold
        }
    }


}
