package org.ywzj.midi.custom;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.api.custom.IInstrumentDataManager;
import org.ywzj.midi.api.custom.IInstrumentModelManager;
import org.ywzj.midi.network.Channel;
import org.ywzj.midi.network.SliceReassembler;

@EventBusSubscriber
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
                    (b, id) -> b.writeResourceLocation(id),
                    (b, value) -> b.writeUtf(value));
            buf.writeMap(INSTANCE.instrumentDataManager.getCache(),
                    (b, id) -> b.writeResourceLocation(id),
                    (b, value) -> b.writeUtf(value));

            var packets = SliceReassembler.sliceData(buf);
            for (var packet : packets) {
                if (event.getPlayer() != null) {
                    Channel.CHANNEL.sendToPlayer(event.getPlayer(), packet);
                } else {
                    Channel.CHANNEL.sendToAllPlayers(packet);
                }
            }
        }
    }

    public static void fromNetwork(FriendlyByteBuf buf) {
        try {
            var instrumentModelMap = buf.readMap(b -> b.readResourceLocation(), b -> b.readUtf());
            var instrumentDataMap = buf.readMap(b -> b.readResourceLocation(), b -> b.readUtf());
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
