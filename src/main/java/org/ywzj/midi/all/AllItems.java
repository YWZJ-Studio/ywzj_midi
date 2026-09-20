package org.ywzj.midi.all;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.item.*;

import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class AllItems {

    public static final ConcurrentHashMap<String, DeferredRegister<Item>> ITEMS = new ConcurrentHashMap<>();
    public static final LinkedHashMap<String, DeferredHolder<Item, ? extends Item>> ITEMS_LOOKUP = new LinkedHashMap<>();

    public static final DeferredHolder<Item, MusicPlayerItem> MUSIC_PLAYER = registerItem("music_player_item", () -> new MusicPlayerItem(new Item.Properties().stacksTo(1)), true);
    public static final DeferredHolder<Item, BatonItem> BATON = registerItem("baton_item", () -> new BatonItem(new Item.Properties().stacksTo(1)), true);
    public static final DeferredHolder<Item, InstrumentItem> INSTRUMENT_ITEM = registerItem("instrument_item", () -> new InstrumentItem(new Item.Properties().stacksTo(1)), false);
    public static final DeferredHolder<Item, InstrumentEntityItem> INSTRUMENT_ENTITY_ITEM = registerItem("instrument_entity_item", () -> new InstrumentEntityItem(new Item.Properties().stacksTo(1)), false);
    public static final DeferredHolder<Item, InstrumentToolItem> INSTRUMENT_TOOL_ITEM = registerItem("instrument_tool_item", () -> new InstrumentToolItem(new Item.Properties().stacksTo(1)), false);
    public static final DeferredHolder<Item, PlainTextureItem> PLAIN_TEXTURE_ITEM = registerItem("plain_texture_item", () -> new PlainTextureItem(new Item.Properties().stacksTo(1)), false);
    // 仅用于读取java模型
    public static final DeferredHolder<Item, Item> VIOLIN = registerItem("violin", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final DeferredHolder<Item, Item> CFX = registerItem("cfx", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final DeferredHolder<Item, Item> VIOLA = registerItem("viola", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final DeferredHolder<Item, Item> CELLO = registerItem("cello", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final DeferredHolder<Item, Item> DOUBLE_BASS = registerItem("double_bass", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final DeferredHolder<Item, Item> OBOE = registerItem("oboe", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final DeferredHolder<Item, Item> CLARINET = registerItem("clarinet", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final DeferredHolder<Item, Item> FLUTE = registerItem("flute", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final DeferredHolder<Item, Item> BASSOON = registerItem("bassoon", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final DeferredHolder<Item, Item> HORN = registerItem("horn", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final DeferredHolder<Item, Item> TRUMPET = registerItem("trumpet", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final DeferredHolder<Item, Item> TROMBONE = registerItem("trombone", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final DeferredHolder<Item, Item> TUBA = registerItem("tuba", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final DeferredHolder<Item, Item> CYMBAL = registerItem("cymbal", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final DeferredHolder<Item, Item> VIOLIN_BOW = registerItem("violin_bow", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final DeferredHolder<Item, Item> VIOLA_BOW = registerItem("viola_bow", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final DeferredHolder<Item, Item> CELLO_BOW = registerItem("cello_bow", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final DeferredHolder<Item, Item> DOUBLE_BASS_BOW = registerItem("double_bass_bow", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final DeferredHolder<Item, Item> FELT_MALLET = registerItem("felt_mallet_item", () -> new Item(new Item.Properties().stacksTo(1)), false);

    public static <T extends Item> DeferredHolder<Item, T> registerItem(String name, Supplier<T> item, boolean tab) {
        return registerItem(YwzjMidi.MOD_ID, name, item, tab);
    }

    public static <T extends Item> DeferredHolder<Item, T> registerItem(String namespace, String name, Supplier<T> item, boolean tab) {
        DeferredRegister<Item> itemDeferredRegister = ITEMS.computeIfAbsent(namespace, k -> DeferredRegister.create(Registries.ITEM, namespace));
        DeferredHolder<Item, T> registryObject = itemDeferredRegister.register(name, item);
        if (tab) {
            AllTabs.MISC_ITEMS.add(registryObject);
        }
        AllItems.ITEMS_LOOKUP.put(name, registryObject);
        return registryObject;
    }

    public static void register(IEventBus eventBus, String namespace) {
        if (ITEMS.get(namespace) != null) {
            ITEMS.get(namespace).register(eventBus);
        }
    }

}
