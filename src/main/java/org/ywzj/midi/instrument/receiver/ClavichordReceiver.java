package org.ywzj.midi.instrument.receiver;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.pose.PoseManager;
import org.ywzj.midi.script.MidiPoseScriptContext;
import org.ywzj.midi.script.MidiScriptPoseProvider;
import org.ywzj.midi.util.MathUtils;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import java.util.ArrayList;
import java.util.List;

public class ClavichordReceiver extends MidiReceiver {

    private final List<Integer> posePlayNotes = new ArrayList<>();
    private long lastTimeStamp;

    public ClavichordReceiver(Instrument instrument, LivingEntity player, Vec3 pos) {
        super(instrument, player, pos);
    }

    @Override
    public void send(MidiMessage message, long timeStamp, int delay) {
        if (timeStamp < lastTimeStamp) {
            lastTimeStamp = 0;
        }
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
                posePlayNotes.add(note);
                if (timeStamp - lastTimeStamp > 10 || posePlayNotes.size() > 8) {
                    if (MathUtils.distance(player.getX(), player.getY(), player.getZ(), pos.x, pos.y, pos.z) < 3) {
                        var ctx = new MidiPoseScriptContext(note, velocity, instrument.getInstrumentId());
                        ctx.setNotes(new ArrayList<>(posePlayNotes));
                        PoseManager.PlayPose pose = MidiScriptPoseProvider.getInstance().computePlayPose(instrument, ctx);
                        if (pose != null) {
                            PoseManager.publish(player, pose);
                        }
                    } else {
                        PoseManager.clearCache(player);
                    }
                    posePlayNotes.clear();
                    lastTimeStamp = timeStamp;
                }
            } else if (command == ShortMessage.NOTE_OFF) {
                stopNote(shortMessage.getData1());
            } else if (command == ShortMessage.CONTROL_CHANGE) {
                commandChange(shortMessage.getData1(), shortMessage.getData2());
            }
        }
    }

    @Override
    public void stopPose() {
        PoseManager.clearCache(player);
    }

}
