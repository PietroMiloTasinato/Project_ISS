package main.kotlin

interface IHold {

    val ioPort: Pair<IPoint, IContainer?>
    val slotNumber: Int
    fun getFreeSlot(): ISlot?
    fun getPoint(id: String): IPoint?
    fun markSlot(id: String, code: Int?)
}