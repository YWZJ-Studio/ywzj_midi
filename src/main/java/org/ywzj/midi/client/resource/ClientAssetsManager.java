package org.ywzj.midi.client.resource;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockAnimationFile;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockModelPOJO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ywzj.midi.custom.serialize.GsonUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public enum ClientAssetsManager {

    INSTANCE;

    private JsonDataManager<BedrockModelPOJO> models;
    private JsonDataManager<BedrockAnimationFile> animations;
    private InstrumentDisplayManager instrumentDisplayManager;

    public void registerListeners(Consumer<PreparableReloadListener> consumer) {
        models = new JsonDataManager<>(BedrockModelPOJO.class, GsonUtil.GSON, "models/bedrock", "BedrockModelPojo");
        animations = new JsonDataManager<>(BedrockAnimationFile.class, GsonUtil.GSON, "animations/bedrock", "BedrockAnimationPojo");
        instrumentDisplayManager = new InstrumentDisplayManager();

        consumer.accept(models);
        consumer.accept(animations);
        consumer.accept(instrumentDisplayManager);

        consumer.accept(new SimplePreparableReloadListener<Void>() {
            @Override
            @ParametersAreNonnullByDefault
            protected @NotNull Void prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                return null;
            }

            @Override
            @ParametersAreNonnullByDefault
            protected void apply(Void pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                models.clearData();
                animations.clearData();
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void reload(ResourceManager resourceManager) {
        models.apply(models.prepare(resourceManager, null), null, null);
        animations.apply(animations.prepare(resourceManager, null), null, null);
        instrumentDisplayManager.apply(instrumentDisplayManager.prepare(resourceManager, null), null, null);

        instrumentDisplayManager.getDisplayMap().values().forEach(display -> {
            try {
                if (display.getTexture() != null) {
                    Minecraft.getInstance().textureManager.register(display.getTexture(), new SimpleTexture(display.getTexture()));
                }
                if (display.getSlotTexture() != null) {
                    Minecraft.getInstance().textureManager.register(display.getSlotTexture(), new SimpleTexture(display.getSlotTexture()));
                }
            } catch (Exception e) {
                // texture may already be registered
            }
        });
    }

    @Nullable
    public Optional<BedrockModelPOJO> getModelPojo(ResourceLocation id) {
        if (models == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(models.getAllData().get(id));
    }

    @Nullable
    public Optional<BedrockAnimationFile> getAnimation(ResourceLocation id) {
        if (animations == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(animations.getAllData().get(id));
    }

    @NotNull
    public Optional<BaseInstrumentDisplay> getInstrumentDisplay(ResourceLocation id) {
        if (instrumentDisplayManager == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(instrumentDisplayManager.getDisplayMap().get(id));
    }

}