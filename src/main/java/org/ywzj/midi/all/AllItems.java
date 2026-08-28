package org.ywzj.midi.all;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.item.*;

import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class AllItems {

    public static final ConcurrentHashMap<String, DeferredRegister<Item>> ITEMS = new ConcurrentHashMap<>();
    public static final LinkedHashMap<String, RegistryObject<Item>> ITEMS_LOOKUP = new LinkedHashMap<>();

    public static final RegistryObject<Item> MUSIC_PLAYER = registerItem("music_player_item", () -> new MusicPlayerItem(new Item.Properties().stacksTo(1)), true);
    public static final RegistryObject<Item> BATON = registerItem("baton_item", () -> new BatonItem(new Item.Properties().stacksTo(1)), true);
    public static final RegistryObject<Item> INSTRUMENT_ITEM = registerItem("instrument_item", () -> new InstrumentItem(new Item.Properties().stacksTo(1)), false);
    public static final RegistryObject<Item> INSTRUMENT_ENTITY_ITEM = registerItem("instrument_entity_item", () -> new InstrumentEntityItem(new Item.Properties().stacksTo(1)), false);
    public static final RegistryObject<Item> INSTRUMENT_TOOL_ITEM = registerItem("instrument_tool_item", () -> new InstrumentToolItem(new Item.Properties().stacksTo(1)), false);
    // 仅用于读取java模型
    public static final RegistryObject<Item> VIOLIN = registerItem("violin", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final RegistryObject<Item> U1H = registerItem("u1h", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final RegistryObject<Item> CFX = registerItem("cfx", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final RegistryObject<Item> VIOLA = registerItem("viola", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final RegistryObject<Item> CELLO = registerItem("cello", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final RegistryObject<Item> DOUBLE_BASS = registerItem("double_bass", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final RegistryObject<Item> OBOE = registerItem("oboe", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final RegistryObject<Item> CLARINET = registerItem("clarinet", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final RegistryObject<Item> FLUTE = registerItem("flute", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final RegistryObject<Item> BASSOON = registerItem("bassoon", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final RegistryObject<Item> HORN = registerItem("horn", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final RegistryObject<Item> TRUMPET = registerItem("trumpet", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final RegistryObject<Item> TROMBONE = registerItem("trombone", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final RegistryObject<Item> TUBA = registerItem("tuba", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final RegistryObject<Item> CYMBAL = registerItem("cymbal", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final RegistryObject<Item> VIOLIN_BOW = registerItem("violin_bow", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final RegistryObject<Item> VIOLA_BOW = registerItem("viola_bow", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final RegistryObject<Item> CELLO_BOW = registerItem("cello_bow", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final RegistryObject<Item> DOUBLE_BASS_BOW = registerItem("double_bass_bow", () -> new Item(new Item.Properties().stacksTo(1)), false);
    public static final RegistryObject<Item> FELT_MALLET = registerItem("felt_mallet_item", () -> new Item(new Item.Properties().stacksTo(16)), false);
    public static final RegistryObject<Item> AA775 = registerItem("aa775", () -> new Item(new Item.Properties().stacksTo(1)), false);

    public static <T extends Item> RegistryObject<Item> registerItem(String name, Supplier<T> item, boolean tab) {
        return registerItem(YwzjMidi.MOD_ID, name, item, tab);
    }

    public static <T extends Item> RegistryObject<Item> registerItem(String namespace, String name, Supplier<T> item, boolean tab) {
        DeferredRegister<Item> itemDeferredRegister = ITEMS.computeIfAbsent(namespace, k -> DeferredRegister.create(ForgeRegistries.ITEMS, namespace));
        RegistryObject<Item> registryObject = itemDeferredRegister.register(name, item);
        if (tab) {
            AllTabs.TAB_ITEMS.add(registryObject);
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
