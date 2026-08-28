package org.ywzj.midi.instrument.player;

import net.minecraft.client.Minecraft;
import org.ywzj.midi.gui.screen.MidiInstrumentScreen;

public class InstrumentMidiPlayer extends MidiPlayer {

    private final Runnable endCallback;

    public InstrumentMidiPlayer(MidiInstrumentScreen midiInstrumentScreen) {
        super(Minecraft.getInstance().player);
        this.endCallback = midiInstrumentScreen::callbackPlayButton;
    }

    public InstrumentMidiPlayer(Runnable endCallback) {
        super(Minecraft.getInstance().player);
        this.endCallback = endCallback;
    }

    @Override
    public void handleStep() {}

    @Override
    public void endCallback() {
        endCallback.run();
    }

}
