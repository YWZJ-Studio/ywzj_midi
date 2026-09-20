package org.ywzj.midi.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.ywzj.midi.all.AllSounds;
import org.ywzj.midi.audio.sound.ClientBufferMusicSound;
import org.ywzj.midi.audio.sound.ClientNetMusicSound;
import org.ywzj.midi.audio.sound.ClientSyncMusicSound;
import org.ywzj.midi.audio.sound.MusicSound;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class MusicPlayer {

    public static final HashMap<UUID, MusicSound> MUSIC_INSTANCES = new HashMap<>();

    public static MusicSound getInstance(UUID uuid) {
        return MUSIC_INSTANCES.get(uuid);
    }

    public static void stopStream(UUID uuid) {
        if (MUSIC_INSTANCES.get(uuid) != null) {
            MUSIC_INSTANCES.get(uuid).kill();
            MUSIC_INSTANCES.remove(uuid);
        }
    }

    public static boolean playClientNetStream(ClientPlayerInstance playerInstance, Vec3 pos, String url, String musicName, int readOffset, boolean isHost) throws MalformedURLException {
        MusicSound soundInstance = MUSIC_INSTANCES.get(playerInstance.soundUuid);
        if ((soundInstance == null || soundInstance.isStopped() && !soundInstance.isKilled())) {
            ClientNetMusicSound instance = new ClientNetMusicSound(AllSounds.MUSIC.get(),
                    2f,
                    1f,
                    SoundInstance.createUnseededRandom(),
                    pos,
                    new URL(url),
                    musicName,
                    readOffset,
                    isHost,
                    playerInstance);
            MUSIC_INSTANCES.put(playerInstance.soundUuid, instance);
            if (instance.stream != null) {
                Minecraft.getInstance().submitAsync(() -> Minecraft.getInstance().getSoundManager().play(instance));
                return true;
            }
        } else if (playerInstance.portable) {
            ((ClientNetMusicSound) MUSIC_INSTANCES.get(playerInstance.soundUuid)).updatePos(pos);
            return true;
        }
        return false;
    }

    public static boolean playClientSyncStream(ClientPlayerInstance playerInstance, Vec3 pos, String url, String musicName) throws MalformedURLException {
        ClientSyncMusicSound soundInstance = new ClientSyncMusicSound(AllSounds.MUSIC.get(),
                2f,
                1f,
                SoundInstance.createUnseededRandom(),
                pos,
                new URL(url),
                musicName,
                playerInstance);
        if (soundInstance.stream != null) {
            Minecraft.getInstance().submitAsync(() -> Minecraft.getInstance().getSoundManager().play(soundInstance));
            MUSIC_INSTANCES.put(soundInstance.soundUuid, soundInstance);
            return true;
        }
        return false;
    }

    public static void playOtherClientStream(UUID uuid, Vec3 pos, float sampleRate, float frameRate, String musicName, byte[] bytes) {
        MusicSound musicInstance = MUSIC_INSTANCES.get(uuid);
        if ((musicInstance == null || musicInstance.isStopped() && !musicInstance.isKilled())) {
            ClientBufferMusicSound instance = new ClientBufferMusicSound(AllSounds.MUSIC.get(),
                    2f,
                    1f,
                    SoundInstance.createUnseededRandom(),
                    pos,
                    sampleRate,
                    frameRate,
                    musicName);
            instance.updatePosAndStream(pos, bytes);
            Minecraft.getInstance().submitAsync(() -> Minecraft.getInstance().getSoundManager().play(instance));
            MUSIC_INSTANCES.put(uuid, instance);
        } else {
            if (musicInstance instanceof ClientBufferMusicSound) {
                ((ClientBufferMusicSound) musicInstance).updatePosAndStream(pos, bytes);
            } else if (musicInstance instanceof ClientSyncMusicSound) {
                ((ClientSyncMusicSound) musicInstance).updatePos(pos);
            }
        }
    }

}
