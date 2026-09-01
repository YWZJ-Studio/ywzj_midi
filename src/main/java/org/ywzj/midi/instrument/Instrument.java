package org.ywzj.midi.instrument;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.ywzj.midi.all.AllInstruments;
import org.ywzj.midi.custom.instrument.BaseInstrumentData;
import org.ywzj.midi.instrument.receiver.MidiReceiver;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Instrument {

    private static final AtomicInteger indexGenerator = new AtomicInteger(0);
    private final Integer index;
    private final BaseInstrumentData data;
    private AllInstruments.Family family;

    public Instrument(BaseInstrumentData data) {
        this.index = indexGenerator.addAndGet(1);
        this.data = data;
    }

    public AllInstruments.Family getFamily() {
        return family;
    }

    public void setFamily(AllInstruments.Family family) {
        this.family = family;
    }

    public MidiReceiver receiver(LivingEntity player, Vec3 pos) {
        return data.createReceiver(this, player, pos);
    }

    public String getName() {
        return data.getRawName();
    }

    public Integer getIndex() {
        return index;
    }

    public ResourceLocation getInstrumentId() {
        return data.getInstrumentId();
    }

    public Boolean isLoop() {
        return data.isLoop();
    }

    public Boolean isPortable() {
        return data.isPortable();
    }

    public String getKeyStart() {
        return data.getKeyStart();
    }

    public String getKeyEnd() {
        return data.getKeyEnd();
    }

    public Variant getVariant(int variantId) {
        if (variantId <= 0) {
            return new Variant(0, "raw", data.isLoop(), data.getKeyStart(), data.getKeyEnd());
        }
        int index = 0;
        for (BaseInstrumentData.Variant variant : data.getVariants()) {
            if ("raw".equals(variant.getName())) {
                continue;
            }
            if (index++ == variantId - 1) {
                return new Variant(variantId, variant.getName(), variant.isLoop(), variant.getKeyStart(), variant.getKeyEnd());
            }
        }
        return new Variant(0, "raw", data.isLoop(), data.getKeyStart(), data.getKeyEnd());
    }

    public Collection<Variant> getAllVariants() {
        LinkedHashMap<Integer, Variant> map = new LinkedHashMap<>();
        map.put(0, new Variant(0, "raw", data.isLoop(), data.getKeyStart(), data.getKeyEnd()));
        for (BaseInstrumentData.Variant v : data.getVariants()) {
            if ("raw".equals(v.getName())) continue;
            map.put(map.size(), new Variant(map.size(), v.getName(), v.isLoop(), v.getKeyStart(), v.getKeyEnd()));
        }
        return map.values();
    }

    public BaseInstrumentData getData() {
        return data;
    }

    public static class Variant {

        private final Integer index;
        private final String name;
        private final boolean loop;
        private final String keyStart;
        private final String keyEnd;

        public Variant(Integer index, String name, boolean loop, String keyStart, String keyEnd) {
            this.index = index;
            this.name = name;
            this.loop = loop;
            this.keyStart = keyStart;
            this.keyEnd = keyEnd;
        }

        public Integer getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        public boolean isLoop() {
            return loop;
        }

        public String getKeyStart() {
            return keyStart;
        }

        public String getKeyEnd() {
            return keyEnd;
        }
    }

}
