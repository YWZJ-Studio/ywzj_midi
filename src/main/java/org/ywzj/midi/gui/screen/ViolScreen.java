package org.ywzj.midi.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.ywzj.midi.audio.NotePlayer;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.pose.PoseManager;
import org.ywzj.midi.script.MidiPoseScriptContext;
import org.ywzj.midi.script.MidiScriptPoseProvider;
import org.ywzj.midi.util.MidiUtils;

import java.util.UUID;

public class ViolScreen extends PlaySustainScreen {

    private final MidiPoseScriptContext poseContext = new MidiPoseScriptContext();
    private boolean bowLoop = false;
    private boolean pauseBow = false;

    public ViolScreen(Instrument instrument, Vec3 pos, Component titleIn, String keyStart, String keyEnd) {
        super(instrument, pos, titleIn, keyStart, keyEnd);
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
                    PoseManager.PlayPose pose = MidiScriptPoseProvider.getInstance().computePlayPose(instrument, poseContext);
                    if (pose != null) {
                        publishPose(pose);
                    }
                }
            }
        }).start();
    }

    protected void playNoteSwitch(String notation) {
        if (notes.containsKey(notation)) {
            NotePlayer.stopNote(notes.remove(notation), getMinecraft().player);
            if (notes.isEmpty()) {
                pauseBow = true;
            }
        } else {
            UUID uuid = UUID.randomUUID();
            int variantId = variantSelectButton.getValue();
            int note = MidiUtils.notationToNote(notation);
            NotePlayer.playNote(uuid, Minecraft.getInstance().player.position(), instrument, variantId, note, (int) velocitySlider.value, 0, getMinecraft().player);
            if (instrument.getVariant(variantId).isLoop()) {
                poseContext.setInt("variantId", 0);
                startBowLoop();
                pauseBow = false;
                notes.put(notation, uuid);
            } else {
                poseContext.setInt("variantId", 1);
                poseContext.setInt("pizzStep", 0);
                for (int i = 0; i < 7; i++) {
                    PoseManager.PlayPose pose = MidiScriptPoseProvider.getInstance().computePlayPose(instrument, poseContext);
                    if (pose != null) {
                        publishPose(pose);
                    }
                }
            }
        }
    }

}
