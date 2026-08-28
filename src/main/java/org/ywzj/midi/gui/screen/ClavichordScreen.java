package org.ywzj.midi.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.ywzj.midi.audio.NotePlayer;
import org.ywzj.midi.gui.widget.CommonButton;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.instrument.receiver.PercussionMidiReceiver;
import org.ywzj.midi.pose.PoseManager;
import org.ywzj.midi.script.MidiPoseScriptContext;
import org.ywzj.midi.script.MidiScriptPoseProvider;
import org.ywzj.midi.util.ComponentUtils;
import org.ywzj.midi.util.MidiUtils;

import java.util.Collections;
import java.util.UUID;

public class ClavichordScreen extends PlayLegatoScreen {

    private boolean uiPedal = false;

    public ClavichordScreen(Instrument instrument, BlockPos pos, Component titleIn, String keyStart, String keyEnd) {
        super(instrument, new Vec3(pos.getX(), pos.getY(), pos.getZ()), titleIn, keyStart, keyEnd);
    }

    @Override
    protected void init() {
        super.init();
        Button pedalButton = new CommonButton(width / 2 - 190, height / 2 + 30, 65, 20, ComponentUtils.translatable("ui.ywzj_midi.pedal_off"), (button) -> {
            uiPedal = !uiPedal;
            button.setMessage(ComponentUtils.translatable("ui.ywzj_midi.pedal_" + (uiPedal ? "on" : "off")));
        });
        addRenderableWidget(pedalButton);
    }

    protected void playNoteOnce(String notation) {
        UUID uuid = UUID.randomUUID();
        int note = MidiUtils.notationToNote(notation);
        NotePlayer.playNote(uuid, pos, instrument, variantSelectButton.getValue(), note, (int) velocitySlider.value, 0, getMinecraft().player);
        if (receiver instanceof PercussionMidiReceiver percussionReceiver) {
            percussionReceiver.publishStrikeFrames(note, (int) velocitySlider.value);
        } else {
            MidiPoseScriptContext ctx = new MidiPoseScriptContext();
            ctx.setInstrumentId(instrument.getInstrumentId());
            ctx.setNotes(Collections.singletonList(note));
            PoseManager.PlayPose pose = MidiScriptPoseProvider.getInstance().computePlayPose(instrument, ctx);
            if (pose != null) {
                PoseManager.publish(Minecraft.getInstance().player, pose);
            }
        }
        new Thread(() -> {
            try {
                if (uiPedal) {
                    Thread.sleep(10000);
                } else {
                    Thread.sleep(800);
                }
            } catch (Exception ignore) {}
            NotePlayer.stopNote(uuid, getMinecraft().player);
        }).start();
    }

}
