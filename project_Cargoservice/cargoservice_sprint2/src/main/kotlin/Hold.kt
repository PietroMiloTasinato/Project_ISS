package main.kotlin

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import main.kotlin.serializer.HoldDeserializer
import main.kotlin.serializer.gson
import java.io.File

class Hold(
    private val slotMap: Map<String, ISlot>,
    override val ioPort: ISlot,
    override val slot5: ISlot,
    home: IPoint,
): IHold(){

    private val pointMap = slotMap.mapValues{ (key, value) ->
        value.point
    } + mapOf(
        "home" to home,
        "slot5" to slot5.point,
        "ioport" to ioPort.point
    )

    override fun getFreeSlot() = slotMap.values.firstOrNull { it.status == ISlot.Status.FREE }
    override fun getPoint(id: String) = pointMap[id]
    override fun changeStatus(id: String, status: String){
        runCatching{ ISlot.Status.valueOf(status) }.getOrNull()?.let{
            slotMap[id]?.status = it
        }
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
