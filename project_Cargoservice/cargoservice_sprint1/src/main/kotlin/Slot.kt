package main.kotlin

data class Slot(
    override val id: String,
    override val container: IContainer?
) : ISlot