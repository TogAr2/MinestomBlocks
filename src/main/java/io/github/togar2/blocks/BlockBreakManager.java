package io.github.togar2.blocks;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerStartDiggingEvent;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.gamedata.tags.TagManager;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket;
import net.minestom.server.network.packet.server.play.AcknowledgePlayerDiggingPacket;
import net.minestom.server.network.packet.server.play.BlockBreakAnimationPacket;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

class BlockBreakManager {
	private boolean mining;
	private int miningStartTime;
	private Point miningPos = Vec.ZERO;
	private int tickCounter;
	private boolean failedToMine;
	private Point failedMiningPos = Vec.ZERO;
	private int failedMiningStartTime;
	private byte blockBreakingStage = -1;
	
	public void update(Player player) {
		tickCounter++;
		
		Instance instance = player.getInstance();
		assert instance != null;
		if (failedToMine) {
			Block block = instance.getBlock(failedMiningPos);
			if (block.isAir()) {
				failedToMine = false;
			} else {
				double progress = continueMining(player, block, failedMiningPos, failedMiningStartTime);
				if (progress >= 1.0f) {
					failedToMine = false;
					tryBreakBlock(failedMiningPos);
				}
			}
		} else if (mining) {
			Block block = instance.getBlock(miningPos);
			if (block.isAir()) {
				blockBreakingStage = -1;
				mining = false;
				player.sendPacketToViewers(new BlockBreakAnimationPacket(player.getEntityId(), miningPos, blockBreakingStage));
			} else {
				continueMining(player, block, miningPos, miningStartTime);
			}
		}
	}
	
	public CustomPlayerDiggingListener.DiggingResult startDigging(Player player, Instance instance, Point blockPosition) {
		final Block block = instance.getBlock(blockPosition);
		final GameMode gameMode = player.getGameMode();
		
		// Prevent spectators and check players in adventure mode
		if (shouldPreventBreaking(player, block)) return new CustomPlayerDiggingListener.DiggingResult(block, false);
		if (gameMode == GameMode.CREATIVE) return breakBlock(instance, player, blockPosition, block);
		
		// Survival digging
		miningStartTime = tickCounter;
		double delta = 1;
		if (!block.isAir()) {
			delta = getBlockBreakingProgress(player, block);
		}
		if (player.isInstantBreak() || (!block.isAir() && delta >= 1)) {
			// Instant break
			return breakBlock(instance, player, blockPosition, block);
		} else {
			if (mining) {
				// Cancel previous
				player.sendPacket(new AcknowledgePlayerDiggingPacket(blockPosition, instance.getBlock(miningPos),
						ClientPlayerDiggingPacket.Status.STARTED_DIGGING, false));
			}
			
			mining = true;
			miningPos = blockPosition;
			blockBreakingStage = (byte) (delta * 10);
			player.sendPacketToViewers(new BlockBreakAnimationPacket(player.getEntityId(), miningPos, blockBreakingStage));
			
			PlayerStartDiggingEvent playerStartDiggingEvent = new PlayerStartDiggingEvent(player, block, blockPosition);
			EventDispatcher.call(playerStartDiggingEvent);
			return new CustomPlayerDiggingListener.DiggingResult(block, !playerStartDiggingEvent.isCancelled());
		}
	}
	
	public CustomPlayerDiggingListener.DiggingResult finishDigging(Player player, Instance instance, Point blockPosition) {
		final Block block = instance.getBlock(blockPosition);
		
		if (block.isAir() || !blockPosition.equals(miningPos) || shouldPreventBreaking(player, block))
			return new CustomPlayerDiggingListener.DiggingResult(block, false);
		
		int ticksMining = tickCounter - miningStartTime;
		double progress = getBlockBreakingProgress(player, block) * (ticksMining + 1);
		if (progress > 0.7) {
			mining = false;
			player.sendPacketToViewers(new BlockBreakAnimationPacket(player.getEntityId(), miningPos, (byte) -1));
			return breakBlock(instance, player, blockPosition, block);
		}
		if (!failedToMine) {
			mining = false;
			failedToMine = true;
			failedMiningPos = blockPosition;
			failedMiningStartTime = miningStartTime;
		}
		
		return new CustomPlayerDiggingListener.DiggingResult(block, false);
	}
	
	public CustomPlayerDiggingListener.DiggingResult cancelDigging(Player player, Instance instance, Point blockPosition) {
		mining = false;
		player.sendPacketToViewers(new BlockBreakAnimationPacket(player.getEntityId(), blockPosition, (byte) -1));
		
		final Block block = instance.getBlock(blockPosition);
		return new CustomPlayerDiggingListener.DiggingResult(block, true);
	}
	
	private boolean shouldPreventBreaking(@NotNull Player player, Block block) {
		if (player.getGameMode() == GameMode.SPECTATOR) {
			// Spectators can't break blocks
			return true;
		} else if (player.getGameMode() == GameMode.ADVENTURE) {
			// Check if the item can break the block with the current item
			final ItemStack itemInMainHand = player.getItemInMainHand();
			return !itemInMainHand.meta().getCanDestroy().contains(block);
		}
		
		return false;
	}
	
	private CustomPlayerDiggingListener.DiggingResult breakBlock(Instance instance,
	                                                              Player player,
	                                                              Point blockPosition, Block previousBlock) {
		// Unverified block break, client is fully responsible
		final boolean success = instance.breakBlock(player, blockPosition);
		final Block updatedBlock = instance.getBlock(blockPosition);
		if (!success) {
			if (previousBlock.isSolid()) {
				final Pos playerPosition = player.getPosition();
				// Teleport the player back if he broke a solid block just below him
				if (playerPosition.sub(0, 1, 0).samePoint(blockPosition)) {
					player.teleport(playerPosition);
				}
			}
		}
		return new CustomPlayerDiggingListener.DiggingResult(updatedBlock, success);
	}
	
