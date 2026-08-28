package org.ywzj.midi.pose.handler;

import net.minecraft.resources.ResourceLocation;
import org.ywzj.midi.all.AllInstruments;
import org.ywzj.midi.instrument.Instrument;

import java.util.List;
import java.util.UUID;

public abstract class NotesHandler {

    public abstract ResourceLocation getInstrumentId();

    public Instrument getInstrument() {
        return AllInstruments.fromId(getInstrumentId());
    }

    public abstract void handle(UUID playerUuid, List<Integer> notes);

}
