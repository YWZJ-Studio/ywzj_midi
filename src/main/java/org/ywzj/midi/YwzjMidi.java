package org.ywzj.midi;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ywzj.midi.all.*;
import org.ywzj.midi.client.render.entity.InstrumentRenderer;
import org.ywzj.midi.network.Channel;
import org.ywzj.midi.render.renderer.FakePlayerRenderer;
import org.ywzj.midi.render.renderer.SeatRenderer;
import org.ywzj.midi.resource.InstrumentPackLoader;

@Mod(YwzjMidi.MODID)
public class YwzjMidi {

    public static final String MODID = "ywzj_midi";
    public static final String PROTOCOL = "1.6";
    public static final String CHANNEL = "ywzj_midi_channel";
    public static final Logger LOGGER = LogManager.getLogger(YwzjMidi.class);

    public YwzjMidi()
    {
        Dist side = FMLLoader.getDist();
        InstrumentPackLoader.INSTANCE.packType = side.isClient() ? PackType.CLIENT_RESOURCES : PackType.SERVER_DATA;
        InstrumentPackLoader.INSTANCE.scanInstrumentPacks();
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        AllInstruments.preRegister();
        AllHoldPose.preRegister();
        AllNotesHandler.preRegister();
        AllConfigs.register(ModLoadingContext.get());
        AllBlockEntities.register(eventBus);
        AllEntities.register(eventBus);
        AllTabs.register(eventBus);
        register(eventBus, MODID);
        eventBus.register(Channel.class);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    public static void register(IEventBus eventBus, String namespace) {
        AllItems.register(eventBus, namespace);
        AllBlocks.register(eventBus, namespace);
        AllSounds.register(eventBus, namespace);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> EntityRenderers.register(AllEntities.SEAT.get(), SeatRenderer::new));
        event.enqueueWork(() -> EntityRenderers.register(AllEntities.FAKE_PLAYER.get(), FakePlayerRenderer::new));
        event.enqueueWork(() -> EntityRenderers.register(AllEntities.GENERIC_INSTRUMENT.get(), InstrumentRenderer::new));
    }

}
