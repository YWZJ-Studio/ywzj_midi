package org.ywzj.midi.pose.handler;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.all.AllItems;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TromboneSlideHandler extends NotesHandler {

    private static final ConcurrentHashMap<UUID, ConcurrentLinkedQueue<Integer>> SLIDES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Integer> CACHE_SLIDE = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation getInstrumentId() {
        return YwzjMidi.modLocation("trombone");
    }

    @Override
    public void handle(UUID playerUuid, List<Integer> notes) {
        Integer slide = noteToSlide(notes.get(0));
        SLIDES.computeIfAbsent(playerUuid, k -> new ConcurrentLinkedQueue<>());
        SLIDES.get(playerUuid).add(slide);
    }

    public static Integer noteToSlide(Integer note) {
        return switch (note % 12) {
            case 0, 1 -> 6;
            case 2, 3 -> 5;
            case 4, 5 -> 4;
            case 6, 7 -> 3;
            case 8, 9 -> 2;
            case 10 -> 1;
            case 11 -> 0;
            default -> 0;
        };
    }

    @SubscribeEvent
    public static void propertyOverrideRegistry(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(AllItems.INSTRUMENT_ITEM.get(),
                new ResourceLocation(YwzjMidi.MOD_ID, "note"),
                (itemStack, clientWorld, player, tag) -> {
                    if (player != null) {
                        SLIDES.computeIfAbsent(player.getUUID(), k -> new ConcurrentLinkedQueue<>());
                        Integer slide = SLIDES.get(player.getUUID()).poll();
                        if (slide != null) {
                            CACHE_SLIDE.put(player.getUUID(), slide);
                        }
                        return CACHE_SLIDE.getOrDefault(player.getUUID(), 7);
                    }
                    return 7;
                }));
    }

}
