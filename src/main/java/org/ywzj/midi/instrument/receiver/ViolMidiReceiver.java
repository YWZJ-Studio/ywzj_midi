package org.ywzj.midi.instrument.receiver;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.pose.PoseManager;
import org.ywzj.midi.script.MidiPoseScriptContext;
import org.ywzj.midi.script.MidiScriptPoseProvider;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

public class ViolMidiReceiver extends MidiReceiver {

    private final MidiPoseScriptContext poseContext = new MidiPoseScriptContext();
    private long lastTimeStamp = 0;
    private boolean bowLoop = false;
    private boolean pauseBow = false;

    public ViolMidiReceiver(Instrument instrument, LivingEntity player, Vec3 pos) {
        super(instrument, player, pos);
        poseContext.setInstrumentId(instrument.getInstrumentId());
    }

    private void startBowLoop() {
        if (bowLoop) return;
        bowLoop = true;
        new Thread(() -> {
            while (bowLoop) {
                try {
                    Thread.sleep(50);
                } catch (Exception ignore) {}
                if (!pauseBow) {
                    poseContext.setTick(System.currentTimeMillis());
                    PoseManager.PlayPose pose = MidiScriptPoseProvider.getInstance().computePlayPose(instrument, poseContext);
                    if (pose != null) {
                        PoseManager.publish(player, pose);
                    }
                }
            }
        }).start();
    }

    public void stopBowLoop() {
        bowLoop = false;
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
                    if (playedKeys.size() == 0) {
                        pauseBow = true;
                    }
                    return;
                }
                playNote(variantId, note, velocity, delay);
                if (variantId == 0) {
                    // Bowing mode
                    if (lastTimeStamp == 0 || timeStamp < lastTimeStamp) {
                        poseContext.resetState();
                        startBowLoop();
                    }
                    pauseBow = false;
                    if (timeStamp - lastTimeStamp > 4000) {
                        poseContext.resetState();
                    }
                } else {
                    // Pizzicato mode
                    poseContext.setInt("variantId", 1);
                    poseContext.setInt("pizzStep", 0);
                    for (int i = 0; i < 7; i++) {
                        PoseManager.PlayPose pose = MidiScriptPoseProvider.getInstance().computePlayPose(instrument, poseContext);
                        if (pose != null) {
                            PoseManager.publish(player, pose);
                        }
                    }
                }
                lastTimeStamp = timeStamp;
            } else if (command == ShortMessage.NOTE_OFF) {
                int note = shortMessage.getData1();
                stopNote(note);
                if (playedKeys.isEmpty()) {
                    pauseBow = true;
                }
            } else if (command == ShortMessage.PROGRAM_CHANGE) {
                int program = shortMessage.getData1();
                if (program == 45) {
                    variantId = 1;
                } else {
                    variantId = 0;
                }
                poseContext.setInt("variantId", variantId);
            } else if (command == ShortMessage.CONTROL_CHANGE) {
                commandChange(shortMessage.getData1(), shortMessage.getData2());
            }
        }
    }

    @Override
    public void stopPose() {
        stopBowLoop();
        PoseManager.clearCache(player);
    }

}
