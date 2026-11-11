package main.kotlin

data class Slot(
    override val id: String,
    override var status: ISlot.Status,
    override val point: IPoint
) : ISlot