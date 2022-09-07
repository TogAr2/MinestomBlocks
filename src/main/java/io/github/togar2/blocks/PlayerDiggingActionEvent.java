package io.github.togar2.blocks;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class PlayerDiggingActionEvent implements PlayerEvent, EntityInstanceEvent, BlockEvent {
	private final Player player;
	private final Block block;
	private final Point blockPosition;
	private final ClientPlayerDiggingPacket.Status status;
	
	private Handler handler;
	
	public PlayerDiggingActionEvent(Player player, Block block, Point blockPosition, ClientPlayerDiggingPacket.Status status) {
		this.player = player;
		this.block = block;
		this.blockPosition = blockPosition;
		this.status = status;
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
	
	public ClientPlayerDiggingPacket.Status getStatus() {
		return status;
	}
	
	/**
	 * Gets the handler for this action.
	 *
	 * @return the handler for this action
	 */
	public Handler getHandler() {
		return handler;
	}
	
	/**
	 * Sets the handler for this action
	 *
	 * @param handler the handler for this action
	 */
	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	
	@FunctionalInterface
	public interface Handler {
		void handle(Player player, Instance instance, Point blockPosition);
	}
}
