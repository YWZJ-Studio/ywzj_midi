package org.ywzj.midi.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.audio.NotePlayer;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.pose.PoseManager;
import org.ywzj.midi.script.MidiPoseScriptContext;
import org.ywzj.midi.script.MidiScriptPoseProvider;
import org.ywzj.midi.util.MidiUtils;

import java.util.Collections;
import java.util.UUID;

public class BrassScreen extends PlaySustainScreen {

    public BrassScreen(Instrument instrument, Vec3 pos, Component titleIn, String keyStart, String keyEnd) {
        super(instrument, pos, titleIn, keyStart, keyEnd);
    }

    @Override
    protected void playNoteSwitch(String notation) {
        if (notes.containsKey(notation)) {
            NotePlayer.stopNote(notes.remove(notation), getMinecraft().player);
            clearPublishedPose();
        } else {
            UUID uuid = UUID.randomUUID();
            int note = MidiUtils.notationToNote(notation);
            NotePlayer.playNote(uuid, Minecraft.getInstance().player.position(), instrument, variantSelectButton.getValue(), note, (int) velocitySlider.value, 0, getMinecraft().player);
            var ctx = new MidiPoseScriptContext(note, (int) velocitySlider.value, instrument.getInstrumentId());
            PoseManager.PlayPose pose = MidiScriptPoseProvider.getInstance().computePlayPose(instrument, ctx);
            if (pose != null) {
                if (instrument.getInstrumentId().equals(YwzjMidi.modLocation("trombone"))) {
                    publishPose(pose, instrument, Collections.singletonList(note));
                } else {
                    publishPose(pose);
                }
            }
            notes.put(notation, uuid);
        }
    }

}
