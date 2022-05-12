package io.github.togar2.blocks.test;

import io.github.togar2.blocks.MinestomBlocks;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public class TestMain {
	public static void main(String[] args) {
		MinecraftServer server = MinecraftServer.init();
		MinestomBlocks.init();
		
		Instance instance = MinecraftServer.getInstanceManager().createInstanceContainer();
		instance.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.STONE));
		instance.enableAutoChunkLoad(true);
		
		Pos spawn = new Pos(0, 42, 0);
		MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent.class, event -> {
			event.setSpawningInstance(instance);
			event.getPlayer().setRespawnPoint(spawn);
		});
		
		MinecraftServer.getGlobalEventHandler().addChild(MinestomBlocks.fallSounds());
		
		MinecraftServer.getCommandManager().register(new GameModeCommand());
		
		server.start("127.0.0.1", 25565);
	}
}
