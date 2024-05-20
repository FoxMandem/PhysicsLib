package me.foxmandem.shape

import net.minestom.server.coordinate.Vec

//Source: EmortalDev (?)

data class Quad(
    val point1: Vec,
    val point2: Vec,
    val point3: Vec,
    val point4: Vec
) {
    fun triangles(): List<Triangle> {
        return listOf(
            Triangle(point1, point2, point3),
            Triangle(point3, point4, point1)
        )
    }
}