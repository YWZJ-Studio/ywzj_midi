package org.ywzj.midi.all;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.item.InstrumentItem;
import org.ywzj.midi.util.ComponentUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AllTabs {

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, YwzjMidi.MOD_ID);

    public static final List<Supplier<? extends ItemLike>> TAB_ITEMS = new ArrayList<>();
    public static final List<Supplier<ItemStack>> INSTRUMENT_STACKS = new ArrayList<>();

    public static final RegistryObject<CreativeModeTab> MIDI_TAB = TABS.register("tab", () ->
            CreativeModeTab
                    .builder()
                    .title(ComponentUtils.translatable("tab.ywzj_midi"))
                    .icon(() -> InstrumentItem.createInstance(YwzjMidi.modLocation("d274"), ComponentUtils.literal("D-274")))
                    .displayItems((displayParams, output) -> {
                        TAB_ITEMS.forEach(itemLike -> output.accept(itemLike.get()));
                        INSTRUMENT_STACKS.forEach(supplier -> output.accept(supplier.get()));
                    })
                    .build());

    public static <T extends Item> void addToTab(RegistryObject<T> itemLike) {
        TAB_ITEMS.add(itemLike);
    }

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }

}
