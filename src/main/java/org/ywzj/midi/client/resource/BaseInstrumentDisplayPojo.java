package org.ywzj.midi.client.resource;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceLocation;

/**
 * POJO for instrument display definition JSON.
 * Lives at assets/<namespace>/display/instrument/<name>.json
 */
public class BaseInstrumentDisplayPojo {

    @SerializedName("model")
    public ResourceLocation model;

    @SerializedName("texture")
    public ResourceLocation texture;

    @SerializedName("slot_texture")
    public ResourceLocation slotTexture;

}
