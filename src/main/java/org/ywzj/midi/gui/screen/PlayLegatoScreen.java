package org.ywzj.midi.gui.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.ywzj.midi.all.AllSounds;
import org.ywzj.midi.gui.widget.CommonButton;
import org.ywzj.midi.gui.widget.ValueSlider;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.util.ComponentUtils;
import org.ywzj.midi.util.MidiUtils;

import java.util.Locale;
import java.util.Map;

public abstract class PlayLegatoScreen extends PlayKeyScreen {

    protected ValueSlider velocitySlider;
    private final String keyStart;
    private final String keyEnd;

    public PlayLegatoScreen(Instrument instrument, Vec3 pos, Component titleIn, String keyStart, String keyEnd) {
        super(instrument, pos, titleIn);
        this.keyStart = keyStart;
        this.keyEnd = keyEnd;
    }

    @Override
    protected void init() {
        super.init();
        keyButtons.clear();
        Map<String, net.minecraft.sounds.SoundEvent> sounds = AllSounds.INSTRUMENT_WITH_SOUNDS.get(instrument.getInstrumentId());
        if (sounds == null) {
            return;
        }
        int keyStartPos = width/2 - 170;
        int white = height/2 - 80;
        int black = height/2 - 100;
        int count = 0;
        boolean startFlag = false;
        for (String soundName : sounds.keySet()) {
            final String notation = soundName.replace(instrument.getName() + "_", "");
            if (!startFlag && MidiUtils.notationToNote(notation) >= MidiUtils.notationToNote(keyStart)) {
                startFlag = true;
            }
            if (!startFlag) {
                continue;
            }
            if (count < 12) {
                CommonButton keyButton = new CommonButton(keyStartPos, notation.contains("s") ? black : white, 20, 20,
                        ComponentUtils.literal(notation.replace("s", "#").toUpperCase(Locale.ROOT)),
                        (button) -> playNoteOnce(notation), false);
                keyButtons.add(keyButton);
                addRenderableWidget(keyButton);
            } else {
                if (count == 12) {
                    keyStartPos = width/2 - 170;
                }
                CommonButton keyButton =new CommonButton(keyStartPos, notation.contains("s") ? black + 45 : white + 45, 20, 20,
                        ComponentUtils.literal(notation.replace("s", "#").toUpperCase(Locale.ROOT)),
                        (button) -> playNoteOnce(notation), false);
                keyButtons.add(keyButton);
                addRenderableWidget(keyButton);
            }
            keyStartPos += 20;
            count += 1;
            if (notation.equals(keyEnd)) {
                break;
            }
        }
        velocitySlider = new ValueSlider(width/2 - 170, height/2, 100, 20, ComponentUtils.translatable("ui.ywzj_midi.play_velocity").getString(), 64, 127);
        addRenderableWidget(velocitySlider);
    }

    abstract protected void playNoteOnce(String notation);

}
