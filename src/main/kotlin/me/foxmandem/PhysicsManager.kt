package me.foxmandem

import com.jme3.bullet.PhysicsSpace
import kotlinx.coroutines.NonCancellable.cancel
import me.foxmandem.thread.PhysicsThread
import net.minestom.server.collision.BoundingBox
import net.minestom.server.collision.ShapeImpl
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block

class PhysicsManager(val instance: Instance) {

    companion object {
        private val managers: MutableList<PhysicsManager> = mutableListOf()
        private val cacheIsFull = mutableMapOf<Block, Boolean>()
        private val cacheBoundingBox = mutableMapOf<ShapeImpl, Array<BoundingBox>>()

        internal fun getOrCreate(instance: Instance): PhysicsManager {
            val manager = managers.firstOrNull { it.instance == instance }
            if(manager == null)
                managers.add(PhysicsManager((instance)))
            return managers.first { it.instance == instance}
        }

        internal fun Block.isFull(): Boolean {
            return cacheIsFull.getOrPut(this) { isFullBlock() }
        }

        private fun Block.isFullBlock(): Boolean {
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
        internal fun ShapeImpl.collisionBoundingBoxes(): Array<BoundingBox> {
            //cache bounding boxes or add to cache and return
            return cacheBoundingBox.getOrPut(this) { collisionBoundingBoxesField.get(this) as Array<BoundingBox> }
        }
    }

    lateinit var space: PhysicsSpace

    val isInitialized get() = this::space.isInitialized

    val physicsThread = PhysicsThread(this::isInitialized.getter, this::update, 20)

    init {
        physicsThread.execute {
            space = PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT)
        }

        managers.add(this)
    }

    private fun update(timeInterval: Float) {
        physicsThread.execute {
            space.update(timeInterval)
        }
    }

    fun getPhysicsSpace() : PhysicsSpace {
        return space
    }

    fun remove() {
        cancel()
        managers.remove(this)
        physicsThread.destroy()
    }

}