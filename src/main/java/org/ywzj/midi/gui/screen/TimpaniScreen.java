package org.ywzj.midi.gui.screen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.all.AllInstruments;
import org.ywzj.midi.blockentity.TimpaniBlockEntity;
import org.ywzj.midi.gui.widget.ValueSlider;
import org.ywzj.midi.util.ComponentUtils;
import org.ywzj.midi.util.MidiUtils;

public class TimpaniScreen extends MidiInstrumentScreen {

    private ValueSlider valueSlider;
    private final TimpaniBlockEntity timpaniBlockEntity;

    public TimpaniScreen(BlockPos pos, TimpaniBlockEntity timpaniBlockEntity) {
        super(AllInstruments.fromId(YwzjMidi.modLocation("timpani")), new Vec3(pos.getX(), pos.getY(), pos.getZ()), ComponentUtils.literal("timpani"));
        this.timpaniBlockEntity = timpaniBlockEntity;
    }

    @Override
    protected void init() {
        super.init();
        if (valueSlider == null) {
            valueSlider = new ValueSlider(width/2 - 100, height/2 - 80, 200, 20,
                    ComponentUtils.translatable("ui.ywzj_midi.note").getString(), timpaniBlockEntity.note - 36, 22,
                    (note) -> MidiUtils.noteToNotation(36 + note));
        }
        addRenderableWidget(valueSlider);
    }

    @Override
    public void onClose() {
        timpaniBlockEntity.note = 36 + valueSlider.value;
        timpaniBlockEntity.clientSendData(false);
        needSave = true;
        super.onClose();
    }

}
