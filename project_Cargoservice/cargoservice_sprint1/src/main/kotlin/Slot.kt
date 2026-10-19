package main.kotlin

data class Slot(
    override val id: String,
    override var container: IContainer?,
    override val point: IPoint
) : ISlot