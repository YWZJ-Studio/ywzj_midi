package org.ywzj.midi.custom;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.NotNull;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.api.custom.IInstrumentDataManager;
import org.ywzj.midi.custom.instrument.BaseInstrumentData;
import org.ywzj.midi.custom.instrument.BaseInstrumentDataPojo;
import org.ywzj.midi.custom.serialize.GsonUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.ywzj.midi.util.ResourceScanner.scanDirectory;

@ParametersAreNonnullByDefault
public class InstrumentDataManager extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> implements IInstrumentDataManager {

    public static final Marker MARKER = MarkerManager.getMarker("InstrumentDataManager");

    enum ClientCache implements IInstrumentDataManager {
        INSTANCE;
        private Map<ResourceLocation, BaseInstrumentData> indexes = Map.of();

        @Override
        public Optional<BaseInstrumentData> getInstrumentData(ResourceLocation id) {
            return Optional.ofNullable(indexes.get(id));
        }

        @Override
        public Map<ResourceLocation, BaseInstrumentData> getInstrumentData() {
            return indexes;
        }

        public void fromNetwork(Map<ResourceLocation, String> map) {
            Map<ResourceLocation, JsonElement> jsonMap = new HashMap<>();
            for (var entry : map.entrySet()) {
                var ele = GsonUtil.GSON.fromJson(entry.getValue(), JsonElement.class);
                if (ele != null) {
                    jsonMap.put(entry.getKey(), ele);
                }
            }
            indexes = parseIndexes(jsonMap);
        }
    }

    private static Map<ResourceLocation, BaseInstrumentData> parseIndexes(Map<ResourceLocation, JsonElement> jsonMap) {
        ImmutableMap.Builder<ResourceLocation, BaseInstrumentData> builder = ImmutableMap.builder();
        for (var instrumentIdAndInstrumentDataJson : jsonMap.entrySet()) {
            ResourceLocation instrumentId = instrumentIdAndInstrumentDataJson.getKey();
            JsonElement instrumentDataJson = instrumentIdAndInstrumentDataJson.getValue();
            try {
                var obj = GsonHelper.convertToJsonObject(instrumentDataJson, "instrument data");
                // For now, we assume generic type; could parse "type" field in future
                var pojo = GsonUtil.GSON.fromJson(instrumentDataJson, BaseInstrumentDataPojo.class);
                if (pojo == null) {
                    YwzjMidi.LOGGER.warn(MARKER, "Failed to parse instrument data: {}", instrumentId);
                    continue;
                }
                var data = new BaseInstrumentData();
                data.setInstrumentId(instrumentId);
                data.setName(Component.translatable("instrument." + instrumentId.getNamespace() + "." + instrumentId.getPath()));
                data.build(pojo);
                builder.put(instrumentId, data);
            } catch (Exception e) {
                YwzjMidi.LOGGER.error(MARKER, "Failed to load instrument data: {}", instrumentId, e);
            }
        }
        return builder.build();
    }

    private Map<ResourceLocation, String> cache = Map.of();
    private Map<ResourceLocation, BaseInstrumentData> indexes = Map.of();

    @NotNull
    @Override
    public Map<ResourceLocation, JsonElement> prepare(ResourceManager manager, ProfilerFiller profiler) {
        var map = scanDirectory(manager, "instruments", GsonUtil.GSON);
        ImmutableMap.Builder<ResourceLocation, String> builder = ImmutableMap.builder();
        for (var entry : map.entrySet()) {
            builder.put(entry.getKey(), entry.getValue().toString());
        }
        cache = builder.build();
        return map;
    }

    @Override
    public void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager manager, ProfilerFiller profiler) {
        indexes = parseIndexes(resources);
    }

    public static void fromNetwork(Map<ResourceLocation, String> map) {
        ClientCache.INSTANCE.fromNetwork(map);
    }

    @Override
    public Optional<BaseInstrumentData> getInstrumentData(ResourceLocation id) {
        return Optional.ofNullable(indexes.get(id));
    }

    @Override
    public Map<ResourceLocation, BaseInstrumentData> getInstrumentData() {
        return indexes;
    }

    public Map<ResourceLocation, String> getCache() {
        return cache;
    }

}
