package org.ywzj.midi.instrument.receiver;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.pose.PoseManager;
import org.ywzj.midi.script.MidiPoseScriptContext;
import org.ywzj.midi.script.MidiScriptPoseProvider;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

public class PluckedMidiReceiver extends MidiReceiver {

    public PluckedMidiReceiver(Instrument instrument, LivingEntity player, Vec3 pos) {
        super(instrument, player, pos);
    }

    @Override
    public void send(MidiMessage message, long timeStamp, int delay) {
        if (!(message instanceof ShortMessage shortMessage)) {
            return;
        }
        int command = shortMessage.getCommand();
        if (command == ShortMessage.NOTE_ON) {
            int note = shortMessage.getData1();
            int velocity = shortMessage.getData2();
            if (velocity == 0) {
                stopNote(note);
                return;
            }
            playNote(variantId, note, velocity, delay);
            MidiPoseScriptContext context = new MidiPoseScriptContext(note, velocity, instrument.getInstrumentId());
            context.setInt("variantId", variantId);
            PoseManager.PlayPose pose = MidiScriptPoseProvider.getInstance().computePlayPose(instrument, context);
            if (pose != null) {
                publish(pose);
            }
        } else if (command == ShortMessage.NOTE_OFF) {
            stopNote(shortMessage.getData1());
        } else if (command == ShortMessage.CONTROL_CHANGE) {
            commandChange(shortMessage.getData1(), shortMessage.getData2());
        }
    }

    @Override
    public void stopPose() {
        clearPose();
    }

}
