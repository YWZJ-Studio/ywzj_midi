package org.ywzj.midi.audio.sound;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.ywzj.midi.audio.stream.ClientBufferStream;
import org.ywzj.midi.util.ComponentUtils;

import java.util.concurrent.CompletableFuture;

public class ClientBufferMusicSound extends MusicSound {

    private final ClientBufferStream stream;

    public ClientBufferMusicSound(SoundEvent event, float volume, float pitch, RandomSource source, Vec3 pos, float sampleRate, float frameRate, String musicName) {
        super(event, volume, pitch, source, pos);
        this.stream = new ClientBufferStream(this, sampleRate, frameRate);
        Minecraft.getInstance().gui.setNowPlaying(ComponentUtils.literal(musicName));
    }

    public void updatePosAndStream(Vec3 pos, byte[] bytes) {
        updatePos(pos);
        stream.writeBytes(bytes);
    }

    @Override
    public CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers, Sound sound, boolean looping) {
        return CompletableFuture.supplyAsync(() -> stream, Util.backgroundExecutor());
    }

}
