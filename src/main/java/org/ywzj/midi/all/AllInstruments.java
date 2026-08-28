package org.ywzj.midi.all;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.ywzj.midi.custom.instrument.BaseInstrumentData;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.item.InstrumentEntityItem;
import org.ywzj.midi.item.InstrumentItem;
import org.ywzj.midi.item.InstrumentToolItem;
import org.ywzj.midi.util.MidiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AllInstruments {

    private static final ConcurrentHashMap<ResourceLocation, Instrument> ALL_INSTRUMENTS_BY_ID = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<ItemLike, Instrument> INSTRUMENTS_LOOKUP = new ConcurrentHashMap<>();

    public static void registerFromData(Map<ResourceLocation, BaseInstrumentData> dataMap) {
        ALL_INSTRUMENTS_BY_ID.clear();
        INSTRUMENTS_LOOKUP.clear();
        AllSounds.INSTRUMENT_WITH_SOUNDS.clear();
        for (var entry : dataMap.entrySet()) {
            BaseInstrumentData data = entry.getValue();
            Instrument instrument = new Instrument(data);
            ALL_INSTRUMENTS_BY_ID.put(instrument.getInstrumentId(), instrument);
            registerSounds(data);
            var itemLookup = AllItems.ITEMS_LOOKUP.get(data.getRawName());
            if (itemLookup != null) {
                INSTRUMENTS_LOOKUP.put(itemLookup.get(), instrument);
            }
            if (data.getFamily() != null) {
                instrument.setFamily(data.getFamily());
            }
        }
    }

    public static List<ItemStack> createCreativeTabStacks(Instrument instrument) {
        BaseInstrumentData data = instrument.getData();
        ResourceLocation id = data.getInstrumentId();
        Component name = data.getName();
        List<ItemStack> stacks = new ArrayList<>();
        if (data.getTool() != null) {
            stacks.add(InstrumentToolItem.createInstance(data));
        }
        String itemType = data.getItemType() != null ? data.getItemType() : "item";
        if (itemType.equals("entity")) {
            stacks.add(InstrumentEntityItem.createInstance(id, name));
        } else if (itemType.equals("block")) {
            var item = ForgeRegistries.ITEMS.getValue(id);
            if (item != null && item != Items.AIR) {
                stacks.add(item.getDefaultInstance());
            }
        } else {
            stacks.add(InstrumentItem.createInstance(id, name));
        }
        return stacks;
    }

    private static void registerSounds(BaseInstrumentData data) {
        int noteStart = MidiUtils.notationToNote(data.getKeyStart());
        int noteEnd = MidiUtils.notationToNote(data.getKeyEnd());
        AllSounds.registerKeys(data.getInstrumentId(), data.getRawName(), noteStart, noteEnd, "");
        for (var variant : data.getVariants()) {
            if (!"raw".equals(variant.getName())) {
                AllSounds.registerKeys(data.getInstrumentId(), data.getRawName(),
                        MidiUtils.notationToNote(variant.getKeyStart()),
                        MidiUtils.notationToNote(variant.getKeyEnd()),
                        variant.getName());
            }
        }
    }

    public static Instrument fromId(ResourceLocation id) {
        return ALL_INSTRUMENTS_BY_ID.get(id);
    }

    public static Instrument byItem(ItemLike item) {
        return INSTRUMENTS_LOOKUP.get(item);
    }

    public static List<Instrument> getInstruments() {
        return new ArrayList<>(ALL_INSTRUMENTS_BY_ID.values());
    }

    public enum Family {
        KEYBOARD,
        STRING,
        WOODWIND,
        BRASS,
        PERCUSSION,
        SPECIAL
    }

}
