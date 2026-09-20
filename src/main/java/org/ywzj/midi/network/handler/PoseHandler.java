package org.ywzj.midi.network.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.ywzj.midi.network.Channel;
import org.ywzj.midi.network.message.CPoseData;
import org.ywzj.midi.network.message.SPoseData;
import org.ywzj.midi.pose.PoseManager;

import java.util.function.Supplier;

public class PoseHandler {

    public static void onClientMessageReceived(CPoseData message, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.setPacketHandled(true);
        ctx.enqueueWork(() -> broadcastPose(ctxSupplier.get().getSender(), message));
    }

    public static void broadcastPose(LivingEntity sender, CPoseData message) {
        broadcastPose(sender, message, false);
    }

    public static void broadcastPose(LivingEntity sender, CPoseData message, boolean includeSender) {
        for (ServerPlayer player : sender.level().getServer().getPlayerList().getPlayers()) {
            if (!includeSender && player.equals(sender)) {
                continue;
            }
            if (player.level().dimension().equals(sender.level().dimension()) && player.distanceTo(sender) < 64) {
                Channel.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SPoseData(message));
            }
        }
    }

    public static void onServerMessageReceived(SPoseData message, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.setPacketHandled(true);
        ctx.enqueueWork(() -> processServerMessage(ctxSupplier.get().getSender(), message));
    }

    private static void processServerMessage(ServerPlayer sender, SPoseData message) {
        PoseManager.push(message.playerUuid, message.toPlayPose());
    }

}