	private double continueMining(Player player, Block block, Point pos, int startTime) {
		int ticksMining = tickCounter - startTime;
		double progress = getBlockBreakingProgress(player, block) * (double) (ticksMining + 1);
		byte stage = (byte) (progress * 10);
		if (stage != blockBreakingStage) {
			player.sendPacketToViewers(new BlockBreakAnimationPacket(player.getEntityId(), pos, stage));
			blockBreakingStage = stage;
		}
		return progress;
	}
	
	private double getBlockBreakingProgress(Player player, Block block) {
		double hardness = block.registry().hardness();
		if (hardness == -1 || hardness == 0) return 0;
		int division = (!MinestomBlocks.requiresTool(block)
				|| isItemEffective(player.getItemInMainHand().material(), block)) ? 30 : 100;
		return getBlockBreakingSpeed(player, block) / hardness / (double) division;
	}
	
	private double getBlockBreakingSpeed(Player player, Block block) {
		ItemStack stack = player.getItemInMainHand();
		double breakingSpeed = getMiningSpeedMultiplier(stack.material(), block);
		
		Map<Enchantment, Short> enchantmentMap = stack.meta().getEnchantmentMap();
		if (breakingSpeed > 1) {
			Short eff = enchantmentMap.get(Enchantment.EFFICIENCY);
			if (eff == null) eff = 0;
			if (eff > 0 && !stack.isAir()) {
				breakingSpeed += eff * eff + 1;
			}
		}
		
		byte haste = -1;
		byte miningFatigue = -1;
		for (TimedPotion potion : player.getActiveEffects()) {
			if (potion.getPotion().effect() == PotionEffect.HASTE) {
				haste = potion.getPotion().amplifier();
			} else if (potion.getPotion().effect() == PotionEffect.MINING_FATIGUE) {
				miningFatigue = potion.getPotion().amplifier();
			}
		}
		
		if (haste > -1) breakingSpeed *= 1 + (haste + 1) * 0.2;
		if (miningFatigue > -1) {
			breakingSpeed *= switch (miningFatigue) {
				case 0 -> 0.3;
				case 1 -> 0.09;
				case 2 -> 0.0027;
				default -> 8.1E-4f;
			};
		}
		
		if (!player.isOnGround()) breakingSpeed /= 5.0;
		
		return breakingSpeed;
	}
	
	private double getMiningSpeedMultiplier(Material material, Block block) {
		ToolTier tier = MinestomBlocks.getToolTier(material);
		if (tier == null) return 1.0;
		
		if (material.name().contains("sword")) return block.compare(Block.COBWEB) ? 15.0f : 1.0f;
		if (material.name().contains("shears")) {
			if (block.compare(Block.COBWEB) || block.name().contains("leaves")) {
				return 15.0;
			} else if (block.name().contains("wool")) {
				return 5.0;
			} else if (block.compare(Block.VINE) || block.compare(Block.GLOW_LICHEN)) {
				return 2.0;
			} else {
				return 1.0;
			}
		}
		
		return getEffectiveTag(material).contains(block.namespace()) ? tier.getMiningSpeed() : 1.0;
	}
	
	private boolean isItemEffective(Material material, Block block) {
		ToolTier tier = MinestomBlocks.getToolTier(material);
		if (tier == null) return false;
		
		if (material.name().contains("sword")) return block.compare(Block.COBWEB);
		if (material.name().contains("shears")) return block.compare(Block.COBWEB) || block.compare(Block.REDSTONE_WIRE) || block.compare(Block.TRIPWIRE);
		
		int miningLevel = tier.getMiningLevel();
		TagManager tags = MinecraftServer.getTagManager();
		if (miningLevel < ToolTier.DIAMOND.getMiningLevel() && Objects.requireNonNull(tags.getTag(Tag.BasicType.BLOCKS, "minecraft:needs_diamond_tool")).contains(block.namespace())) return false;
		if (miningLevel < ToolTier.IRON.getMiningLevel() && Objects.requireNonNull(tags.getTag(Tag.BasicType.BLOCKS, "minecraft:needs_iron_tool")).contains(block.namespace())) return false;
		if (miningLevel < ToolTier.STONE.getMiningLevel() && Objects.requireNonNull(tags.getTag(Tag.BasicType.BLOCKS, "minecraft:needs_stone_tool")).contains(block.namespace())) return false;
		
		return getEffectiveTag(material).contains(block.namespace());
	}
	
	private Tag getEffectiveTag(Material material) {
		TagManager tags = MinecraftServer.getTagManager();
		if (material.name().contains("pickaxe")) return Objects.requireNonNull(tags.getTag(Tag.BasicType.BLOCKS, "minecraft:mineable/pickaxe"));
		if (material.name().contains("axe")) return Objects.requireNonNull(tags.getTag(Tag.BasicType.BLOCKS, "minecraft:mineable/axe"));
		if (material.name().contains("hoe")) return Objects.requireNonNull(tags.getTag(Tag.BasicType.BLOCKS, "minecraft:mineable/hoe"));
		else return Objects.requireNonNull(tags.getTag(Tag.BasicType.BLOCKS, "minecraft:mineable/shovel"));
	}
	
	private void tryBreakBlock(Point failedMiningPoint) {
	
	}
}
