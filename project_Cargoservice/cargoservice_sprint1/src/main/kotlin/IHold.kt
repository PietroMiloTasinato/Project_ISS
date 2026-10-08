package main.kotlin

interface IHold {

    val home: IPoint
    val ioPort: Pair<IPoint, IContainer>
    val marker: IPoint
    val holdMap: Map<IPoint, ISlot>

}