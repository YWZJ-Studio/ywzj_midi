package org.ywzj.midi.custom.instrument;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class BaseInstrumentData {

    protected ResourceLocation instrumentId;
    protected Component name;
    protected boolean loop;
    protected boolean portable;
    protected String keyStart;
    protected String keyEnd;
    protected List<Variant> variants = new ArrayList<>();
    protected ResourceLocation structureModel;

    public BaseInstrumentData() {}

    public void build(BaseInstrumentDataPojo pojo) {
        this.loop = pojo.loop;
        this.portable = pojo.portable;
        this.keyStart = pojo.keyStart;
        this.keyEnd = pojo.keyEnd;
        this.structureModel = pojo.structureModel;
        this.variants.clear();
        for (BaseInstrumentDataPojo.VariantPojo variantPojo : pojo.variants) {
            variants.add(new Variant(variantPojo.name, variantPojo.loop, variantPojo.keyStart, variantPojo.keyEnd));
        }
        // If no variants, add default raw variant
        if (variants.isEmpty()) {
            variants.add(new Variant("raw", loop, keyStart, keyEnd));
        }
    }

    public ResourceLocation getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(ResourceLocation instrumentId) {
        this.instrumentId = instrumentId;
    }

    public Component getName() {
        return name;
    }

    public void setName(Component name) {
        this.name = name;
    }

    public boolean isLoop() {
        return loop;
    }

    public boolean isPortable() {
        return portable;
    }

    public String getKeyStart() {
        return keyStart;
    }

    public String getKeyEnd() {
        return keyEnd;
    }

    public List<Variant> getVariants() {
        return variants;
    }

    public ResourceLocation getStructureModel() {
        return structureModel;
    }

    // Possibly create an Instrument instance from this data
    // This would require mapping type string to Instrument subclass
    // For now, we just store the data.

    public static class Variant {
        private final String name;
        private final boolean loop;
        private final String keyStart;
        private final String keyEnd;

        public Variant(String name, boolean loop, String keyStart, String keyEnd) {
            this.name = name;
            this.loop = loop;
            this.keyStart = keyStart;
            this.keyEnd = keyEnd;
        }

        public String getName() { return name; }
        public boolean isLoop() { return loop; }
        public String getKeyStart() { return keyStart; }
        public String getKeyEnd() { return keyEnd; }
    }
}