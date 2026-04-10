package org.ywzj.midi.custom.instrument;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class BaseInstrumentDataPojo {

    @SerializedName("type")
    public String type = "generic";

    @SerializedName("name")
    public String name = "unknown";

    @SerializedName("loop")
    public boolean loop = false;

    @SerializedName("portable")
    public boolean portable = false;

    @SerializedName("key_start")
    public String keyStart = "c4";

    @SerializedName("key_end")
    public String keyEnd = "c5";

    @SerializedName("variants")
    public List<VariantPojo> variants = new ArrayList<>();

    @SerializedName("structure_model")
    public ResourceLocation structureModel = null;

    public static class VariantPojo {
        @SerializedName("name")
        public String name = "raw";

        @SerializedName("loop")
        public boolean loop = false;

        @SerializedName("key_start")
        public String keyStart = "c4";

        @SerializedName("key_end")
        public String keyEnd = "c5";
    }
}