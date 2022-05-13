package io.github.togar2.blocks;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.listener.PlayerDiggingListener;
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket;
import net.minestom.server.network.packet.server.play.AcknowledgePlayerDiggingPacket;

class CustomPlayerDiggingListener {
	public static void listener(ClientPlayerDiggingPacket packet, Player player) {
		final ClientPlayerDiggingPacket.Status status = packet.status();
		if (status != ClientPlayerDiggingPacket.Status.STARTED_DIGGING
				&& status != ClientPlayerDiggingPacket.Status.CANCELLED_DIGGING
				&& status != ClientPlayerDiggingPacket.Status.FINISHED_DIGGING) {
			PlayerDiggingListener.playerDiggingListener(packet, player); // Delegate to minestom default
			return;
		}
		
		final Point blockPosition = packet.blockPosition();
		final Instance instance = player.getInstance();
		if (instance == null) return;
		
		final Block block = instance.getBlock(blockPosition);
		BreakManagerEvent breakManagerEvent = new BreakManagerEvent(player, block, blockPosition);
		EventDispatcher.call(breakManagerEvent);
		if (!breakManagerEvent.delegateManager()) {
			PlayerDiggingListener.playerDiggingListener(packet, player); // Delegate to minestom default
			return;
		}
		
		final BlockBreakManager manager = BreakAnimation.getManager(player);
		
		DiggingResult diggingResult;
		if (status == ClientPlayerDiggingPacket.Status.STARTED_DIGGING) {
			if (!instance.isChunkLoaded(blockPosition)) return;
			diggingResult = manager.startDigging(player, instance, blockPosition);
		} else if (status == ClientPlayerDiggingPacket.Status.CANCELLED_DIGGING) {
			if (!instance.isChunkLoaded(blockPosition)) return;
			diggingResult = manager.cancelDigging(player, instance, blockPosition);
		} else { // Finished digging
			if (!instance.isChunkLoaded(blockPosition)) return;
			diggingResult = manager.finishDigging(player, instance, blockPosition);
		}
		
		// Acknowledge start/cancel/finish digging status
		if (diggingResult != null) {
			player.sendPacket(new AcknowledgePlayerDiggingPacket(blockPosition, diggingResult.block,
					status, diggingResult.success));
		}
	}
	
	public record DiggingResult(Block block, boolean success) {
	}
}
