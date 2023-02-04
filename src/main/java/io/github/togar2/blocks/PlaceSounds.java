package io.github.togar2.blocks;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;

class PlaceSounds {
	@SuppressWarnings("UnstableApiUsage")
	public static EventNode<PlayerInstanceEvent> node() {
		EventNode<PlayerInstanceEvent> node = EventNode.type("blocks-place-sound", MinestomBlocks.PLAYER_INSTANCE_FILTER);
		
		node.addListener(PlayerBlockPlaceEvent.class, event -> {
			BlockSoundGroup group = MinestomBlocks.getSoundGroup(event.getBlock());
			event.getPlayer().getViewersAsAudience().playSound(
					Sound.sound(
							group.placeSound(),
							Sound.Source.BLOCK,
							(group.volume() + 1.0f) / 2.0f,
							group.pitch() * 0.8f
					),
					event.getBlockPosition().x() + 0.5,
					event.getBlockPosition().y() + 0.5,
					event.getBlockPosition().z() + 0.5
			);
		});
		
		return node;
	}
}
