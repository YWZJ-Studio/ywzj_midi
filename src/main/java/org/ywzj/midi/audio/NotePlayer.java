package org.ywzj.midi.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.all.AllSounds;
import org.ywzj.midi.audio.sound.MidiSound;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.network.Channel;
import org.ywzj.midi.network.handler.ChangeNoteHandler;
import org.ywzj.midi.network.handler.PlayNoteHandler;
import org.ywzj.midi.network.message.CChangeNote;
import org.ywzj.midi.network.message.CPlayNote;
import org.ywzj.midi.util.MidiUtils;
import org.ywzj.midi.util.ParticleUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class NotePlayer {

    private static final ConcurrentHashMap<UUID, MidiSound> SOUND_INSTANCES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Integer> SOUND_DELAYS = new ConcurrentHashMap<>();
    private static final ExecutorService delayClientNotePool = new ThreadPoolExecutor(1024, 1024, 0L,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(2048),
            new ThreadPoolExecutor.AbortPolicy());

    public static MidiSound getInstance(UUID uuid) {
        return SOUND_INSTANCES.get(uuid);
    }

    public static void playNote(UUID uuid, Vec3 pos, Instrument instrument, int variantId, int note, int velocity, int delay, LivingEntity player) {
        if (instrument == null || player == null) {
            return;
        }
        if (player.level().isClientSide) {
            playClientNote(uuid, pos, instrument, variantId, note, velocity, delay);
            playServerNote(uuid, pos, instrument, variantId, note, velocity, delay);
        } else {
            PlayNoteHandler.broadcastNote(player, new CPlayNote(pos, instrument.getInstrumentId(), variantId, note, velocity, delay, uuid), false);
        }
    }

    public static void changeNote(UUID uuid, float velScale, LivingEntity player) {
        if (player.level().isClientSide) {
            changeClientNote(uuid, velScale);
            changeServerNote(uuid, velScale);
        } else {
            ChangeNoteHandler.broadcastNote(player, new CChangeNote(velScale, uuid), false);
        }
    }

    public static void stopNote(UUID uuid, LivingEntity player) {
        if (player.level().isClientSide) {
            stopClientNote(uuid);
            stopServerNote(uuid);
        } else {
            PlayNoteHandler.broadcastNote(player, new CPlayNote(uuid), false);
        }
    }

    public static void playServerNote(UUID uuid, Vec3 pos, Instrument instrument, int variantId, int note, int velocity, int delay) {
        Channel.CHANNEL.sendToServer(new CPlayNote(pos, instrument.getInstrumentId(), variantId, note, velocity, delay, uuid));
    }

    public static void changeServerNote(UUID uuid, float velScale) {
        Channel.CHANNEL.sendToServer(new CChangeNote(velScale, uuid));
    }

    public static void stopServerNote(UUID uuid) {
        Channel.CHANNEL.sendToServer(new CPlayNote(uuid));
    }

    @OnlyIn(Dist.CLIENT)
    public static void playClientNote(UUID uuid, Vec3 pos, Instrument instrument, int variantId, int note, int velocity, int delay) {
        if (instrument == null) {
            YwzjMidi.LOGGER.warn("Unknown instrument id in note packet");
            return;
        }
        String notation = MidiUtils.noteToNotation(note);
        String baseName = instrument.getName() + "_" + notation;
        boolean loop = instrument.isLoop();
        String variantSuffix = "";
        if (variantId > 0) {
            Instrument.Variant variant = instrument.getVariant(variantId);
            if (variant.getIndex() != 0) {
                variantSuffix = "_" + variant.getName();
                loop = variant.isLoop();
            }
        }
        Map<String, SoundEvent> sounds = AllSounds.INSTRUMENT_WITH_SOUNDS.get(instrument.getInstrumentId());
        if (sounds == null) {
            YwzjMidi.LOGGER.warn("Unknown instrument sound set {}", instrument.getName());
            return;
        }
        // 多级力度音色
        String velSuffix = MidiUtils.velocitySuffix(velocity);
        SoundEvent event = sounds.get(baseName + velSuffix + variantSuffix);
        if (event == null && !velSuffix.isEmpty()) {
            event = sounds.get(baseName + velSuffix);
        }
        if (event == null) {
            event = sounds.get(baseName + variantSuffix);
        }
        if (event == null && !variantSuffix.isEmpty()) {
            event = sounds.get(baseName);
        }
        if (event == null) {
            YwzjMidi.LOGGER.warn("Unknown sound sample {}", baseName + velSuffix + variantSuffix);
            return;
        }
        MidiSound instance = new MidiSound(event,
                MidiUtils.velocityVolume(velocity),
                1f,
                loop,
                pos);
        delayClientNotePool.submit(() -> {
            SOUND_DELAYS.put(uuid, delay);
            try {
                Thread.sleep(delay);
            } catch (Exception ignore) {}
            Minecraft.getInstance().execute(() -> Minecraft.getInstance().getSoundManager().play(instance));
            Minecraft.getInstance().execute(() -> ParticleUtils.addNoteParticle(pos));
        });
        if (uuid != null) {
            SOUND_INSTANCES.put(uuid, instance);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void changeClientNote(UUID uuid, float velScale) {
        delayClientNotePool.submit(() -> {
            try {
                Thread.sleep(SOUND_DELAYS.get(uuid));
            } catch (Exception ignore) {}
            MidiSound instance = SOUND_INSTANCES.get(uuid);
            if (instance != null) {
                instance.volume(instance.getTargetVolume() * velScale);
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public static void stopClientNote(UUID uuid) {
        delayClientNotePool.submit(() -> {
            try {
                Thread.sleep(SOUND_DELAYS.get(uuid));
            } catch (Exception ignore) {}
            MidiSound instance = SOUND_INSTANCES.get(uuid);
            if (instance != null) {
                instance.noteOff();
                SOUND_INSTANCES.remove(uuid);
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public static void stopAllNotes() {
        SOUND_INSTANCES.values().forEach(MidiSound::noteOff);
        SOUND_INSTANCES.clear();
    }

}
