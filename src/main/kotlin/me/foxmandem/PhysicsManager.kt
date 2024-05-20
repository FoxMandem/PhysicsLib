package me.foxmandem

import com.jme3.bullet.PhysicsSpace
import kotlinx.coroutines.NonCancellable.cancel
import me.foxmandem.space.PhysicsThread
import net.minestom.server.instance.Instance

class PhysicsManager(val instance: Instance) {

    companion object {
        private val managers: MutableList<PhysicsManager> = mutableListOf()

        internal fun getOrCreate(instance: Instance): PhysicsManager {
            val manager = managers.firstOrNull { it.instance == instance }
            if(manager == null)
                managers.add(PhysicsManager((instance)))
            return managers.first { it.instance == instance}
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