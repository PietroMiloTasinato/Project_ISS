package main.kotlin

interface ISlot {
    val id: String
    var container: IContainer?
    val point: IPoint
}