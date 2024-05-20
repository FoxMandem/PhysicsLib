package me.foxmandem.shape

import net.minestom.server.coordinate.Vec
import net.minestom.server.instance.block.BlockFace

//Source: EmortalDev (?)

data class Face(
    val blockFace: BlockFace,
    val minX: Double,
    val minY: Double,
    val minZ: Double,
    val maxX: Double,
    val maxY: Double,
    val maxZ: Double,
    val blockX: Int,
    val blockY: Int,
    val blockZ: Int,
) {
    private val blockPos = Vec(blockX.toDouble(), blockY.toDouble(), blockZ.toDouble())
    fun isEdge(): Boolean = when (blockFace) {
        BlockFace.BOTTOM -> minY == 0.0
        BlockFace.TOP -> maxY == 1.0
        BlockFace.NORTH -> minZ == 0.0
        BlockFace.SOUTH -> maxZ == 1.0
        BlockFace.WEST -> minX == 0.0
        BlockFace.EAST -> maxX == 1.0
    }

    fun toQuad(): Quad {
        return when (blockFace) {
            BlockFace.TOP, BlockFace.BOTTOM -> Quad(
                Vec(minX, maxY, minZ).add(blockPos),
                Vec(maxX, maxY, minZ).add(blockPos),
                Vec(maxX, maxY, maxZ).add(blockPos),
                Vec(minX, maxY, maxZ).add(blockPos)
            )

            BlockFace.EAST, BlockFace.WEST -> Quad(
                Vec(maxX, minY, minZ).add(blockPos),
                Vec(maxX, maxY, minZ).add(blockPos),
                Vec(maxX, maxY, maxZ).add(blockPos),
                Vec(maxX, minY, maxZ).add(blockPos)
            )

            BlockFace.NORTH, BlockFace.SOUTH -> Quad(
                Vec(minX, minY, minZ).add(blockPos),
                Vec(maxX, minY, minZ).add(blockPos),
                Vec(maxX, maxY, minZ).add(blockPos),
                Vec(minX, maxY, minZ).add(blockPos)
            )
        }
    }

    fun triangles() = toQuad().triangles()
}