package io.github.togar2.blocks;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class MinestomBlocks {
	private static final Logger LOGGER = LoggerFactory.getLogger(MinestomBlocks.class);
	private static final Map<Block, BlockSoundGroup> BLOCK_SOUND_GROUPS = new HashMap<>();
	private static final Map<Block, Boolean> REQUIRES_TOOL = new HashMap<>();
	private static final Map<Material, ToolTier> TOOL_TIER = new HashMap<>();
	
	public static @NotNull BlockSoundGroup getSoundGroup(@NotNull Block block) {
		return BLOCK_SOUND_GROUPS.getOrDefault(block, BlockSoundGroup.STONE);
	}
	
	public static boolean requiresTool(@NotNull Block block) {
		return REQUIRES_TOOL.getOrDefault(block, true);
	}
	
	public static @Nullable ToolTier getToolTier(@NotNull Material material) {
		return TOOL_TIER.get(material);
	}
	
	public static EventNode<EntityEvent> fallSounds() {
		return FallSounds.node();
	}
	
	public static EventNode<PlayerEvent> placeSounds() {
		return PlaceSounds.node();
	}
	
	public static EventNode<PlayerEvent> moveSounds() {
		return MoveSounds.node();
	}
	
	public static EventNode<PlayerEvent> breakAnimation() {
		return BreakAnimation.node();
	}
	
	public static void init() {
		MinecraftServer.getPacketListenerManager().setListener(ClientPlayerDiggingPacket.class, CustomPlayerDiggingListener::listener);
		
		String blockPath = "/generated-block-groups.txt";
		String itemPath = "/generated-item-tiers.txt";
		try (
				InputStream blockStream = MinestomBlocks.class.getResourceAsStream(blockPath);
				InputStream itemStream = MinestomBlocks.class.getResourceAsStream(itemPath)
		) {
		//try (InputStream stream = new FileInputStream(path)) {
			if (blockStream == null) throw new IOException("Could not find resource '" + blockPath + "'");
			if (itemStream == null) throw new IOException("Could not find resource '" + itemPath + "'");
			
			// ITEMS
			BufferedReader reader = new BufferedReader(new InputStreamReader(itemStream));
			
			String line = reader.readLine();
			while (line != null) {
				String[] parts = line.split(" ");
				if (parts.length != 2) {
					LOGGER.error("Failed to read item tier '{}'", line);
					continue;
				}
				
				TOOL_TIER.put(Material.fromNamespaceId(parts[0]), ToolTier.valueOf(parts[1]));
				line = reader.readLine();
			}
			
			// BLOCKS
			reader = new BufferedReader(new InputStreamReader(blockStream));
			
			line = reader.readLine();
			lineLoop: while (line != null) {
				String[] parts = line.split(" ");
				if (parts.length != 4) {
					LOGGER.error("Failed to read block group '{}'", line);
					continue;
				}
				
				String namespace = parts[0];
				String propertiesString = parts[1];
				String groupFieldName = parts[2];
				String requiresToolString = parts[3];
				
				Map<String, String> properties = new HashMap<>();
				String[] propertiesArray = propertiesString.split(",");
				for (String property : propertiesArray) {
					if (property.isEmpty()) continue;
					String[] keyValue = property.split(":");
					if (keyValue.length != 2) {
						LOGGER.error("Failed to read block property '{}'", property);
						continue lineLoop;
					}
					
					properties.put(keyValue[0], keyValue[1]);
				}
				
				Block block = Block.fromNamespaceId(namespace);
				if (block == null) {
					LOGGER.error("Unknown block '{}'", namespace);
					continue;
				}
				block = block.withProperties(properties);
				BlockSoundGroup group = (BlockSoundGroup) BlockSoundGroup.class.getField(groupFieldName).get(null);
				
				BLOCK_SOUND_GROUPS.put(block, group);
				REQUIRES_TOOL.put(block, Boolean.valueOf(requiresToolString));
				
				line = reader.readLine();
			}
		} catch (IOException | NoSuchFieldException | IllegalAccessException e) {
			LOGGER.error("Failed to read block groups from file, all sounds will be stone", e);
		}
	}
}
