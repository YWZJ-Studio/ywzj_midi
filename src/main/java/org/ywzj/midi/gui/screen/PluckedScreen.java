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

public class PluckedScreen extends PlayLegatoScreen {

    public PluckedScreen(Instrument instrument, Vec3 pos, Component titleIn, String keyStart, String keyEnd) {
        super(instrument, pos, titleIn, keyStart, keyEnd);
    }

    @Override
    protected void playNoteOnce(String notation) {
        UUID uuid = UUID.randomUUID();
        int note = MidiUtils.notationToNote(notation);
        int velocity = (int) velocitySlider.value;
        int variantId = variantSelectButton.getValue();
        NotePlayer.playNote(uuid, Minecraft.getInstance().player.position(), instrument,
                variantId, note, velocity, 0, Minecraft.getInstance().player);

        MidiPoseScriptContext context = new MidiPoseScriptContext(note, velocity, instrument.getInstrumentId());
        context.setInt("variantId", variantId);
        PoseManager.PlayPose pose = MidiScriptPoseProvider.getInstance().computePlayPose(instrument, context);
        if (pose != null) {
            publishPose(pose);
        }

        delayActiveKeyPool.submit(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
            NotePlayer.stopNote(uuid, Minecraft.getInstance().player);
        });
    }

}
