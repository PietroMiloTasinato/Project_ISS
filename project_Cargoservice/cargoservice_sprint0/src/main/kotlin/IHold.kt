package main.kotlin

abstract class IHold {
    val slotNumber: Int = 4
    abstract val ioPort: ISlot
    abstract val slot5: ISlot
    abstract fun getFreeSlot(): ISlot?
    abstract fun getPoint(id: String): IPoint?
    abstract fun codeAssigned(id: String, code: Int?)
}