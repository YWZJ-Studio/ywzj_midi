package org.ywzj.midi.network.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.ywzj.midi.audio.MusicPlayer;
import org.ywzj.midi.audio.ServerSoundManager;
import org.ywzj.midi.network.message.CSyncMusic;
import org.ywzj.midi.network.message.SSyncMusic;


public class SyncMusicHandler {

    public static void onClientMessageReceived(CSyncMusic message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> processClientMessage((ServerPlayer) ctx.player(), message));
    }

    private static void processClientMessage(ServerPlayer sender, CSyncMusic message) {
        ServerSoundManager.dispatch(sender, message);
    }

    public static void onServerMessageReceived(SSyncMusic message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> processServerMessage(message));
    }

    private static void processServerMessage(SSyncMusic message) {
        if (message.on) {
            MusicPlayer.playOtherClientStream(message.soundUuid, new Vec3(message.x, message.y, message.z), message.sampleRate, message.frameRate, message.musicName, message.bytes);
        } else {
            MusicPlayer.stopStream(message.soundUuid);
        }
    }

}
