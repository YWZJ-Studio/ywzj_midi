package org.ywzj.midi.all;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.block.AABlock;
import org.ywzj.midi.block.BassDrumBlock;
import org.ywzj.midi.block.TimpaniBlock;
import org.ywzj.midi.block.piano.CFXBlock;
import org.ywzj.midi.block.piano.U1HBlock;
import org.ywzj.midi.instrument.*;
import org.ywzj.midi.item.*;
import org.ywzj.midi.util.MidiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class AllInstruments {

    private final static ConcurrentHashMap<Integer, Instrument> ALL_INSTRUMENTS = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<ItemLike, Instrument> INSTRUMENTS_LOOKUP = new ConcurrentHashMap<>();

    public static final Instrument U1H = registerInstrument(() -> new UprightPiano("u1h", false, false, "a1", "c8"), () -> new U1HBlock(BlockBehaviour.Properties.of().strength(1f)), Type.BLOCK);
    public static final Instrument CFX = registerInstrument(() -> new GrandPiano("cfx", false, false, "a1", "c8"), () -> new CFXBlock(BlockBehaviour.Properties.of().strength(1f)), Type.BLOCK);
    public static final Instrument VIOLIN = registerInstrument(() -> new Violin("violin", true, true, "g3", "e7").extra("pizz", false), () -> new ViolinItem(new Item.Properties().stacksTo(1)), Type.ITEM);
    public static final Instrument VIOLA = registerInstrument(() -> new Viola("viola", true, true, "c3", "f6").extra("pizz", false), () -> new ViolaItem(new Item.Properties().stacksTo(1)), Type.ITEM);
    public static final Instrument CELLO = registerInstrument(() -> new Cello("cello", true, true, "c2", "g5").extra("pizz", false, "c2", "cs5"), () -> new CelloItem(new Item.Properties().stacksTo(1)), Type.ITEM);
    public static final Instrument DOUBLE_BASS = registerInstrument(() -> new DoubleBass("double_bass", true, true, "c1", "cs4").extra("pizz", false), () -> new DoubleBassItem(new Item.Properties().stacksTo(1)), Type.ITEM);
    public static final Instrument OBOE = registerInstrument(() -> new Oboe("oboe", true, true, "c4", "ds6"), () -> new OboeItem(new Item.Properties().stacksTo(1)), Type.ITEM);
    public static final Instrument CLARINET = registerInstrument(() -> new Clarinet("clarinet", true, true, "d3", "ds6"), () -> new ClarinetItem(new Item.Properties().stacksTo(1)), Type.ITEM);
    public static final Instrument FLUTE = registerInstrument(() -> new Flute("flute", true, true, "c4", "c7"), () -> new FluteItem(new Item.Properties().stacksTo(1)), Type.ITEM);
    public static final Instrument BASSOON = registerInstrument(() -> new Bassoon("bassoon", true, true, "a2", "b5"), () -> new BassoonItem(new Item.Properties().stacksTo(1)), Type.ITEM);
    public static final Instrument HORN = registerInstrument(() -> new Horn("horn", false, true, "c2", "g5"), () -> new HornItem(new Item.Properties().stacksTo(1)), Type.ITEM);
    public static final Instrument TRUMPET = registerInstrument(() -> new Trumpet("trumpet", false, true, "c3", "e6"), () -> new TrumpetItem(new Item.Properties().stacksTo(1)), Type.ITEM);
    public static final Instrument TROMBONE = registerInstrument(() -> new Trombone("trombone", false, true, "c1", "e5"), () -> new TromboneItem(new Item.Properties().stacksTo(1)), Type.ITEM);
    public static final Instrument TUBA = registerInstrument(() -> new Tuba("tuba", false, true, "gs0", "e4"), () -> new TubaItem(new Item.Properties().stacksTo(1)), Type.ITEM);
    public static final Instrument TIMPANI = registerInstrument(() -> new Timpani("timpani", false, false, "c2", "as4"), () -> new TimpaniBlock(BlockBehaviour.Properties.of().strength(1f)), Type.BLOCK);
    public static final Instrument BASS_DRUM = registerInstrument(() -> new BassDrum("bass_drum", false, false, "f1", "c2"), () -> new BassDrumBlock(BlockBehaviour.Properties.of().strength(1f)), Type.BLOCK);
    public static final Instrument CYMBAL = registerInstrument(() -> new Cymbal("cymbal", false, false, "c1", "c1"), () -> new CymbalItem(new Item.Properties().stacksTo(1)), Type.ITEM);
    public static final Instrument AA775 = registerInstrument(() -> new AA775("aa775", false, false, "n", "n"), () -> new AABlock(BlockBehaviour.Properties.of().strength(1f).requiresCorrectToolForDrops()), Type.BLOCK);

    public static void preRegister() {}

    public static Instrument registerInstrument(Supplier<Instrument> instrumentSupplier, Supplier<ItemLike> registrySupplier, Type type) {
        return registerInstrument(YwzjMidi.MOD_ID, instrumentSupplier, registrySupplier, type);
    }

    public static Instrument registerInstrument(String namespace, Supplier<Instrument> instrumentSupplier, Supplier<ItemLike> registrySupplier, Type type) {
        Instrument instrument = instrumentSupplier.get();
        if (type.equals(Type.BLOCK)) {
            AllBlocks.registerBlock(namespace, instrument.getName(), () -> {
                Block block = (Block) registrySupplier.get();
                INSTRUMENTS_LOOKUP.put(block, instrument);
                return block;
            });
        } else if (type.equals(Type.ITEM)) {
            AllItems.registerItem(namespace, instrument.getName(), () -> {
                Item item = (Item) registrySupplier.get();
                INSTRUMENTS_LOOKUP.put(item, instrument);
                return item;
            });
        }
        AllSounds.registerKeys(namespace, instrument.getName(), MidiUtils.notationToNote(instrument.getKeyStart()), MidiUtils.notationToNote(instrument.getKeyEnd()), "");
        for (Instrument.Variant variant : instrument.getAllVariants()) {
            if (variant.getIndex().equals(0) && variant.getName().equals("raw")) {
                continue;
            }
            AllSounds.registerKeys(namespace, instrument.getName(), MidiUtils.notationToNote(variant.getKeyStart()), MidiUtils.notationToNote(variant.getKeyEnd()), variant.getName());
        }
        ALL_INSTRUMENTS.put(instrument.getIndex(), instrument);
        YwzjMidi.LOGGER.info("Registering instrument {} with id {} by {}", instrument.getName(), instrument.getIndex(), namespace);
        return instrument;
    }

    public static Instrument fromIndex(Integer index) {
        return ALL_INSTRUMENTS.get(index) == null ? AllInstruments.U1H : ALL_INSTRUMENTS.get(index);
    }

    public static List<Instrument> getInstruments() {
        return new ArrayList<>(ALL_INSTRUMENTS.values());
    }

    public enum Type {
        ITEM,
        BLOCK
    }

}
