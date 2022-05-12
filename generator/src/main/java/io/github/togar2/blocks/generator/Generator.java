package io.github.togar2.blocks.generator;

import com.google.gson.*;
import io.github.togar2.blocks.BlockSoundGroup;
import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEvent;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Generator {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	public static void main(String[] args) throws IOException {
		Files.deleteIfExists(Path.of("./generated-block-groups.txt"));
		
		try (
				InputStream stream = Generator.class.getResourceAsStream("/1_18_2_blocks.json");
				OutputStream outputStream = new FileOutputStream("./generated-block-groups.txt");
				OutputStreamWriter writer = new OutputStreamWriter(outputStream)
		) {
			if (stream == null) {
				System.err.println("Resource is null");
				return;
			}
			
			JsonObject json = GSON.fromJson(new InputStreamReader(stream), JsonObject.class);
			json.entrySet().forEach(entry -> {
				String namespace = entry.getKey();
				
				if (Block.fromNamespaceId(namespace) == null) {
					System.err.println("Could not find block " + namespace);
					return;
				}
				
				JsonObject blockObject = entry.getValue().getAsJsonObject();
				JsonArray states = blockObject.getAsJsonArray("states");
				
				for (JsonElement element : states) {
					JsonObject propertiesObject = element.getAsJsonObject().getAsJsonObject("properties");
					JsonObject sounds = element.getAsJsonObject().getAsJsonObject("sounds");
					
					Map<String, String> properties = new HashMap<>();
					propertiesObject.keySet().forEach(property -> properties.put(property,
							propertiesObject.get(property).getAsString().toLowerCase(Locale.ROOT)));
					
					String breakSoundId = sounds.get("breakSound").getAsString();
					String stepSoundId = sounds.get("stepSound").getAsString();
					String fallSoundId = sounds.get("fallSound").getAsString();
					String placeSoundId = sounds.get("placeSound").getAsString();
					String hitSoundId = sounds.get("hitSound").getAsString();
					float pitch = sounds.get("pitch").getAsFloat();
					float volume = sounds.get("volume").getAsFloat();
					
					SoundEvent breakSound = SoundEvent.fromNamespaceId("block." + breakSoundId.toLowerCase(Locale.ROOT)
							.replaceAll("_break", ".break"));
					SoundEvent stepSound = SoundEvent.fromNamespaceId("block." + stepSoundId.toLowerCase(Locale.ROOT)
							.replaceAll("_step", ".step"));
					SoundEvent fallSound = SoundEvent.fromNamespaceId("block." + fallSoundId.toLowerCase(Locale.ROOT)
							.replaceAll("_fall", ".fall"));
					SoundEvent placeSound = SoundEvent.fromNamespaceId("block." + placeSoundId.toLowerCase(Locale.ROOT)
							.replaceAll("_place", ".place"));
					SoundEvent hitSound = SoundEvent.fromNamespaceId("block." + hitSoundId.toLowerCase(Locale.ROOT)
							.replaceAll("_hit", ".hit"));
					
					if (placeSound == null && placeSoundId.endsWith("_PLANTED")) {
						// Planted are the only exception
						placeSound = SoundEvent.fromNamespaceId("item." + placeSoundId.toLowerCase(Locale.ROOT)
								.replaceAll("_planted", ".plant"));
					}
					
					if (breakSound == null || stepSound == null || fallSound == null || placeSound == null || hitSound == null) {
						System.err.println("Could not find sounds: " + breakSoundId + " " + stepSoundId + " "
								+ fallSoundId + " " + placeSoundId + " " + hitSoundId);
						continue;
					}
					
					BlockSoundGroup group = new BlockSoundGroup(volume, pitch, breakSound, stepSound,
							placeSound, hitSound, fallSound);
					if (BlockSoundGroup.STONE.equals(group)) continue; // Stone is the default
					
					String groupFieldName = null;
					try {
						for (Field field : BlockSoundGroup.class.getFields()) {
							BlockSoundGroup blockSoundGroup = (BlockSoundGroup) field.get(null);
							if (blockSoundGroup.equals(group)) {
								groupFieldName = field.getName();
							}
						}
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
					if (groupFieldName == null) {
						System.err.println("Could not find group for sounds: " + breakSoundId + " " + stepSoundId + " "
								+ fallSoundId + " " + placeSoundId + " " + hitSoundId);
						continue;
					}
					
					StringBuilder propertyString = new StringBuilder();
					properties.forEach((property, value) -> propertyString
							.append(property).append(":")
							.append(value).append(","));
					if (!properties.isEmpty()) propertyString.deleteCharAt(propertyString.length() - 1);
					
					try {
						writer.write(namespace + " " + propertyString + " " + groupFieldName + "\n");
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			});
		}
	}
	
}
