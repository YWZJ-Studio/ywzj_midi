package org.ywzj.midi.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.all.AllInstruments;
import org.ywzj.midi.blockentity.AABlockEntity;
import org.ywzj.midi.gui.widget.CommonButton;
import org.ywzj.midi.gui.widget.ValueSlider;
import org.ywzj.midi.instrument.receiver.BlockMidiReceiver;
import org.ywzj.midi.util.ComponentUtils;
import org.ywzj.midi.util.MidiUtils;

public class AABlockScreen extends MidiInstrumentScreen {

    private ValueSlider valueSlider;
    private final AABlockEntity aaBlockEntity;

    public AABlockScreen(BlockPos pos, Component titleIn, AABlockEntity aaBlockEntity) {
        super(AllInstruments.fromId(YwzjMidi.modLocation("aa775")), new Vec3(pos.getX(), pos.getY(), pos.getZ()), titleIn);
        this.aaBlockEntity = aaBlockEntity;
        this.receiver = new BlockMidiReceiver(aaBlockEntity);
    }

    @Override
    protected void init() {
        super.init();
        valueSlider = new ValueSlider(width/2 - 170, height/2, 100, 20, ComponentUtils.translatable("ui.ywzj_midi.note").getString(), aaBlockEntity.note, 127, MidiUtils::noteToNotation);
        addRenderableWidget(valueSlider);
        if (receiver != null && receiver.getMidiInputDevice() != null) {
            Button closeReceiverButton = new CommonButton(width / 2 - 60, height / 2 + 60, 120, 20,
                    ComponentUtils.translatable("ui.ywzj_midi.midi_connected"),
                    (button) -> {
                        receiver.close();
                        Minecraft.getInstance().player.sendSystemMessage(ComponentUtils.translatable("info.ywzj_midi.midi_disconnected"));
                        button.active = false;
                        button.visible = false;
                    });
            addRenderableWidget(closeReceiverButton);
        }
    }

    @Override
    public void onClose() {
        aaBlockEntity.note = valueSlider.value;
        aaBlockEntity.clientSendData(false);
        needSave = true;
        super.onClose();
    }

}
