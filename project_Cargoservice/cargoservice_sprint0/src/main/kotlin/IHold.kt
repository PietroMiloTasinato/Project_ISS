package main.kotlin

interface IHold {

    val home: IPoint
    val ioPort: Pair<IPoint, IContainer?>
    val slot5: IPoint
    val holdMap: MutableMap<IPoint, ISlot>

    fun getFreeSlot(): ISlot?

}