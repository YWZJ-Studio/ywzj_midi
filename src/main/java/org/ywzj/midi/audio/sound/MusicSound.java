package org.ywzj.midi.audio.sound;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.ywzj.midi.all.AllConfigs;
import org.ywzj.midi.util.ParticleUtils;

public class MusicSound extends SimpleSoundInstance implements TickableSoundInstance {

    private boolean isPlaying;
    private boolean isKilled;
    private Long tickCount;
    private Vec3 playPos;

    public MusicSound(SoundEvent event, float volume, float pitch, RandomSource source, Vec3 pos) {
        super(event, SoundSource.RECORDS, volume, pitch, source, pos.x, pos.y, pos.z);
        this.isPlaying = true;
        this.tickCount = 0L;
        this.playPos = pos;
        updateRelativePos();
    }

    public void updatePos(Vec3 pos) {
        this.playPos = pos;
    }

    public void stop() {
        isPlaying = false;
    }

    public void kill() {
        stop();
        isKilled = true;
    }

    public Vec3 getPlayPos() {
        return playPos;
    }

    @Override
    public boolean isStopped() {
        return !isPlaying;
    }

    public boolean isKilled() {
        return isKilled;
    }

    @Override
    public void tick() {
        updateRelativePos();
        if (tickCount % 2 == 0) {
            ParticleUtils.addNoteParticle(playPos);
        }
        tickCount += 1;
    }

    private void updateRelativePos() {
        double distanceMultiplier = AllConfigs.common.soundDistanceMultiplier.get();
        Vec3 simulatedPos = MidiSound.calRelativePos(playPos, 1.0d / distanceMultiplier);
        this.x = simulatedPos.x;
        this.y = simulatedPos.y;
        this.z = simulatedPos.z;
    }

}
