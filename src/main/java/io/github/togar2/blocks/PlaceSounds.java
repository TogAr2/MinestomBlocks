package io.github.togar2.blocks;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.trait.PlayerEvent;

class PlaceSounds {
	public static EventNode<PlayerEvent> node() {
		EventNode<PlayerEvent> node = EventNode.type("blocks-place-sound", EventFilter.PLAYER);
		
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
