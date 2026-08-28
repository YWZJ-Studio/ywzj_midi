package org.ywzj.midi.client.resource;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockAnimationFile;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockModelPOJO;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.custom.serialize.GsonUtil;
import org.ywzj.midi.script.MidiScriptPoseProvider;
import org.ywzj.midi.util.ResourceScanner;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class InstrumentDisplayManager extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {

    private Map<ResourceLocation, BaseInstrumentDisplay> displayMap = Map.of();

    @NotNull
    @Override
    public Map<ResourceLocation, JsonElement> prepare(@NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
        return ResourceScanner.scanDirectory(manager, "display/instrument", GsonUtil.GSON);
    }

    @Override
    public void apply(@NotNull Map<ResourceLocation, JsonElement> resources,
                      @NotNull ResourceManager manager,
                      @NotNull ProfilerFiller profiler) {
        ImmutableMap.Builder<ResourceLocation, BaseInstrumentDisplay> builder = ImmutableMap.builder();
        for (var entry : resources.entrySet()) {
            ResourceLocation displayId = entry.getKey();
            try {
                var obj = GsonHelper.convertToJsonObject(entry.getValue(), "instrument display");
                var pojo = GsonUtil.GSON.fromJson(obj, BaseInstrumentDisplayPojo.class);
                if (pojo == null) {
                    YwzjMidi.LOGGER.warn("Failed to parse instrument display: {}", displayId);
                    continue;
                }
                BedrockModelPOJO modelPojo = null;
                if (pojo.model != null) {
                    modelPojo = ClientAssetsManager.INSTANCE.getModelPojo(pojo.model).orElse(null);
                }
                BedrockAnimationFile animationFile = null;
                if (pojo.animations != null) {
                    animationFile = ClientAssetsManager.INSTANCE.getAnimation(pojo.animations).orElse(null);
                }
                var display = new BaseInstrumentDisplay(displayId, modelPojo, animationFile,
                        pojo.texture, pojo.slotTexture, pojo.switchableAnimations, pojo.script, pojo.display);
                builder.put(displayId, display);
                if (pojo.script != null) {
                    MidiScriptPoseProvider.setScriptOverride(displayId, pojo.script);
                }
            } catch (Exception e) {
                YwzjMidi.LOGGER.error("Failed to load instrument display: {}", displayId, e);
            }
        }
        displayMap = builder.build();
    }

    public Map<ResourceLocation, BaseInstrumentDisplay> getDisplayMap() {
        return displayMap;
    }

}
