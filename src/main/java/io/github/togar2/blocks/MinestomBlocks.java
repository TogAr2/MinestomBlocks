package io.github.togar2.blocks;

import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class MinestomBlocks {
	private static final Logger LOGGER = LoggerFactory.getLogger(MinestomBlocks.class);
	private static final Map<Block, BlockSoundGroup> BLOCK_SOUND_GROUPS = new HashMap<>();
	
	public static @NotNull BlockSoundGroup getSoundGroup(@NotNull Block block) {
		return BLOCK_SOUND_GROUPS.getOrDefault(block, BlockSoundGroup.STONE);
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
	
	public static void init() {
		String path = "./generated-block-groups.txt";
		//try (InputStream stream = MinestomBlocks.class.getResourceAsStream(path)) {
		try (InputStream stream = new FileInputStream(path)) {
			if (stream == null) throw new IOException("Could not find resource '" + path + "'");
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			
			String line = reader.readLine();
			lineLoop: while (line != null) {
				String[] parts = line.split(" ");
				if (parts.length != 3) {
					LOGGER.error("Failed to read block group '{}'", line);
					continue;
				}
				
				String namespace = parts[0];
				String propertiesString = parts[1];
				String groupFieldName = parts[2];
				
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
				
				line = reader.readLine();
			}
		} catch (IOException | NoSuchFieldException | IllegalAccessException e) {
			LOGGER.error("Failed to read block groups from file, all sounds will be stone", e);
		}
	}
}
