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
import org.ywzj.midi.audio.stream.ClientMp3Stream;
import org.ywzj.midi.network.Channel;
import org.ywzj.midi.network.message.CPlayMusic;
import org.ywzj.midi.util.ComponentUtils;

import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ClientNetMusicSound extends MusicSound {

    public final ClientPlayerInstance playerInstance;
    public AudioStream stream;
    public final URL url;
    public final String musicName;
    public final int readOffset;
    public final boolean isHost;
    public final UUID soundUuid;
    public final UUID deviceUuid;

    public ClientNetMusicSound(SoundEvent event, float volume, float pitch, RandomSource source, Vec3 pos, URL url, String musicName, int readOffset, boolean isHost, ClientPlayerInstance playerInstance) {
        super(event, volume, pitch, source, pos);
        this.playerInstance = playerInstance;
        this.url = url;
        this.musicName = musicName;
        this.readOffset = readOffset;
        this.isHost= isHost;
        this.soundUuid = playerInstance.soundUuid;
        this.deviceUuid = playerInstance.deviceUuid;
        try {
            this.stream = new ClientMp3Stream(this);
        } catch (Exception exception) {
            exception.printStackTrace();
            super.stop();
        }
        Minecraft.getInstance().gui.setNowPlaying(ComponentUtils.literal(musicName));
    }

    @Override
    public void stop() {
        super.stop();
        Channel.CHANNEL.sendToServer(new CPlayMusic(soundUuid));
    }

    @Override
    public CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers, Sound sound, boolean looping) {
        return CompletableFuture.supplyAsync(() -> stream, Util.backgroundExecutor());
    }

}
