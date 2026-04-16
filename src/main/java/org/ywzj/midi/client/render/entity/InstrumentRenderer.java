package org.ywzj.midi.client.render.entity;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.animation.BedrockAnimation;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.maydaymemory.mae.basic.ArrayPoseBuilder;
import com.maydaymemory.mae.basic.DummyPose;
import com.maydaymemory.mae.basic.Pose;
import com.maydaymemory.mae.basic.ZYXBoneTransformFactory;
import com.maydaymemory.mae.blend.EulerAdditiveBlender;
import com.maydaymemory.mae.blend.SimpleEulerAdditiveBlender;
import com.maydaymemory.mae.control.runner.AnimationContext;
import com.maydaymemory.mae.control.runner.AnimationRunner;
import com.maydaymemory.mae.control.runner.PauseState;
import com.maydaymemory.mae.control.runner.PlayingState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.ywzj.midi.client.resource.BaseInstrumentDisplay;
import org.ywzj.midi.client.resource.ClientAssetsManager;
import org.ywzj.midi.entity.InstrumentEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InstrumentRenderer extends EntityRenderer<InstrumentEntity> {

    private static final EulerAdditiveBlender BLENDER = new SimpleEulerAdditiveBlender(new ZYXBoneTransformFactory(), ArrayPoseBuilder::new);
    private static final Map<Integer, SwitchableAnimationState> SWITCHABLE_ANIMATION_STATES = new ConcurrentHashMap<>();

    public InstrumentRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(InstrumentEntity entity, float pEntityYaw, float pPartialTick,
                       PoseStack pPoseStack, MultiBufferSource bufferSource, int pPackedLight) {
        ResourceLocation displayId = entity.getDisplayId();
        if (displayId == null) {
            return;
        }
        BaseInstrumentDisplay display = ClientAssetsManager.INSTANCE.getInstrumentDisplay(displayId).orElse(null);
        if (display == null) {
            return;
        }
        BedrockModel model = display.getModel();
        ResourceLocation texture = display.getTexture();
        if (model == null || texture == null) {
            return;
        }

        SwitchableAnimationState animationState = getAnimationState(entity, display);
        animationState.tick(entity.isSwitchableOn());

        pPoseStack.pushPose();
        super.render(entity, pEntityYaw, pPartialTick, pPoseStack, bufferSource, pPackedLight);
        model.applyPose(BLENDER.blend(model.getBindPose(), animationState.evaluatePose()));
        VertexConsumer builder = bufferSource.getBuffer(RenderType.entityCutout(texture));
        model.renderToBuffer(pPoseStack, builder, pPackedLight, OverlayTexture.NO_OVERLAY);
        model.applyPose(model.getBindPose());
        pPoseStack.popPose();
    }

    private SwitchableAnimationState getAnimationState(InstrumentEntity entity, BaseInstrumentDisplay display) {
        return SWITCHABLE_ANIMATION_STATES.compute(entity.getId(), (id, existing) -> {
            if (existing == null || !existing.matches(display)) {
                return new SwitchableAnimationState(display, entity.isSwitchableOn());
            }
            return existing;
        });
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull InstrumentEntity pEntity) {
        return new ResourceLocation("missingno");
    }

    private static class SwitchableAnimationState {
        private final ResourceLocation displayId;
        private final boolean invert;
        private final AnimationRunner runner;
        private boolean lastState;

        private SwitchableAnimationState(BaseInstrumentDisplay display, boolean initialState) {
            this.displayId = display.getDisplayId();
            BaseInstrumentDisplay.SwitchableAnimationDefinition definition = display.getPrimarySwitchableAnimation();
            BedrockAnimation animation = definition != null ? display.getAnimation(definition.animation()) : null;
            this.invert = definition != null && definition.invert();
            this.runner = createRunner(animation, initialState, this.invert);
            this.lastState = initialState;
        }

        private boolean matches(BaseInstrumentDisplay display) {
            return displayId.equals(display.getDisplayId());
        }

        private void tick(boolean currentState) {
            if (runner != null) {
                runner.tick();
            }
            if (runner == null || currentState == lastState) {
                lastState = currentState;
                return;
            }
            float speed = currentState ? 1.0f : -1.0f;
            if (invert) {
                speed = -speed;
            }
            PlayingState playingState = new PlayingState(System::nanoTime, PauseState::new);
            playingState.setSpeed(speed);
            runner.setState(playingState);
            lastState = currentState;
        }

        private Pose evaluatePose() {
            if (runner == null) {
                return DummyPose.INSTANCE;
            }
            return runner.evaluate();
        }

        private static AnimationRunner createRunner(BedrockAnimation animation, boolean initialState, boolean invert) {
            if (animation == null) {
                return null;
            }
            AnimationContext animationContext = new AnimationContext(animation.getSpecifiedEndTimeS());
            boolean effectiveState = invert ? !initialState : initialState;
            animationContext.setProgress(effectiveState ? animation.getSpecifiedEndTimeS() : 0);
            AnimationRunner animationRunner = new AnimationRunner(animation, animationContext);
            animationRunner.setState(new PauseState());
            return animationRunner;
        }
    }

}