package me.foxmandem.convert

import me.foxmandem.shape.Hitbox
import me.foxmandem.shape.Quad
import net.minestom.server.collision.BoundingBox
import net.minestom.server.coordinate.Vec

//Source: EmortalDev (?)

object PhysUtils {

    fun BoundingBox.getQuads(): List<Quad> {
        return listOf(
            Quad(
                Vec(minX(), maxY(), minZ()),
                Vec(maxX(), maxY(), minZ()),
                Vec(maxX(), minY(), minZ()),
                Vec(minX(), minY(), minZ())
            ),
            Quad(
                Vec(maxX(), maxY(), minZ()),
                Vec(maxX(), maxY(), maxZ()),
                Vec(maxX(), minY(), maxZ()),
                Vec(maxX(), minY(), minZ())
            ),
            Quad(
                Vec(maxX(), maxY(), maxZ()),
                Vec(minX(), maxY(), maxZ()),
                Vec(minX(), minY(), maxZ()),
                Vec(maxX(), minY(), maxZ())
            ),
            Quad(
                Vec(minX(), maxY(), maxZ()),
                Vec(minX(), maxY(), minZ()),
                Vec(minX(), minY(), minZ()),
                Vec(minX(), minY(), maxZ())
            ),
            Quad(
                Vec(maxX(), maxY(), minZ()),
                Vec(minX(), maxY(), minZ()),
                Vec(minX(), maxY(), maxZ()),
                Vec(maxX(), maxY(), maxZ())
            ),
            Quad(
                Vec(maxX(), minY(), maxZ()),
                Vec(minX(), minY(), maxZ()),
                Vec(minX(), minY(), minZ()),
                Vec(maxX(), minY(), minZ())
            )
        )
    }

    fun Hitbox.getQuads(): List<Quad> {
        return listOf(
            Quad(
                Vec(min.x, max.y, min.z),
                Vec(max.x, max.y, min.z),
                Vec(max.x, min.y, min.z),
                Vec(min.x, min.y, min.z)
            ),
            Quad(
                Vec(max.x, max.y, min.z),
                Vec(max.x, max.y, max.z),
                Vec(max.x, min.y, max.z),
                Vec(max.x, min.y, min.z)
            ),
            Quad(
                Vec(max.x, max.y, max.z),
                Vec(min.x, max.y, max.z),
                Vec(min.x, min.y, max.z),
                Vec(max.x, min.y, max.z)
            ),
            Quad(
                Vec(min.x, max.y, max.z),
                Vec(min.x, max.y, min.z),
                Vec(min.x, min.y, min.z),
                Vec(min.x, min.y, max.z)
            ),
            Quad(
                Vec(max.x, max.y, min.z),
                Vec(min.x, max.y, min.z),
                Vec(min.x, max.y, max.z),
                Vec(max.x, max.y, max.z)
            ),
            Quad(
                Vec(max.x, min.y, max.z),
                Vec(min.x, min.y, max.z),
                Vec(min.x, min.y, min.z),
                Vec(max.x, min.y, min.z)
            )
        )
    }
}