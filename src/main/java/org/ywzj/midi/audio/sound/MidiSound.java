package org.ywzj.midi.audio.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.ywzj.midi.all.AllConfigs;

public class MidiSound extends SimpleSoundInstance implements TickableSoundInstance {

    private final static Integer FADE_IN_TICKS = 2;
    private final boolean loop;
    private final Vec3 notePos;
    private float volumeFin;
    private boolean on;
    private Integer life = 600;
    private boolean isPlaying;
    private boolean isFadeIn;
    private Integer fadeInTick = 0;

    public MidiSound(SoundEvent event, float volume, float pitch, boolean loop, Vec3 pos) {
        super(event, SoundSource.BLOCKS, volume, pitch, SoundInstance.createUnseededRandom(), 0, 0, 0);
        this.loop = loop;
        this.notePos = pos;
        this.volume = loop ? Math.min(0.001f, volume) : volume;
        this.volumeFin = volume;
        this.on = true;
        this.looping = loop;
        this.isPlaying = true;
        this.isFadeIn = true;
        updateRelativePos();
    }

    public void noteOff() {
        on = false;
    }

    public void volume(float vol) {
        if (isFadeIn) {
            volumeFin = vol;
        } else {
            volume = vol;
        }
    }

    public float getTargetVolume() {
        return this.volumeFin;
    }

    @Override
    public boolean isStopped() {
        return !isPlaying;
    }

    @Override
    public void tick() {
        updateRelativePos();
        if (!on && (!loop || !isFadeIn)) {
            volume = volume * 0.65f;
            if (volume < 0.001f) {
                isPlaying = false;
            }
        } else {
            if (loop) {
                if (isFadeIn) {
                    volume = (float) Math.max(Math.min(0.01f, volumeFin), Math.log(fadeInTick + 1) / Math.log(FADE_IN_TICKS + 1) * volumeFin);
                    fadeInTick += 1;
                    if (volume == volumeFin) {
                        isFadeIn = false;
                    }
                }
            }
            life -= 1;
            if (life <= 0) {
                noteOff();
            }
        }
    }

    private void updateRelativePos() {
        double distanceMultiplier = AllConfigs.common.soundDistanceMultiplier.get();
        Vec3 simulatedPos = calRelativePos(notePos, 1.0d / distanceMultiplier);
        this.x = simulatedPos.x;
        this.y = simulatedPos.y;
        this.z = simulatedPos.z;
    }

    public static Vec3 calRelativePos(Vec3 notePose, double scale) {
        if (Minecraft.getInstance().player == null) {
            return new Vec3(0, 0, 0);
        }
        Vec3 localPos = Minecraft.getInstance().player.position();
        double dX = notePose.x - localPos.x;
        double dY = notePose.y - localPos.y;
        double dZ = notePose.z - localPos.z;
        double xC = localPos.x + dX * scale;
        double yC = localPos.y + dY * scale;
        double zC = localPos.z + dZ * scale;
        return new Vec3(xC, yC, zC);
    }

}
