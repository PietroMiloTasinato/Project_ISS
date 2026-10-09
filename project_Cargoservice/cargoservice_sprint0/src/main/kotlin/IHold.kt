package main.kotlin

interface IHold {

    val home: IPoint
    val ioPort: Pair<IPoint, IContainer?>
    val slot5: IPoint
    val holdMap: Map<IPoint, ISlot>

    fun getFreeSlot(): ISlot?

}