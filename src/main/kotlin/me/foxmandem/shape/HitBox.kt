package me.foxmandem.shape

import net.minestom.server.coordinate.Vec
import kotlin.math.abs

//Source: EmortalDev (?)

class Hitbox private constructor(
    size: Vec,
    center: Vec
) {
    val min: Vec = center.sub(size.mul(0.5))
    val max: Vec = center.add(size.mul(0.5))

    companion object {
        fun fromSize(size: Vec): Hitbox {
            return Hitbox(size, Vec.ZERO)
        }

        fun fromMinAndMax(min: Vec, max: Vec): Hitbox {
            val center = max.add(min).mul(0.5)
            val xSize = abs(max.x - min.x)
            val ySize = abs(max.x - min.x)
            val zSize = abs(max.z - min.z)
            return Hitbox(center, Vec(xSize, ySize, zSize))
        }

        fun fromSizeAndCenter(size: Vec, center: Vec): Hitbox {
            return Hitbox(size, center)
        }
    }
}
