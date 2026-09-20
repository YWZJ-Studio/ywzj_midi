package org.ywzj.midi.custom;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.api.custom.IInstrumentDataManager;
import org.ywzj.midi.api.custom.IInstrumentModelManager;
import org.ywzj.midi.network.Channel;
import org.ywzj.midi.network.CommonAssetsPayload;
import org.ywzj.midi.network.SliceReassembler;
import org.ywzj.midi.network.message.ServerSlicedPacket;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonAssetsManager {

    public static CommonAssetsManager INSTANCE;
    private final InstrumentModelManager instrumentModelManager = new InstrumentModelManager();
    private final InstrumentDataManager instrumentDataManager = new InstrumentDataManager();

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent event) {
        CommonAssetsManager manager = new CommonAssetsManager();
        event.addListener(manager.instrumentModelManager);
        event.addListener(manager.instrumentDataManager);
        event.addListener((barrier, resourceManager, preparationProfiler,
                           reloadProfiler, backgroundExecutor, gameExecutor) -> {
            return barrier.wait(Void.TYPE).thenRunAsync(() -> {
                        INSTANCE = manager;
                    }, gameExecutor);
        });
        // 首次加载时设置实例，避免重载步骤中无法访问前置的数据
        if (INSTANCE == null) {
            INSTANCE = manager;
        }
    }

    public void reload(ResourceManager resourceManager) {
        instrumentModelManager.apply(instrumentModelManager.prepare(resourceManager, null), null, null);
        instrumentDataManager.apply(instrumentDataManager.prepare(resourceManager, null), null, null);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        INSTANCE = null;
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (INSTANCE != null) {
            FriendlyByteBuf buf = new CommonAssetsPayload(
                    INSTANCE.instrumentModelManager.getCache(),
                    INSTANCE.instrumentDataManager.getCache()).encode();
            ServerSlicedPacket[] packets;
            try {
                // 先整体压缩，再分包。
                packets = SliceReassembler.sliceData(buf);
            } finally {
                buf.release();
            }

            for (var packet : packets) {
                if (event.getPlayer() != null) {
                    Channel.CHANNEL.send(PacketDistributor.PLAYER.with(event::getPlayer), packet);
                } else {
                    Channel.CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
                }
            }
        }
    }

    // 收到所有数据包后组装数据并重载
    public static void fromNetwork(FriendlyByteBuf buf) {
        try {
            CommonAssetsPayload payload = CommonAssetsPayload.decode(buf);
            InstrumentModelManager.fromNetwork(payload.instrumentModels());
            InstrumentDataManager.fromNetwork(payload.instrumentData());
        } catch (Exception exception) {
            YwzjMidi.LOGGER.error("Failed to read common assets from network", exception);
        }
    }

    public static IInstrumentModelManager instrumentModelManager() {
        return INSTANCE != null ? INSTANCE.instrumentModelManager : InstrumentModelManager.ClientCache.INSTANCE;
    }

    public static IInstrumentDataManager instrumentDataManager() {
        return INSTANCE != null ? INSTANCE.instrumentDataManager : InstrumentDataManager.ClientCache.INSTANCE;
    }

}
