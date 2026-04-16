package org.ywzj.midi.all;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.util.ComponentUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AllTabs {

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, YwzjMidi.MOD_ID);

    public static final List<Supplier<? extends ItemLike>> TAB_ITEMS = new ArrayList<>();

    public static final RegistryObject<CreativeModeTab> MIDI_TAB = TABS.register("tab", () ->
            CreativeModeTab
                    .builder()
                    .title(ComponentUtils.translatable("tab.ywzj_midi"))
                    .icon(AllItems.ITEMS_LOOKUP.get("aa775").get()::getDefaultInstance)
                    .displayItems((displayParams, output) -> TAB_ITEMS.forEach(itemLike -> output.accept(itemLike.get())))
                    .build());

    public static <T extends Item> void addToTab(RegistryObject<T> itemLike) {
        TAB_ITEMS.add(itemLike);
    }

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }

}
