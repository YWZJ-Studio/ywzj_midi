package org.ywzj.midi.gui.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.ywzj.midi.all.AllSounds;
import org.ywzj.midi.gui.widget.CommonButton;
import org.ywzj.midi.gui.widget.ValueSlider;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.util.ComponentUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public abstract class PlaySustainScreen extends PlayKeyScreen {

    protected final HashMap<String, UUID> notes = new HashMap<>();
    protected ValueSlider velocitySlider;
    private final String keyStart;
    private final String keyEnd;

    public PlaySustainScreen(Instrument instrument, Vec3 pos, Component titleIn, String keyStart, String keyEnd) {
        super(instrument, pos, titleIn);
        this.keyStart = keyStart;
        this.keyEnd = keyEnd;
    }

    @Override
    protected void init() {
        super.init();
        keyButtons.clear();
        Map<String, net.minecraft.sounds.SoundEvent> sounds = AllSounds.INSTRUMENT_WITH_SOUNDS.get(instrument.getName());
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
            if (notation.equals(keyStart)) {
                startFlag = true;
            }
            if (!startFlag) {
                continue;
            }
            if (count < 12) {
                String message = notation.replace("s", "#").toUpperCase(Locale.ROOT);
                if (notes.containsKey(notation)) {
                    message = "(" + message + ")";
                }
                CommonButton keyButton = new CommonButton(keyStartPos, notation.contains("s") ? black : white, 20, 20,
                        ComponentUtils.literal(message),
                        (button) -> {
                            playNoteSwitch(notation);
                            button.setMessage(button.getMessage().toString().contains("(")
                                    ? ComponentUtils.literal(button.getMessage().getString().replace("(","").replace(")", ""))
                                    : ComponentUtils.literal("(" + button.getMessage().getString() + ")"));
                        }, false);
                keyButtons.add(keyButton);
                addRenderableWidget(keyButton);
            } else {
                if (count == 12) {
                    keyStartPos = width/2 - 170;
                }
                String message = notation.replace("s", "#").toUpperCase(Locale.ROOT);
                if (notes.containsKey(notation)) {
                    message = "(" + message + ")";
                }
                CommonButton keyButton = new CommonButton(keyStartPos, notation.contains("s") ? black + 45 : white + 45, 20, 20,
                        ComponentUtils.literal(message),
                        (button) -> {
                            playNoteSwitch(notation);
                            button.setMessage(button.getMessage().toString().contains("(")
                                    ? ComponentUtils.literal(button.getMessage().getString().replace("(","").replace(")", ""))
                                    : ComponentUtils.literal("(" + button.getMessage().getString() + ")"));
                        }, false);
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

    abstract protected void playNoteSwitch(String notation);

}
