import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.system.NativeLibraryLoader
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.foxmandem.PhysicsChunk
import me.foxmandem.PhysicsManager.Companion.getOrCreate
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.instance.AnvilLoader
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.generator.GenerationUnit
import net.minestom.server.utils.NamespaceID
import net.minestom.server.world.DimensionType
import java.io.File
import java.util.logging.Filter
import java.util.logging.Level
import java.util.logging.LogRecord


@OptIn(DelicateCoroutinesApi::class)
fun main() {
    val minecraftServer = MinecraftServer.init()
    val instanceManager = MinecraftServer.getInstanceManager()

    NativeLibraryLoader.loadLibbulletjme(true, File("natives/"), "Release", "Sp");

    val dimension = DimensionType.builder(NamespaceID.from("light"))
        .ambientLight(15f)
        .build()

    MinecraftServer.getDimensionTypeManager().addDimension(dimension)


    val instanceContainer = instanceManager.createInstanceContainer(dimension)
    getOrCreate(instanceContainer)

    instanceContainer.setGenerator { unit: GenerationUnit ->
        unit.modifier().fillHeight(-64, 64, Block.GRASS_BLOCK)
    }

    instanceContainer.setChunkSupplier(::PhysicsChunk)
    PhysicsRigidBody.logger2.filter = Filter { record: LogRecord ->
        if (record.level === Level.INFO && record.message.startsWith("Created")) {
            return@Filter false
        }
        if (record.level === Level.INFO && record.message.startsWith("Clearing")) {
            return@Filter false
        }
        if (record.level === Level.INFO && record.message.startsWith("Substituted")) {
            return@Filter false
        }
        true
    }

    for (x in 0 until 16) {
        for (z in 0 until 16) {
            instanceContainer.loadChunk(x,z)
        }
    }

    val globalEventHandler = MinecraftServer.getGlobalEventHandler()
    globalEventHandler.addListener(
        AsyncPlayerConfigurationEvent::class.java
    ) { event: AsyncPlayerConfigurationEvent ->
        val player: Player = event.player
        event.spawningInstance = instanceContainer
        player.respawnPoint = Pos(0.0, 70.0, 0.0)
        player.gameMode = GameMode.CREATIVE
    }

    globalEventHandler.addListener(PlayerSpawnEvent::class.java) {
        it.player.teleport(Pos(0.0, 70.0, 0.0))
    }


    // Start the server on port 25565
    minecraftServer.start("0.0.0.0", 25565)
}