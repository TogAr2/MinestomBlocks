package io.github.togar2.blocks;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class BreakManagerEvent implements PlayerEvent, EntityInstanceEvent, BlockEvent {
	private final Player player;
	private final Block block;
	private final Point blockPosition;
	
	private boolean delegateManager;
	
	public BreakManagerEvent(Player player, Block block, Point blockPosition) {
		this.player = player;
		this.block = block;
		this.blockPosition = blockPosition;
	}
	
	@Override
	public @NotNull Block getBlock() {
		return block;
	}
	
	public @NotNull Point getBlockPosition() {
		return blockPosition;
	}
	
	@Override
	public @NotNull Player getPlayer() {
		return player;
	}
	
	/**
	 * Gets whether MinestomBlocks' block break manager will be used (makes block break animation work)
	 *
	 * @return whether MinestomBlocks' block break manager will be used
	 */
	public boolean delegateManager() {
		return delegateManager;
	}
	
	/**
	 * Sets whether MinestomBlocks' block break manager should be used (makes block break animation work)
	 *
	 * @param delegateManager whether MinestomBlocks' block break manager should be used
	 */
	public void setDelegateManager(boolean delegateManager) {
		this.delegateManager = delegateManager;
	}
}
