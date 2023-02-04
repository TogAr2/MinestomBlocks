package io.github.togar2.blocks;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.listener.PlayerDiggingListener;
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket;
import net.minestom.server.network.packet.server.play.AcknowledgeBlockChangePacket;

@SuppressWarnings("UnstableApiUsage")
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
		if (instance == null || !instance.isChunkLoaded(blockPosition)) return;
		
		final Block block = instance.getBlock(blockPosition);
		PlayerDiggingActionEvent playerDiggingActionEvent = new PlayerDiggingActionEvent(player, block, blockPosition, status);
		EventDispatcher.call(playerDiggingActionEvent);
		if (playerDiggingActionEvent.getHandler() == null) {
			PlayerDiggingListener.playerDiggingListener(packet, player); // Delegate to minestom default
			return;
		}
		
		playerDiggingActionEvent.getHandler().handle(player, instance, blockPosition, packet.blockFace());
		
		// Acknowledge start/cancel/finish digging status
		player.sendPacket(new AcknowledgeBlockChangePacket(packet.sequence()));
	}
}
