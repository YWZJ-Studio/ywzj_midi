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

    public static final RegistryObject<Item> MUSIC_PLAYER = registerItem("music_player_item", () -> new MusicPlayerItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BATON = registerItem("baton_item", () -> new BatonItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> VIOLIN_BOW = registerItem("violin_bow_item", () -> new ViolinBowItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> VIOLA_BOW = registerItem("viola_bow_item", () -> new ViolaBowItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CELLO_BOW = registerItem("cello_bow_item", () -> new CelloBowItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DOUBLE_BASS_BOW = registerItem("double_bass_bow_item", () -> new DoubleBassBowItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FELT_MALLET = registerItem("felt_mallet_item", () -> new FeltMalletItem(new Item.Properties().stacksTo(16)));

    public static <T extends Item> RegistryObject<Item> registerItem(String name, Supplier<T> item) {
        return registerItem(YwzjMidi.MOD_ID, name, item);
    }

    public static <T extends Item> RegistryObject<Item> registerItem(String namespace, String name, Supplier<T> item) {
        DeferredRegister<Item> itemDeferredRegister = ITEMS.computeIfAbsent(namespace, k -> DeferredRegister.create(ForgeRegistries.ITEMS, namespace));
        RegistryObject<Item> registryObject = itemDeferredRegister.register(name, item);
        AllTabs.TAB_ITEMS.add(registryObject);
        AllItems.ITEMS_LOOKUP.put(name, registryObject);
        return registryObject;
    }

    public static void register(IEventBus eventBus, String namespace) {
        if (ITEMS.get(namespace) != null) {
            ITEMS.get(namespace).register(eventBus);
        }
    }

}
