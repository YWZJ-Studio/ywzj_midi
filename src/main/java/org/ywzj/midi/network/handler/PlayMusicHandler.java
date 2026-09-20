package org.ywzj.midi.network.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.ywzj.midi.audio.ClientPlayerInstance;
import org.ywzj.midi.audio.MusicPlayer;
import org.ywzj.midi.audio.ServerSoundManager;
import org.ywzj.midi.network.message.CPlayMusic;
import org.ywzj.midi.network.message.SPlayMusic;


public class PlayMusicHandler {

    public static void onClientMessageReceived(CPlayMusic message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> processClientMessage((ServerPlayer) ctx.player(), message));
    }

    private static void processClientMessage(ServerPlayer sender, CPlayMusic message) {
        ServerSoundManager.dispatch(sender, message);
    }

    public static void onServerMessageReceived(SPlayMusic message, IPayloadContext ctx) {
        processServerMessage(message);
    }

    private static void processServerMessage(SPlayMusic message) {
        if (message.on) {
            try {
                ClientPlayerInstance playerInstance = new ClientPlayerInstance();
                playerInstance.soundUuid = message.soundUuid;
                playerInstance.deviceUuid = message.soundUuid;
                playerInstance.portable = message.portable;
                MusicPlayer.playClientNetStream(playerInstance, new Vec3(message.x, message.y, message.z), message.url, message.musicName, message.offset, false);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        } else {
            MusicPlayer.stopStream(message.soundUuid);
        }
    }

}
