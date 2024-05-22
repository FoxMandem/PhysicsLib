package me.foxmandem

import com.jme3.bullet.collision.shapes.MeshCollisionShape
import com.jme3.bullet.collision.shapes.infos.IndexedMesh
import com.jme3.bullet.objects.PhysicsRigidBody
import me.foxmandem.PhysicsManager.Companion.collisionBoundingBoxes
import me.foxmandem.PhysicsManager.Companion.getOrCreate
import me.foxmandem.PhysicsManager.Companion.isFull
import me.foxmandem.convert.jme
import me.foxmandem.shape.Face
import net.minestom.server.collision.BoundingBox
import net.minestom.server.collision.ShapeImpl
import net.minestom.server.coordinate.Vec
import net.minestom.server.instance.DynamicChunk
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.instance.heightmap.Heightmap
import java.util.concurrent.ConcurrentLinkedQueue

class PhysicsChunk(
    instance: Instance,
    chunkX: Int,
    chunkZ: Int
) : DynamicChunk(instance, chunkX, chunkZ) {

    private var isLoaded = false
    private var isRecent = false

    override fun onLoad() {
        isLoaded = true
    }

    override fun onGenerate() {
        super.onGenerate()
        generateMesh()
    }

    fun generateMesh() {
        for(i in getMinSection() until Heightmap.getHighestBlockSection(this) / 16) {
            generateSection(i)
        }
    }

    private fun triangulate(faces: List<Face>): List<Vec> {
        val vertices = arrayListOf<Vec>()

        faces.forEach { face ->
            face.triangles().forEach { triangle ->
                vertices.add(triangle.point1)
                vertices.add(triangle.point2)
                vertices.add(triangle.point3)
            }
        }

        return vertices;
    }

    private fun generateSection(sectionInt: Int) {
        val vertices = triangulate(getFaces(sectionInt, ConcurrentLinkedQueue<Face>()))

        if (vertices.isEmpty()) return

        val mesh = IndexedMesh(vertices.map(Vec::jme).toTypedArray(), IntArray(vertices.size) { it })
        val shape = MeshCollisionShape(true, mesh)
        getPhysics().space.addCollisionObject(PhysicsRigidBody(shape, 0f))

        isRecent = true
    }

    private fun getFaces(sectionInt: Int, faces: ConcurrentLinkedQueue<Face>) : List<Face> {
        val section = getSection(sectionInt)
        val palette = section.blockPalette()

        palette.getAll { x, y, z, value ->
            if (value != 0) {
                val blockY = sectionInt * 16 + y

                var occluded = true;

                if(getBlock(x + 1, blockY, z).isAir) occluded = false
                if(getBlock(x, blockY + 1, z).isAir && occluded) occluded = false
                if(getBlock(x, blockY, z + 1).isAir && occluded) occluded = false

                if(getBlock(x - 1, blockY, z).isAir && occluded) occluded = false
                if(getBlock(x, blockY, z - 1).isAir && occluded) occluded = false

                if(blockY != instance.dimensionType.minY && occluded) {
                    if(getBlock(x, blockY - 1, z).isAir) occluded = false
                }

                if (!occluded) {
                    getBlockFaces(x, blockY, z, Block.fromStateId(value.toShort())!!, faces)
                }
            }
        }

        return faces.toList()
    }

    private fun getBlockFaces(
        x: Int,
        blockY: Int,
        z: Int,
        block: Block,
        faces: ConcurrentLinkedQueue<Face>,
    ) {
        val shape = block.registry().collisionShape() as ShapeImpl
        val boxes = shape.collisionBoundingBoxes()
        //Should only have one box
        getFaces(x, blockY, z, boxes[0], faces)
    }

    private fun getFaces(
        x: Int,
        blockY: Int,
        z: Int,
        box: BoundingBox,
        faces: ConcurrentLinkedQueue<Face>
    ) {
        val blockX = chunkX * 16 + x
        val blockZ = chunkZ * 16 + z

        BlockFace.entries.forEach { blockFace ->
            val face = Face(
                blockFace,
                if (blockFace == BlockFace.EAST) box.maxX() else box.minX(),
                if (blockFace == BlockFace.TOP) box.maxY() else box.minY(),
                if (blockFace == BlockFace.SOUTH) box.maxZ() else box.minZ(),
                if (blockFace == BlockFace.WEST) box.minX() else box.maxX(),
                if (blockFace == BlockFace.BOTTOM) box.minY() else box.maxY(),
                if (blockFace == BlockFace.NORTH) box.minZ() else box.maxZ(),
                blockX,
                blockY,
                blockZ
            )

            val dir = blockFace.toDirection()
            val neighbourBlock = getBlock(x + dir.normalX(), blockY + dir.normalY(), z + dir.normalZ())

            if (!neighbourBlock.isFull() || !face.isEdge()) {
                faces.add(face)
            }
        }
    }

    private fun getPhysics(): PhysicsManager {
        return getOrCreate(instance)
    }

}