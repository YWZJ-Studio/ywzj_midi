package org.ywzj.midi.instrument.receiver;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.ywzj.midi.audio.NotePlayer;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.pose.PoseManager;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class MidiReceiver implements Receiver {

    protected final ConcurrentHashMap<Integer, UUID> playedKeys = new ConcurrentHashMap<>();
    protected final Boolean portable;
    protected final LivingEntity player;
    protected final Vec3 pos;
    protected final Instrument instrument;
    protected MidiDevice.Info info;
    protected MidiDevice midiInputDevice;
    protected boolean pedal = false;
    protected final HashSet<UUID> inPedalKeys = new HashSet<>();
    protected int variantId = 0;
    protected int cc7Data = 127;
    protected float volume = 1f;
    private int instrumentEntityId = -1;

    public MidiReceiver(Instrument instrument, LivingEntity player, Vec3 pos) {
        this.portable = instrument.isPortable();
        this.player = player;
        this.pos = pos == null ? this.player.position() : pos;
        this.instrument = instrument;
    }

    public Set<Integer> getPlayedNotes() {
        if (playedKeys.isEmpty()) {
            return Collections.emptySet();
        }
        return Set.copyOf(playedKeys.keySet());
    }

    public boolean initDevice(MidiDevice.Info info) {
        this.info = info;
        try {
            if (midiInputDevice != null && midiInputDevice.isOpen()) {
                midiInputDevice.close();
            }
            midiInputDevice = MidiSystem.getMidiDevice(info);
            midiInputDevice.open();
            midiInputDevice.getTransmitter().setReceiver(this);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            midiInputDevice = null;
            return false;
        }
    }

    public void closeDevice() {
        if (midiInputDevice != null) {
            midiInputDevice.close();
        }
    }

    public void playNote(int note, int velocity, int delay) {
        playNote(0, note, velocity, delay);
    }

    public void playNote(int variantId, int note, int velocity, int delay) {
        if (playedKeys.containsKey(note)) {
            UUID uuid = playedKeys.get(note);
            NotePlayer.stopNote(uuid, player);
            playedKeys.remove(note);
        }
        UUID uuid = UUID.randomUUID();
        NotePlayer.playNote(uuid, portable ? player.position() : pos, instrument, variantId, note, velocity, delay, player);
        playedKeys.put(note, uuid);
    }

    public void stopNote(int note) {
        UUID uuid = playedKeys.get(note);
        if (uuid != null) {
            if (pedal) {
                inPedalKeys.add(uuid);
            } else {
                NotePlayer.stopNote(uuid, player);
            }
            playedKeys.remove(note);
        }
    }

    public void commandChange(int data1, int data2) {
        if (data1 == 7 || data1 == 11) {
            volume = (float) data2 / 127;
            playedKeys.values().forEach(uuid -> NotePlayer.changeNote(uuid, (float) data2 / cc7Data, player));
            cc7Data = data2;
        } else if (data1 == 64) {
            if (data2 > 25) {
                pedal = true;
            } else {
                pedal = false;
                inPedalKeys.forEach(noteUuid -> NotePlayer.stopNote(noteUuid, player));
                inPedalKeys.clear();
            }
        }
    }

    public abstract void stopPose();

    public MidiDevice getMidiInputDevice() {
        return midiInputDevice;
    }

    public LivingEntity getPlayer() {
        return player;
    }

    public boolean useLoop() {
        return instrument.getVariant(variantId).isLoop();
    }

    public void setInstrumentEntityId(int instrumentEntityId) {
        this.instrumentEntityId = instrumentEntityId;
    }

    protected void publish(PoseManager.PlayPose pose) {
        if (pose != null) {
            pose.setInstrumentEntityId(instrumentEntityId);
        }
        PoseManager.publish(player, pose);
    }

    protected void publish(PoseManager.PlayPose pose, Instrument instrument, java.util.List<Integer> notes) {
        if (pose != null) {
            pose.setInstrumentEntityId(instrumentEntityId);
        }
        PoseManager.publish(player, pose, instrument, notes);
    }

    protected void clearPose() {
        PoseManager.PlayPose pose = new PoseManager.PlayPose();
        pose.setNotes(instrument.getInstrumentId(), Collections.emptyList());
        publish(pose);
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {
        this.send(message, timeStamp, 0);
    }

    public abstract void send(MidiMessage message, long timeStamp, int delay);

    public void stopAllKeys() {
        playedKeys.values().forEach(noteUuid -> NotePlayer.stopNote(noteUuid, player));
        playedKeys.clear();
        variantId = 0;
    }

    @Override
    public void close() {
        closeDevice();
    }

}
