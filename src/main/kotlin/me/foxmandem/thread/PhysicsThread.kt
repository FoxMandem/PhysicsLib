package me.foxmandem.thread

import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executor

//Source: EmortalDev (?)

class PhysicsThread(
    private val isInitialized: () -> Boolean,
    private val updateTask: (timeInterval: Float) -> Unit,
    private val tps: Int = 20,
) : Thread(), Executor {
    private var running = true

    private val tasks: Queue<Runnable> = ConcurrentLinkedQueue()

    private var lastUpdate: Long = System.currentTimeMillis()

    private var lastMspt = 0.0
        private set

    init {
        name = "PhysicsThread"
        start()
    }

    override fun run() {

        val minTickTime = 1000 / tps

        while (running) {
            try {
                val nanoTime = System.nanoTime()
                val currentTimeMillis = System.currentTimeMillis()
                val tickTime = currentTimeMillis - lastUpdate

                if (tickTime < minTickTime) continue

                while (!tasks.isEmpty()) {
                    try {
                        tasks.poll().run()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (isInitialized()) {
                    updateTask(tickTime / 1000f)
                }
                lastUpdate = currentTimeMillis
                lastMspt = (System.nanoTime() - nanoTime) / 1_000_000.0
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun execute(task: Runnable) {
        tasks.add(task)
    }

    fun destroy() {
        running = false
        try {
            this.join(5000) // 5 second timeout
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}