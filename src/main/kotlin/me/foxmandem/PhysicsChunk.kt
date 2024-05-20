package me.foxmandem

import com.jme3.bullet.collision.shapes.MeshCollisionShape
import com.jme3.bullet.collision.shapes.infos.IndexedMesh
import com.jme3.bullet.objects.PhysicsRigidBody
import kotlinx.coroutines.*
import me.foxmandem.PhysicsManager.Companion.getOrCreate
import me.foxmandem.convert.jme
import me.foxmandem.shape.Face
import net.minestom.server.collision.BoundingBox
import net.minestom.server.collision.ShapeImpl
import net.minestom.server.coordinate.Vec
import net.minestom.server.instance.DynamicChunk
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.instance.block.BlockHandler
import net.minestom.server.instance.heightmap.Heightmap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import kotlin.math.abs

class PhysicsChunk(
    instance: Instance,
    chunkX: Int,
    chunkZ: Int
) : DynamicChunk(instance, chunkX, chunkZ) {
    private var isLoaded = false
    private var isRecent = false
    private val mesh: MutableList<Pair<Int, PhysicsRigidBody>> = mutableListOf()

    private val executor = Executors.newFixedThreadPool(abs(getMinSection()) + getMaxSection())
    private val scope = CoroutineScope(executor.asCoroutineDispatcher())

    override fun onLoad() {
        isLoaded = true
    }

    override fun onGenerate() {
        super.onGenerate()
        generateMesh()
    }

    private fun generateMesh() {
        for(i in getMinSection() until Heightmap.getHighestBlockSection(this) / 16) {
            generateSection(i)
        }
    }

    override fun setBlock(
        x: Int,
        y: Int,
        z: Int,
        block: Block,
        placement: BlockHandler.Placement?,
        destroy: BlockHandler.Destroy?
    ) {
        super.setBlock(x, y, z, block, placement, destroy)

        val sectionInt = y / 16

        val obj = mesh.firstOrNull { it.first == sectionInt }
        if(obj != null) {
            getPhysics().space.removeCollisionObject(obj.second)
            mesh.remove(obj)
        }

        generateSection(sectionInt)
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
        val job = scope.launch {
            val faces = ConcurrentLinkedQueue<Face>()

            getFaces(sectionInt, faces)

            val vertices = triangulate(faces.toList())

            if (vertices.isEmpty()) return@launch

            val mesh = IndexedMesh(vertices.map(Vec::jme).toTypedArray(), IntArray(vertices.size) { it })
            val shape = MeshCollisionShape(true, mesh)
            val body = PhysicsRigidBody(shape, 0f)

            this@PhysicsChunk.mesh.add(Pair(sectionInt, body))

            getPhysics().space.addCollisionObject(body)
            isRecent = true
        }
        job.invokeOnCompletion {
            job.cancel()
        }

    }

    private fun getFaces(sectionInt: Int, faces: ConcurrentLinkedQueue<Face>) : List<Face> {
        val section = getSection(sectionInt)

        section.blockPalette().getAll { x, y, z, value ->
            val block = Block.fromStateId(value.toShort())
            if (block != null) {
                if (!block.isAir || !block.isLiquid) {
                    val blockY = sectionInt * 16 + y
                    val neighbors = mutableListOf(
                        getBlock(x + 1, blockY, z),
                        getBlock(x, blockY + 1, z),
                        getBlock(x, blockY, z + 1),

                        getBlock(x - 1, blockY, z),
                        getBlock(x, blockY, z - 1),
                    )

                    if(blockY != instance.dimensionType.minY) {
                        neighbors.add(getBlock(x, blockY - 1, z))
                    }

                    var occluded = true;
                    for (neighbor in neighbors) {
                        if (neighbor.isAir || neighbor.isLiquid) occluded = false
                    }

                    if (!occluded) {
                        getBlockFaces(x, blockY, z, block, faces)
                    }
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

        boxes.forEach {
            getFaces(x, blockY, z, it, faces)
        }
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

    private fun Block.isFull(): Boolean {
        if (isAir || isLiquid) return false
        val shape = registry().collisionShape() as ShapeImpl
        val boxes = shape.collisionBoundingBoxes()
        if (boxes.size != 1) return false
        val box = boxes[0]
        return box.minX() == 0.0 && box.minY() == 0.0 && box.minZ() == 0.0 && box.maxX() == 1.0 && box.maxY() == 1.0 && box.maxZ() == 1.0
    }

    private val collisionBoundingBoxesField =
        ShapeImpl::class.java.getDeclaredField("collisionBoundingBoxes").apply { isAccessible = true }

    @Suppress("UNCHECKED_CAST")
    private fun ShapeImpl.collisionBoundingBoxes(): Array<BoundingBox> {
        return collisionBoundingBoxesField.get(this) as Array<BoundingBox>
    }

    private fun getPhysics(): PhysicsManager {
        return getOrCreate(instance)
    }

}