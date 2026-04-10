package org.ywzj.midi.client.resource;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.NotNull;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.util.ResourceScanner;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class JsonDataManager<T> extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {

    protected final Map<ResourceLocation, T> dataMap = Maps.newHashMap();
    private final Gson gson;
    private final Class<T> dataClass;
    private final Marker marker;
    private final FileToIdConverter fileToIdConverter;

    public JsonDataManager(Class<T> dataClass, Gson pGson, String directory, String marker) {
        this(dataClass, pGson, FileToIdConverter.json(directory), marker);
    }

    public JsonDataManager(Class<T> dataClass, Gson pGson, FileToIdConverter fileToIdConverter, String marker) {
        this.gson = pGson;
        this.dataClass = dataClass;
        this.marker = MarkerManager.getMarker(marker);
        this.fileToIdConverter = fileToIdConverter;
    }

    @NotNull
    @Override
    public Map<ResourceLocation, JsonElement> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        return ResourceScanner.scanDirectory(pResourceManager, fileToIdConverter, this.gson);
    }

    @Override
    public void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        dataMap.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonElement element = entry.getValue();
            try {
                T data = gson.fromJson(element, dataClass);
                if (data != null) {
                    dataMap.put(id, data);
                }
            } catch (JsonParseException | IllegalArgumentException e) {
                YwzjMidi.LOGGER.error(marker, "Failed to load data file {}", id, e);
            }
        }
    }

    public T getData(ResourceLocation id) {
        return dataMap.get(id);
    }

    public Map<ResourceLocation, T> getAllData() {
        return dataMap;
    }

    public void clearData() {
        dataMap.clear();
    }

}
