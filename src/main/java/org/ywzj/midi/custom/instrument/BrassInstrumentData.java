package org.ywzj.midi.custom.instrument;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.instrument.receiver.BrassMidiReceiver;
import org.ywzj.midi.instrument.receiver.MidiReceiver;

public class BrassInstrumentData extends BaseInstrumentData {

    @Override
    public MidiReceiver createReceiver(Instrument instrument, LivingEntity player, Vec3 pos) {
        return new BrassMidiReceiver(instrument, player, pos);
    }

}
