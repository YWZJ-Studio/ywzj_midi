package org.ywzj.midi.custom.instrument;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.ywzj.midi.all.AllInstruments;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.instrument.receiver.MidiReceiver;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseInstrumentData {

    protected ResourceLocation instrumentId;
    protected Component name;
    protected String rawName;
    protected boolean loop;
    protected boolean portable;
    protected String keyStart;
    protected String keyEnd;
    protected List<Variant> variants = new ArrayList<>();
    protected ResourceLocation structureModel;
    protected AllInstruments.Family family;
    protected String tool;
    protected InteractionHand toolHand;
    protected InteractionHand playHand;
    protected String itemType;

    public BaseInstrumentData() {}

    public void build(BaseInstrumentDataPojo pojo) {
        this.rawName = pojo.name;
        this.loop = pojo.loop;
        this.portable = pojo.portable;
        this.keyStart = pojo.keyStart;
        this.keyEnd = pojo.keyEnd;
        this.structureModel = pojo.structureModel;
        if (pojo.family != null) {
            try {
                this.family = AllInstruments.Family.valueOf(pojo.family.toUpperCase());
            } catch (IllegalArgumentException ignore) {}
        }
        this.tool = pojo.tool;
        if (pojo.toolHand != null) {
            this.toolHand = "main_hand".equalsIgnoreCase(pojo.toolHand) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        }
        if (pojo.playHand != null) {
            this.playHand = "main_hand".equalsIgnoreCase(pojo.playHand) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        }
        this.itemType = pojo.itemType != null ? pojo.itemType : "item";
        this.variants.clear();
        for (BaseInstrumentDataPojo.VariantPojo variantPojo : pojo.variants) {
            variants.add(new Variant(variantPojo.name, variantPojo.loop, variantPojo.keyStart, variantPojo.keyEnd));
        }
        if (variants.isEmpty()) {
            variants.add(new Variant("raw", loop, keyStart, keyEnd));
        }
    }

    /**
     * 创建此乐器数据对应的 MidiReceiver 实例。
     * 由子类覆写以返回正确的 receiver 类型。
     */
    @Nullable
    public abstract MidiReceiver createReceiver(Instrument instrument, LivingEntity player, Vec3 pos);

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

    public String getRawName() {
        return rawName;
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

    public AllInstruments.Family getFamily() {
        return family;
    }

    public String getTool() {
        return tool;
    }

    public InteractionHand getToolHand() {
        return toolHand;
    }

    public InteractionHand getPlayHand() {
        return playHand;
    }

    public String getItemType() {
        return itemType;
    }

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
