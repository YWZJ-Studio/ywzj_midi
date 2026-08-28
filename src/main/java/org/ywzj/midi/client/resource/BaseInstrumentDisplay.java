package org.ywzj.midi.client.resource;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.animation.BedrockAnimation;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockAnimationFile;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockModelPOJO;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.ywzj.midi.YwzjMidi;

import java.util.*;

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
    @Nullable
    private final String script;
    private final Map<ItemDisplayContext, Display> displays;
    private final int tabIndex;

    public BaseInstrumentDisplay(ResourceLocation displayId, BedrockModelPOJO modelPojo,
                                 @Nullable BedrockAnimationFile animationFile,
                                 ResourceLocation texture, ResourceLocation slotTexture,
                                 @Nullable Map<String, BaseInstrumentDisplayPojo.SwitchableAnimationDefinitionPojo> switchableAnimationPojos,
                                 @Nullable String script,
                                 @Nullable Map<String, BaseInstrumentDisplayPojo.DisplayPojo> displayPojos,
                                 int tabIndex) {
        this.displayId = displayId;
        this.model = modelPojo != null ? new BedrockModel(modelPojo) : null;
        this.texture = texture;
        this.slotTexture = slotTexture;
        this.animations = createAnimations(animationFile);
        this.switchableAnimations = createSwitchableAnimations(switchableAnimationPojos);
        this.primarySwitchableAnimation = this.switchableAnimations.values().stream().findFirst().orElse(null);
        this.script = script;
        this.displays = parseDisplays(displayPojos);
        this.tabIndex = tabIndex;
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

    public int getTabIndex() {
        return tabIndex;
    }

    public boolean hasCustomItemRenderer() {
        return slotTexture != null || model != null && texture != null;
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

    @Nullable
    public String getScript() {
        return script;
    }

    @Nullable
    public Display getDisplay(ItemDisplayContext context) {
        return displays.get(context);
    }

    public static void applyDisplay(PoseStack poseStack, Display display) {
        if (display.translation != null) {
            poseStack.translate(display.translation.x, display.translation.y, display.translation.z);
        }
        if (display.rotation != null) {
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(display.rotation.y));
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(display.rotation.x));
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(display.rotation.z));
        }
        if (display.scale != null) {
            poseStack.scale(display.scale.x, display.scale.y, display.scale.z);
        }
    }

    private static Map<ItemDisplayContext, Display> parseDisplays(
            @Nullable Map<String, BaseInstrumentDisplayPojo.DisplayPojo> pojos) {
        if (pojos == null || pojos.isEmpty()) {
            return Map.of();
        }
        Map<ItemDisplayContext, Display> map = new EnumMap<>(ItemDisplayContext.class);
        for (var entry : pojos.entrySet()) {
            ItemDisplayContext context;
            try {
                context = ItemDisplayContext.valueOf(entry.getKey().toUpperCase());
            } catch (IllegalArgumentException e) {
                YwzjMidi.LOGGER.warn("Unknown display context '{}'", entry.getKey());
                continue;
            }
            BaseInstrumentDisplayPojo.DisplayPojo pojo = entry.getValue();
            map.put(context, Display.fromPojo(pojo));
        }
        return Collections.unmodifiableMap(map);
    }

    public record SwitchableAnimationDefinition(String key, String animation, boolean invert) {}

    public record Display(Vector3f translation, Vector3f rotation, Vector3f scale) {

        static Display fromPojo(BaseInstrumentDisplayPojo.DisplayPojo pojo) {
            return new Display(
                    pojo.translation != null ? new Vector3f(pojo.translation[0], pojo.translation[1], pojo.translation[2]) : new Vector3f(),
                    pojo.rotation != null ? new Vector3f(pojo.rotation[0], pojo.rotation[1], pojo.rotation[2]) : new Vector3f(),
                    pojo.scale != null ? new Vector3f(pojo.scale[0], pojo.scale[1], pojo.scale[2]) : new Vector3f(1)
            );
        }

    }

}
