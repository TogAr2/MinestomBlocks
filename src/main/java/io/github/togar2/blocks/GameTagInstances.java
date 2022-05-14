package io.github.togar2.blocks;

import net.minestom.server.MinecraftServer;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.gamedata.tags.TagManager;

class GameTagInstances {
	public static Tag NEEDS_DIAMOND_TOOL;
	public static Tag NEEDS_IRON_TOOL;
	public static Tag NEEDS_STONE_TOOL;
	
	public static Tag MINEABLE_PICKAXE;
	public static Tag MINEABLE_AXE;
	public static Tag MINEABLE_HOE;
	public static Tag MINEABLE_SHOVEL;
	
	public static Tag INSIDE_STEP_SOUND;
	
	public static Tag FENCES;
	public static Tag WALLS;
	public static Tag FENCE_GATES;
	
	public static void init() {
		TagManager tags = MinecraftServer.getTagManager();
		
		NEEDS_DIAMOND_TOOL = tags.getTag(Tag.BasicType.BLOCKS, "minecraft:needs_diamond_tool");
		NEEDS_IRON_TOOL = tags.getTag(Tag.BasicType.BLOCKS, "minecraft:needs_iron_tool");
		NEEDS_STONE_TOOL = tags.getTag(Tag.BasicType.BLOCKS, "minecraft:needs_stone_tool");
		
		MINEABLE_PICKAXE = tags.getTag(Tag.BasicType.BLOCKS, "minecraft:mineable/pickaxe");
		MINEABLE_AXE = tags.getTag(Tag.BasicType.BLOCKS, "minecraft:mineable/axe");
		MINEABLE_HOE = tags.getTag(Tag.BasicType.BLOCKS, "minecraft:mineable/hoe");
		MINEABLE_SHOVEL = tags.getTag(Tag.BasicType.BLOCKS, "minecraft:mineable/shovel");
		
		INSIDE_STEP_SOUND = tags.getTag(Tag.BasicType.BLOCKS, "minecraft:inside_step_sound_blocks");
		
		FENCES = tags.getTag(Tag.BasicType.BLOCKS, "minecraft:fences");
		WALLS = tags.getTag(Tag.BasicType.BLOCKS, "minecraft:walls");
		FENCE_GATES = tags.getTag(Tag.BasicType.BLOCKS, "minecraft:fence_gates");
	}
}
