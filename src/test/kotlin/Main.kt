import com.jme3.system.NativeLibraryLoader
import me.foxmandem.PhysicsManager.Companion.getOrCreate
import me.foxmandem.PhysicsChunk
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.generator.GenerationUnit
import net.minestom.server.utils.NamespaceID
import net.minestom.server.world.DimensionType
import net.minestom.server.world.DimensionTypeManager
import java.io.File

fun main() {
    val minecraftServer = MinecraftServer.init()
    val instanceManager = MinecraftServer.getInstanceManager()

    NativeLibraryLoader.loadLibbulletjme(true, File("natives/"), "Release", "Sp");

    val dimension = DimensionType.builder(NamespaceID.from("light"))
        .ambientLight(15f).build()

    MinecraftServer.getDimensionTypeManager().addDimension(dimension)

    val instanceContainer = instanceManager.createInstanceContainer(dimension)
    getOrCreate(instanceContainer)

    instanceContainer.setGenerator { unit: GenerationUnit ->
        unit.modifier().fillHeight(-64, 64, Block.GRASS_BLOCK)
    }

    instanceContainer.setChunkSupplier(::PhysicsChunk)

    instanceContainer.loadChunk(0,0)
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