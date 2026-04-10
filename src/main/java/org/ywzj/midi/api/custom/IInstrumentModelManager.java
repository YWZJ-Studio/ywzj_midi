package org.ywzj.midi.api.custom;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

public interface IInstrumentModelManager {

    Map<ResourceLocation, BedrockModel> getInstrumentModels();

    Optional<BedrockModel> getInstrumentModel(ResourceLocation location);

}
