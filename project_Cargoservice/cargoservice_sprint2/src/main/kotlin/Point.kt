package main.kotlin

data class Point(
    override val x: Int,
    override val y: Int,
    override val direction: String?
): IPoint