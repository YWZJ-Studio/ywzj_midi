package org.ywzj.midi.instrument;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.ywzj.midi.instrument.receiver.MidiReceiver;

import java.util.HashMap;

public class AbstractInstrument {

    public ResourceLocation instrumentId;
    public ResourceLocation displayId;
    public HashMap<String, SoundEvent> keys;
    public MidiReceiver midiReceiver;

    public void init() {

    }

}
