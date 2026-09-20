package org.ywzj.midi.network.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.ywzj.midi.audio.NotePlayer;
import org.ywzj.midi.network.Channel;
import org.ywzj.midi.network.message.CChangeNote;
import org.ywzj.midi.network.message.SChangeNote;


public class ChangeNoteHandler {

    public static void onClientMessageReceived(CChangeNote message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> broadcastNote((ServerPlayer) ctx.player(), message, true));
    }

    public static void broadcastNote(LivingEntity sender, CChangeNote message, boolean fromClient) {
        for (ServerPlayer player : sender.level().getServer().getPlayerList().getPlayers()) {
            if (fromClient && player.equals(sender)) {
                continue;
            }
            Channel.CHANNEL.sendToPlayer(player, new SChangeNote(message));
        }
    }

    public static void onServerMessageReceived(SChangeNote message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> processServerMessage(message));
    }

    private static void processServerMessage(SChangeNote message) {
        NotePlayer.changeClientNote(message.uuid, message.velScale);
    }

}
