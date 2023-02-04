package io.github.togar2.blocks;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

class FallSounds {
	@SuppressWarnings("UnstableApiUsage")
	public static EventNode<EntityInstanceEvent> node() {
		EventNode<EntityInstanceEvent> node = EventNode.type("block-fall-sounds", MinestomBlocks.ENTITY_INSTANCE_FILTER);
		
		node.addListener(EntityDamageEvent.class, event -> {
			if (event.getDamageType().getIdentifier().contains("fall")
					|| event.getDamageType().getIdentifier().contains("stalagmite")) {
				Entity entity = event.getEntity();
				if (entity.isSilent()) return;
				Instance instance = entity.getInstance();
				assert instance != null;
				
				Block block = instance.getBlock(entity.getPosition().sub(0, 0.2, 0));
				if (!block.isAir()) {
					BlockSoundGroup group = MinestomBlocks.getSoundGroup(block);
					entity.getViewersAsAudience().playSound(Sound.sound(
							group.fallSound(),
							Sound.Source.PLAYER,
							group.volume() * 0.5f,
							group.pitch() * 0.75f
					), entity);
				}
			}
		});
		
		return node;
	}
}
