package org.ywzj.midi.custom;

import io.netty.buffer.Unpooled;
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
import org.ywzj.midi.network.SliceReassembler;

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
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeMap(INSTANCE.instrumentModelManager.getCache(),
                    FriendlyByteBuf::writeResourceLocation,
                    FriendlyByteBuf::writeUtf);
            buf.writeMap(INSTANCE.instrumentDataManager.getCache(),
                    FriendlyByteBuf::writeResourceLocation,
                    FriendlyByteBuf::writeUtf);

            // 切片发送
            var packets = SliceReassembler.sliceData(buf);

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
            var instrumentModelMap = buf.readMap(FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readUtf);
            var instrumentDataMap = buf.readMap(FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readUtf);

            InstrumentModelManager.fromNetwork(instrumentModelMap);
            InstrumentDataManager.fromNetwork(instrumentDataMap);
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
