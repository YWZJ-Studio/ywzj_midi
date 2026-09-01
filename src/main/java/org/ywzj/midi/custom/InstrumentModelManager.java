package org.ywzj.midi.custom;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockModelPOJO;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.api.custom.IInstrumentModelManager;
import org.ywzj.midi.custom.serialize.GsonUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.Optional;

import static org.ywzj.midi.util.ResourceScanner.scanDirectory;

@ParametersAreNonnullByDefault
public class InstrumentModelManager extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> implements IInstrumentModelManager {

    private final FileToIdConverter fileToIdConverter = new FileToIdConverter("models/bedrock", ".instrument.json");
    private Map<ResourceLocation, BedrockModel> instrumentModels = Map.of();
    private Map<ResourceLocation, String> cache = Map.of();

    // Client-side cache for models received from server
    enum ClientCache implements IInstrumentModelManager {
        INSTANCE;
        private Map<ResourceLocation, BedrockModel> indexes = Map.of();

        @Override
        public Optional<BedrockModel> getInstrumentModel(ResourceLocation id) {
            return Optional.ofNullable(indexes.get(id));
        }

        @Override
        public Map<ResourceLocation, BedrockModel> getInstrumentModels() {
            return indexes;
        }

        public void fromNetwork(Map<ResourceLocation, String> map) {
            Map<ResourceLocation, JsonElement> pojoMap = Maps.newHashMap();
            for (var entry : map.entrySet()) {
                try {
                    JsonElement pojo = GsonUtil.GSON.fromJson(entry.getValue(), JsonElement.class);
                    if (pojo != null) pojoMap.put(entry.getKey(), pojo);
                } catch (Exception e) {
                    YwzjMidi.LOGGER.error("Failed to parse bedrock model from network: {}", entry.getKey(), e);
                }
            }
            indexes = parseIndexes(pojoMap);
        }
    }

    @NotNull
    @Override
    public Map<ResourceLocation, JsonElement> prepare(ResourceManager manager, ProfilerFiller pProfiler) {
        var map = scanDirectory(manager, fileToIdConverter, GsonUtil.GSON);
        ImmutableMap.Builder<ResourceLocation, String> builder = ImmutableMap.builder();
        for (var entry : map.entrySet()) {
            builder.put(entry.getKey(), entry.getValue().toString());
        }
        cache = builder.build();
        return map;
    }

    private static Map<ResourceLocation, BedrockModel> parseIndexes(Map<ResourceLocation, JsonElement> map) {
        ImmutableMap.Builder<ResourceLocation, BedrockModel> builder = ImmutableMap.builder();
        for (var entry : map.entrySet()) {
            try {
                var pojo = GsonUtil.GSON.fromJson(entry.getValue(), BedrockModelPOJO.class);
                BedrockModel model = new BedrockModel(pojo);
                builder.put(entry.getKey(), model);
            } catch (Exception e) {
                YwzjMidi.LOGGER.error("Failed to load instrument model: {}", entry.getKey(), e);
            }
        }
        return builder.build();
    }

    @Override
    public void apply(Map<ResourceLocation, JsonElement> map, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        instrumentModels = parseIndexes(map);
    }

    @Override
    public Map<ResourceLocation, BedrockModel> getInstrumentModels() {
        return instrumentModels;
    }

    @Override
    public Optional<BedrockModel> getInstrumentModel(ResourceLocation location) {
        return Optional.ofNullable(instrumentModels.get(location));
    }

    public Map<ResourceLocation, String> getCache() {
        return cache;
    }

    public static void fromNetwork(Map<ResourceLocation, String> map) {
        ClientCache.INSTANCE.fromNetwork(map);
    }

}
