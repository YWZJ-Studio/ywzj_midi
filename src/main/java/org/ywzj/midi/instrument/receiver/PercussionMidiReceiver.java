package org.ywzj.midi.instrument.receiver;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.ywzj.midi.audio.NotePlayer;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.pose.PoseManager;
import org.ywzj.midi.script.MidiPoseScriptContext;
import org.ywzj.midi.script.MidiScriptPoseProvider;
import org.ywzj.midi.util.MidiUtils;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import java.util.Collections;
import java.util.UUID;

public class PercussionMidiReceiver extends MidiReceiver {

    private int percussionMode; // 0=timpani, 1=bass_drum, 2=cymbal

    public PercussionMidiReceiver(Instrument instrument, LivingEntity player, Vec3 pos) {
        super(instrument, player, pos);
        this.percussionMode = instrument.getInstrumentId().getPath().equals("bass_drum") ? 1
                : instrument.getInstrumentId().getPath().equals("cymbal") ? 2 : 0;
    }

    public void setPercussionMode(int mode) {
        this.percussionMode = mode;
    }

    @Override
    public void send(MidiMessage message, long timeStamp, int delay) {
        if (message instanceof ShortMessage shortMessage) {
            int command = shortMessage.getCommand();
            if (command == ShortMessage.NOTE_ON) {
                int note = shortMessage.getData1();
                int velocity = shortMessage.getData2();
                if (velocity == 0) {
                    UUID uuid = playedKeys.get(note);
                    if (uuid != null) {
                        NotePlayer.stopNote(uuid, player);
                        playedKeys.remove(note);
                    }
                    return;
                }
                publishStrikeFrames(note, velocity);
                if (percussionMode == 2) {
                    // Cymbal: always play c1
                    UUID uuid = UUID.randomUUID();
                    playNote(MidiUtils.notationToNote("c1"), velocity, delay);
                    playedKeys.put(note, uuid);
                } else if (percussionMode == 1) {
                    // Bass drum: map velocity to fixed note
                    UUID uuid = UUID.randomUUID();
                    NotePlayer.playNote(uuid, portable ? player.position() : pos, instrument,
                            variantId, toBassDrumNote(velocity), velocity, delay, player);
                    playedKeys.put(note, uuid);
                } else {
                    // Timpani: play the actual note
                    playNote(note, velocity, delay);
                }
            } else if (command == ShortMessage.NOTE_OFF) {
                int note = shortMessage.getData1();
                UUID uuid = playedKeys.get(note);
                if (uuid != null) {
                    NotePlayer.stopNote(uuid, player);
                    playedKeys.remove(note);
                }
            }
        }
    }

    @Override
    public void stopPose() {}

    public void publishStrikeFrames(int note, int velocity) {
        var ctx = new MidiPoseScriptContext(note, velocity, instrument.getInstrumentId());
        ctx.setInt("strikeFrame", 0);
        if (percussionMode == 2) {
            ctx.setInt("strikeDir", 1);
        }
        int frameCount = percussionMode == 2 ? 9 : 10;
        for (int i = 0; i < frameCount; i++) {
            PoseManager.PlayPose pose = MidiScriptPoseProvider.getInstance().computePlayPose(instrument, ctx);
            if (pose != null) {
                if (i == frameCount - 1 && percussionMode != 2) {
                    // The final frame carries the same metadata as the
                    // hardcoded implementations: it drives look-at handlers.
                    int handlerNote = percussionMode == 1 ? 0 : note;
                    PoseManager.publish(player, pose, instrument, Collections.singletonList(handlerNote));
                } else {
                    PoseManager.publish(player, pose);
                }
            }
        }
    }

    private int toBassDrumNote(int velocity) {
        return switch (velocity / 25) {
            case 0, 1 -> 29;
            case 2 -> 31;
            case 3 -> 33;
            case 4 -> 35;
            case 5 -> 36;
            default -> 36;
        };
    }
}
