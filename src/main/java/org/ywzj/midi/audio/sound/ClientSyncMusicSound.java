package org.ywzj.midi.audio.sound;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.ywzj.midi.audio.ClientPlayerInstance;
import org.ywzj.midi.audio.stream.ClientSyncMp3Stream;
import org.ywzj.midi.network.Channel;
import org.ywzj.midi.network.message.CSyncMusic;
import org.ywzj.midi.util.ComponentUtils;

import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ClientSyncMusicSound extends MusicSound {

    public final ClientPlayerInstance playerInstance;
    public AudioStream stream;
    public final URL url;
    public final String musicName;
    public final UUID soundUuid;
    public final UUID deviceUuid;

    public ClientSyncMusicSound(SoundEvent event, float volume, float pitch, RandomSource source, Vec3 pos, URL url, String musicName, ClientPlayerInstance playerInstance) {
        super(event, volume, pitch, source, pos);
        this.playerInstance = playerInstance;
        this.url = url;
        this.musicName = musicName;
        this.soundUuid = playerInstance.soundUuid;
        this.deviceUuid = playerInstance.deviceUuid;
        try {
            this.stream = new ClientSyncMp3Stream(this);
        } catch (Exception exception) {
            exception.printStackTrace();
            playerInstance.callbackSyncPlayEnd();
            super.stop();
        }
        Minecraft.getInstance().gui.setNowPlaying(ComponentUtils.literal(musicName));
    }

    public void stopThis() {
        super.stop();
        Channel.CHANNEL.sendToServer(new CSyncMusic(soundUuid));
    }

    @Override
    public CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers, Sound sound, boolean looping) {
        return CompletableFuture.supplyAsync(() -> stream, Util.backgroundExecutor());
    }

}
