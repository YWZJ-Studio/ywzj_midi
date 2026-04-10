package org.ywzj.midi.api.custom;

import net.minecraft.resources.ResourceLocation;
import org.ywzj.midi.custom.instrument.BaseInstrumentData;

import java.util.Map;
import java.util.Optional;

public interface IInstrumentDataManager {

    Map<ResourceLocation, BaseInstrumentData> getInstrumentData();

    Optional<BaseInstrumentData> getInstrumentData(ResourceLocation id);

}
