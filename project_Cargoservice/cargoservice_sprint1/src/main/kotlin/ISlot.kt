package main.kotlin

interface ISlot {
    val id: String
    var status: Status
    val point: IPoint

    enum class Status {
        FREE, BOOKED, OCCUPIED
    }
}