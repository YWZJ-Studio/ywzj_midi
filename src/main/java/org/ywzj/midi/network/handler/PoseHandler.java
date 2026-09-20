package org.ywzj.midi.network.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.ywzj.midi.network.Channel;
import org.ywzj.midi.network.message.CPoseData;
import org.ywzj.midi.network.message.SPoseData;
import org.ywzj.midi.pose.PoseManager;


public class PoseHandler {

    public static void onClientMessageReceived(CPoseData message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> broadcastPose((ServerPlayer) ctx.player(), message));
    }

    public static void broadcastPose(LivingEntity sender, CPoseData message) {
        for (ServerPlayer player : sender.level().getServer().getPlayerList().getPlayers()) {
            if (player.equals(sender)) {
                continue;
            }
            if (player.level().dimension().equals(sender.level().dimension()) && player.distanceTo(sender) < 64) {
                Channel.CHANNEL.sendToPlayer(player, new SPoseData(message));
            }
        }
    }

    public static void onServerMessageReceived(SPoseData message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> processServerMessage(message));
    }

    private static void processServerMessage(SPoseData message) {
        PoseManager.push(message.playerUuid, message.toPlayPose());
    }

}
