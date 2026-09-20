package org.ywzj.midi.network.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.ywzj.midi.all.AllInstruments;
import org.ywzj.midi.audio.NotePlayer;
import org.ywzj.midi.network.Channel;
import org.ywzj.midi.network.message.CPlayNote;
import org.ywzj.midi.network.message.SPlayNote;
import org.ywzj.midi.util.MathUtils;


public class PlayNoteHandler {

    public static void onClientMessageReceived(CPlayNote message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> broadcastNote((ServerPlayer) ctx.player(), message, true));
    }

    public static void broadcastNote(LivingEntity sender, CPlayNote message, boolean fromClient) {
        for (ServerPlayer player : sender.level().getServer().getPlayerList().getPlayers()) {
            if (fromClient && player.equals(sender)) {
                continue;
            }
            if (!message.on) {
                Channel.CHANNEL.sendToPlayer(player, new SPlayNote(message));
            } else {
                if (player.level().dimension().equals(sender.level().dimension())
                        && MathUtils.distance(player.getX(), player.getY(), player.getZ(), message.x, message.y, message.z) < 64) {
                    Channel.CHANNEL.sendToPlayer(player, new SPlayNote(message));
                }
            }
        }
    }

    public static void onServerMessageReceived(SPlayNote message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> processServerMessage(message));
    }

    private static void processServerMessage(SPlayNote message) {
        if (message.on) {
            NotePlayer.playClientNote(message.uuid, new Vec3(message.x, message.y, message.z), AllInstruments.fromId(message.instrumentId), message.variantId, message.note, message.velocity, message.delay);
        } else {
            NotePlayer.stopClientNote(message.uuid);
        }
    }

}
