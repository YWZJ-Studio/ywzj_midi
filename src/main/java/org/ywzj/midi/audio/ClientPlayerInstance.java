package org.ywzj.midi.audio;

import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.ywzj.midi.audio.sound.MusicSound;
import org.ywzj.midi.gui.screen.MusicPlayerScreen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ClientPlayerInstance {

    public boolean portable;
    public UUID deviceUuid;
    public UUID soundUuid;
    public String musicName;
    public String playerName;
    private MusicPlayerScreen musicPlayerScreen;
    private PlayList playList;
    private boolean isRound = false;

    public ClientPlayerInstance() {
        this.portable = false;
        this.deviceUuid = null;
    }

    public ClientPlayerInstance(UUID deviceUuid) {
        this.portable = true;
        this.deviceUuid = deviceUuid;
    }

    public void setScreen(MusicPlayerScreen musicPlayerScreen) {
        this.musicPlayerScreen = musicPlayerScreen;
    }

    public MusicSound getSound() {
        if (soundUuid != null) {
            return MusicPlayer.getInstance(soundUuid);
        }
        return null;
    }

    public boolean playNet(Vec3 pos, String url, String musicName) {
        try {
            soundUuid = UUID.randomUUID();
            return MusicPlayer.playClientNetStream(this, pos, url, musicName, 0, true);
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public boolean playSync(Vec3 pos, String url, String musicName) {
        try {
            soundUuid = UUID.randomUUID();
            return MusicPlayer.playClientSyncStream(this, pos, url, musicName);
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public void playSyncRound(Vec3 pos, List<Path> musicList, Path selectMusic) {
        playList = new PlayList(pos, musicList, selectMusic);
        isRound = true;
        try {
            soundUuid = UUID.randomUUID();
            playList.get().uuid = soundUuid;
            musicName = playList.get().musicName;
            MusicPlayer.playClientSyncStream(this, pos, "file:///" + playList.get().musicPath, playList.get().musicName);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void stop() {
        MusicPlayer.stopStream(soundUuid);
    }

    public void stopSyncRound() {
        isRound = false;
        stop();
    }

    public void callbackSyncPlayEnd() {
        new Thread(() -> {
            if (isRound && !MusicPlayer.getInstance(soundUuid).isKilled()) {
                try {
                    playList.next();
                    soundUuid = UUID.randomUUID();
                    playList.get().uuid = soundUuid;
                    musicName = playList.get().musicName;
                    if (musicPlayerScreen != null) {
                        musicPlayerScreen.callbackPlayNext(playList.get().musicPath);
                    }
                    MusicPlayer.playClientSyncStream(this, playList.pos, "file:///" + playList.get().musicPath, playList.get().musicName);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            } else {
                soundUuid = null;
                musicName = null;
                playerName = null;
                if (musicPlayerScreen != null) {
                    musicPlayerScreen.callbackPlayEnd();
                }
            }
        }).start();
    }

    public boolean isPlaying() {
        boolean result = false;
        if (MusicPlayer.getInstance(soundUuid) != null) {
            result = !MusicPlayer.getInstance(soundUuid).isStopped();
        }
        if (!result) {
            soundUuid = null;
            musicName = null;
            playerName = null;
        }
        return result;
    }

    public static class PlayList {

        public Vec3 pos;
        private final List<Music> musicList = new ArrayList<>();
        private int select = 0;

        public PlayList(Vec3 pos, List<Path> musicList, Path selectMusic) {
            this.pos = pos;
            for (int index = 0; index < musicList.size(); index++) {
                this.musicList.add(new Music(musicList.get(index).getFileName().toString(), musicList.get(index), null));
                if (musicList.get(index).equals(selectMusic)) {
                    this.select = index;
                }
            }
        }

        public void setUuid(UUID uuid) {
            musicList.get(select).uuid = uuid;
        }

        public Music get() {
            return musicList.get(select);
        }

        public Music next() {
            select = (select + 1) % musicList.size();
            return musicList.get(select);
        }

    }

    public static class Music {

        public String musicName;
        public Path musicPath;
        public UUID uuid;

        public Music(String musicName, Path musicPath, UUID uuid) {
            this.musicName = musicName;
            this.musicPath = musicPath;
            this.uuid = uuid;
        }

    }

}
