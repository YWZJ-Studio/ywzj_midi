package org.ywzj.midi.client.resource;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.animation.BedrockAnimation;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockAnimationFile;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockModelPOJO;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.ywzj.midi.YwzjMidi;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side instrument display instance: bedrock model + texture + slot texture.
 */
public class BaseInstrumentDisplay {

    private final ResourceLocation displayId;
    private final BedrockModel model;
    private final ResourceLocation texture;
    private final ResourceLocation slotTexture;
    private final Map<String, BedrockAnimation> animations;
    private final Map<String, SwitchableAnimationDefinition> switchableAnimations;
    private final SwitchableAnimationDefinition primarySwitchableAnimation;

    public BaseInstrumentDisplay(ResourceLocation displayId, BedrockModelPOJO modelPojo,
                                 @Nullable BedrockAnimationFile animationFile,
                                 ResourceLocation texture, ResourceLocation slotTexture,
                                 @Nullable Map<String, BaseInstrumentDisplayPojo.SwitchableAnimationDefinitionPojo> switchableAnimationPojos) {
        this.displayId = displayId;
        this.model = modelPojo != null ? new BedrockModel(modelPojo) : null;
        this.texture = texture;
        this.slotTexture = slotTexture;
        this.animations = createAnimations(animationFile);
        this.switchableAnimations = createSwitchableAnimations(switchableAnimationPojos);
        this.primarySwitchableAnimation = this.switchableAnimations.values().stream().findFirst().orElse(null);
        validateSwitchableAnimations();
    }

    private Map<String, BedrockAnimation> createAnimations(@Nullable BedrockAnimationFile animationFile) {
        if (animationFile == null || model == null) {
            return Map.of();
        }
        List<BedrockAnimation> animationList = BedrockAnimation.createAnimation(animationFile, model);
        Map<String, BedrockAnimation> map = new LinkedHashMap<>();
        for (BedrockAnimation animation : animationList) {
            map.put(animation.getName(), animation);
        }
        return Collections.unmodifiableMap(map);
    }

    private Map<String, SwitchableAnimationDefinition> createSwitchableAnimations(
            @Nullable Map<String, BaseInstrumentDisplayPojo.SwitchableAnimationDefinitionPojo> switchableAnimationPojos) {
        if (switchableAnimationPojos == null || switchableAnimationPojos.isEmpty()) {
            return Map.of();
        }
        Map<String, SwitchableAnimationDefinition> map = new LinkedHashMap<>();
        for (Map.Entry<String, BaseInstrumentDisplayPojo.SwitchableAnimationDefinitionPojo> entry : switchableAnimationPojos.entrySet()) {
            BaseInstrumentDisplayPojo.SwitchableAnimationDefinitionPojo pojo = entry.getValue();
            if (pojo == null || pojo.animation == null || pojo.animation.isBlank()) {
                continue;
            }
            map.put(entry.getKey(), new SwitchableAnimationDefinition(entry.getKey(), pojo.animation, pojo.invert));
        }
        return Collections.unmodifiableMap(map);
    }

    private void validateSwitchableAnimations() {
        for (SwitchableAnimationDefinition definition : switchableAnimations.values()) {
            if (!animations.containsKey(definition.animation())) {
                YwzjMidi.LOGGER.warn("Missing switchable animation '{}' for instrument display {}", definition.animation(), displayId);
            }
        }
    }

    public ResourceLocation getDisplayId() {
        return displayId;
    }

    public BedrockModel getModel() {
        return model;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public ResourceLocation getSlotTexture() {
        return slotTexture;
    }

    public Map<String, BedrockAnimation> getAnimations() {
        return animations;
    }

    @Nullable
    public BedrockAnimation getAnimation(String name) {
        return animations.get(name);
    }

    public boolean hasPrimarySwitchableAnimation() {
        return primarySwitchableAnimation != null;
    }

    @Nullable
    public SwitchableAnimationDefinition getPrimarySwitchableAnimation() {
        return primarySwitchableAnimation;
    }

    public record SwitchableAnimationDefinition(String key, String animation, boolean invert) {}

}
