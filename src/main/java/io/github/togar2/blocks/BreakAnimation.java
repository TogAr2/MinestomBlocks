package io.github.togar2.blocks;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.event.trait.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class BreakAnimation {
	private static final Map<UUID, BlockBreakManager> BLOCK_BREAK_MANAGER = new HashMap<>();
	
	public static BlockBreakManager getManager(Player player) {
		return BLOCK_BREAK_MANAGER.computeIfAbsent(player.getUuid(), k -> new BlockBreakManager());
	}
	
	public static EventNode<PlayerEvent> node() {
		EventNode<PlayerEvent> node = EventNode.type("blocks-breaking", EventFilter.PLAYER);
		
		node.addListener(PlayerTickEvent.class, event -> getManager(event.getPlayer()).update(event.getPlayer()));
		node.addListener(BreakManagerEvent.class, event -> event.setDelegateManager(true));
		
		return node;
	}
}
