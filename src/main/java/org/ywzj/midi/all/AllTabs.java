package org.ywzj.midi.all;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.client.resource.ClientAssetsManager;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.resource.InstrumentPackLoader;
import org.ywzj.midi.util.ComponentUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class AllTabs {

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, YwzjMidi.MOD_ID);

    public static final List<Supplier<? extends ItemLike>> MISC_ITEMS = new ArrayList<>();

    public static final RegistryObject<CreativeModeTab> TAB_MISC = TABS.register("tab_misc", () ->
            CreativeModeTab
                    .builder()
                    .title(ComponentUtils.translatable("tab.ywzj_midi.misc"))
                    .icon(AllItems.BATON.get()::getDefaultInstance)
                    .displayItems((displayParams, output) ->
                            MISC_ITEMS.forEach(itemLike -> output.accept(itemLike.get())))
                    .build());

    public static void register(IEventBus eventBus) {
        InstrumentPackLoader.INSTANCE.getInstrumentPacks().forEach(instrumentPack -> {
            String namespace = instrumentPack.meta().getNamespace();
            Supplier<ItemStack> icon = () -> AllItems.PLAIN_TEXTURE_ITEM.get().createInstance(YwzjMidi.resourceLocation(namespace, "textures/tab.png"));
            addInstrumentTab("tab_" + namespace, Component.translatable(instrumentPack.meta().getTitle()), icon, namespace);
        });
        TABS.register(eventBus);
    }

    private static void addInstrumentTab(String name, Component title, Supplier<ItemStack> icon, String namespace) {
        TABS.register(name, () -> CreativeModeTab.builder()
                .title(title)
                .icon(icon)
                .displayItems((displayParams, output) -> AllInstruments.getInstruments().stream()
                        .filter(instrument -> namespace.equals(instrument.getInstrumentId().getNamespace()))
                        .sorted(Comparator.comparingInt(AllTabs::getTabIndex)
                                .thenComparing(instrument -> instrument.getInstrumentId().toString()))
                        .flatMap(instrument -> AllInstruments.createCreativeTabStacks(instrument).stream())
                        .forEach(output::accept))
                .build());
    }

    private static int getTabIndex(Instrument instrument) {
        return ClientAssetsManager.INSTANCE.getInstrumentDisplay(instrument.getInstrumentId())
                .orElseThrow(() -> new IllegalStateException(
                        "Missing instrument display for creative tab: " + instrument.getInstrumentId()))
                .getTabIndex();
    }

}
