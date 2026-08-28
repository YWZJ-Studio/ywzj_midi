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
import org.ywzj.midi.all.AllInstrumentDataTypes;
import org.ywzj.midi.all.AllInstruments;
import org.ywzj.midi.api.custom.IInstrumentDataManager;
import org.ywzj.midi.custom.instrument.BaseInstrumentData;
import org.ywzj.midi.custom.instrument.InstrumentDataType;
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
            AllInstruments.registerFromData(indexes);
        }
    }

    private static Map<ResourceLocation, BaseInstrumentData> parseIndexes(Map<ResourceLocation, JsonElement> jsonMap) {
        ImmutableMap.Builder<ResourceLocation, BaseInstrumentData> builder = ImmutableMap.builder();
        for (var instrumentIdAndInstrumentDataJson : jsonMap.entrySet()) {
            ResourceLocation instrumentId = instrumentIdAndInstrumentDataJson.getKey();
            JsonElement instrumentDataJson = instrumentIdAndInstrumentDataJson.getValue();
            try {
                var obj = GsonHelper.convertToJsonObject(instrumentDataJson, "instrument data");
                String type = GsonHelper.getAsString(obj, "type", "ywzj_midi:generic");
                ResourceLocation typeId = ResourceLocation.tryParse(type);
                if (typeId == null) {
                    YwzjMidi.LOGGER.warn(MARKER, "Failed to load instrument data: {}, invalid type id {}", instrumentId, type);
                    continue;
                }

                InstrumentDataType<?> dataType = AllInstrumentDataTypes.getType(typeId);

                if (dataType == null) {
                    YwzjMidi.LOGGER.warn(MARKER, "Failed to load instrument data: {}, unknown type {}", instrumentId, typeId);
                    continue;
                }

                var data = dataType.parse(instrumentDataJson);
                if (data == null) {
                    YwzjMidi.LOGGER.warn(MARKER, "Failed to parse instrument data: {}", instrumentId);
                    continue;
                }
                data.setInstrumentId(instrumentId);
                data.setName(Component.translatable("instrument." + instrumentId.getNamespace() + "." + instrumentId.getPath()));
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
        AllInstruments.registerFromData(indexes);
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
