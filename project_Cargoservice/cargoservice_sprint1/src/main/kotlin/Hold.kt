package main.kotlin

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import main.kotlin.serializer.HoldDeserializer
import main.kotlin.serializer.gson
import java.io.File

class Hold(
    override val holdMap: Map<IPoint, ISlot>,
    override val home: IPoint,
    override val marker: IPoint,
    override val ioPort: Pair<IPoint, IContainer>
): IHold{

    companion object{
        fun createHoldFromConfig(file: File): Hold {
            val json = file.readText()
            val hold = gson.fromJson(json, Hold::class.java)
            requireNotNull(hold) { "Error while deserializing Hold json" }
            return hold
        }
    }


}
