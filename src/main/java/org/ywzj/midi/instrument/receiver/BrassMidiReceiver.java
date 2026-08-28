package org.ywzj.midi.instrument.receiver;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.pose.PoseManager;
import org.ywzj.midi.script.MidiPoseScriptContext;
import org.ywzj.midi.script.MidiScriptPoseProvider;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import java.util.Collections;

public class BrassMidiReceiver extends MidiReceiver {

    public BrassMidiReceiver(Instrument instrument, LivingEntity player, Vec3 pos) {
        super(instrument, player, pos);
    }

    @Override
    public void send(MidiMessage message, long timeStamp, int delay) {
        if (message instanceof ShortMessage shortMessage) {
            int command = shortMessage.getCommand();
            if (command == ShortMessage.NOTE_ON) {
                int note = shortMessage.getData1();
                int velocity = shortMessage.getData2();
                if (velocity == 0) {
                    stopNote(note);
                    return;
                }
                playNote(note, velocity, delay);
                var ctx = new MidiPoseScriptContext(note, velocity, instrument.getInstrumentId());
                PoseManager.PlayPose pose = MidiScriptPoseProvider.getInstance().computePlayPose(instrument, ctx);
                if (pose != null) {
                    // Trombone's item model consumes note metadata to update
                    // its slide, as in TrombonePlayPose.play(int).
                    if (instrument.getInstrumentId().equals(YwzjMidi.modLocation("trombone"))) {
                        PoseManager.publish(player, pose, instrument, Collections.singletonList(note));
                    } else {
                        PoseManager.publish(player, pose);
                    }
                }
            } else if (command == ShortMessage.NOTE_OFF) {
                int note = shortMessage.getData1();
                stopNote(note);
            }
        }
    }

    @Override
    public void stopPose() {
        PoseManager.clearCache(player);
    }

}
