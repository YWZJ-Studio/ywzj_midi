package org.ywzj.midi.client.resource;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockModelPOJO;
import net.minecraft.resources.ResourceLocation;

/**
 * Client-side instrument display instance: bedrock model + texture + slot texture.
 */
public class BaseInstrumentDisplay {

    private final ResourceLocation displayId;
    private final BedrockModel model;
    private final ResourceLocation texture;
    private final ResourceLocation slotTexture;

    public BaseInstrumentDisplay(ResourceLocation displayId, BedrockModelPOJO modelPojo,
                                  ResourceLocation texture, ResourceLocation slotTexture) {
        this.displayId = displayId;
        this.model = modelPojo != null ? new BedrockModel(modelPojo) : null;
        this.texture = texture;
        this.slotTexture = slotTexture;
    }

    public ResourceLocation getDisplayId() {
        return displayId;
    }

    public BedrockModel getModel() {
        return model;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public ResourceLocation getSlotTexture() {
        return slotTexture;
    }

}
