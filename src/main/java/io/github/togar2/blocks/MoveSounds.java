package io.github.togar2.blocks;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;

import java.util.Objects;

class MoveSounds {
	private static final Tag<Double> DISTANCE_TRAVELED = Tag.Double("distance-traveled");
	
	public static EventNode<PlayerEvent> node() {
		EventNode<PlayerEvent> node = EventNode.type("blocks-move-sounds", EventFilter.PLAYER);
		
		node.addListener(PlayerMoveEvent.class, event -> {
			Player player = event.getPlayer();
			Instance instance = player.getInstance();
			assert instance != null;
			
			double distanceTraveled = player.hasTag(DISTANCE_TRAVELED) ? player.getTag(DISTANCE_TRAVELED) : 0;
			distanceTraveled += event.getNewPosition().sub(event.getPlayer().getPosition()).asVec().length() * 0.6;
			
			if (distanceTraveled > 1) {
				Point landingPos = getLandingPos(player, event.getNewPosition());
				Block inside = instance.getBlock(landingPos.add(0, 1, 0));
				net.minestom.server.gamedata.tags.Tag tag = MinecraftServer.getTagManager()
						.getTag(net.minestom.server.gamedata.tags.Tag.BasicType.BLOCKS, "minecraft:inside_step_sound_blocks");
				assert tag != null;
				Block stepBlock = tag.contains(inside.namespace()) ? inside : instance.getBlock(landingPos);
				if (!stepBlock.isAir()) {
					BlockSoundGroup group = MinestomBlocks.getSoundGroup(stepBlock);
					player.getViewersAsAudience().playSound(Sound.sound(
							group.stepSound(),
							Sound.Source.PLAYER,
							group.volume() * 0.15f,
							group.pitch()
					), player);
					
					distanceTraveled = 0;
				}
			}
			
			player.setTag(DISTANCE_TRAVELED, distanceTraveled);
		});
		
		return node;
	}
	
	private static Point getLandingPos(Player player, Pos position) {
		position = position.add(0, -0.2, 0);
		if (Objects.requireNonNull(player.getInstance()).getBlock(position).isAir()) {
			Pos other = position.add(0, -1, 0);
			Block block = player.getInstance().getBlock(other);
			net.minestom.server.gamedata.tags.Tag fences = MinecraftServer.getTagManager()
					.getTag(net.minestom.server.gamedata.tags.Tag.BasicType.BLOCKS, "minecraft:fences");
			net.minestom.server.gamedata.tags.Tag walls = MinecraftServer.getTagManager()
					.getTag(net.minestom.server.gamedata.tags.Tag.BasicType.BLOCKS, "minecraft:walls");
			net.minestom.server.gamedata.tags.Tag fenceGates = MinecraftServer.getTagManager()
					.getTag(net.minestom.server.gamedata.tags.Tag.BasicType.BLOCKS, "minecraft:fence_gates");
			assert fences != null;
			assert walls != null;
			assert fenceGates != null;
			if (fences.contains(block.namespace()) || walls.contains(block.namespace()) || fenceGates.contains(block.namespace())) {
				return other;
			}
		}
		
		return position;
	}
}
