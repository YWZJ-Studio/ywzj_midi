package org.ywzj.midi.client.render.entity;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.animation.BedrockAnimation;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.maydaymemory.mae.basic.*;
import com.maydaymemory.mae.blend.EulerAdditiveBlender;
import com.maydaymemory.mae.blend.SimpleEulerAdditiveBlender;
import com.maydaymemory.mae.control.runner.AnimationContext;
import com.maydaymemory.mae.control.runner.AnimationRunner;
import com.maydaymemory.mae.control.runner.PauseState;
import com.maydaymemory.mae.control.runner.PlayingState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.ywzj.midi.all.AllInstruments;
import org.ywzj.midi.client.resource.BaseInstrumentDisplay;
import org.ywzj.midi.client.resource.ClientAssetsManager;
import org.ywzj.midi.entity.InstrumentEntity;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.instrument.receiver.MidiReceiver;
import org.ywzj.midi.script.MidiPoseBuilder;
import org.ywzj.midi.script.MidiPoseScriptContext;
import org.ywzj.midi.script.MidiScriptPoseProvider;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InstrumentEntityRenderer extends EntityRenderer<InstrumentEntity> {

    private static final EulerAdditiveBlender BLENDER = new SimpleEulerAdditiveBlender(new ZYXBoneTransformFactory(), ArrayPoseBuilder::new);
    private static final Map<Integer, SwitchableAnimationState> SWITCHABLE_ANIMATION_STATES = new ConcurrentHashMap<>();

    public InstrumentEntityRenderer(EntityRendererProvider.Context pContext) {
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
        {
            super.render(entity, pEntityYaw, pPartialTick, pPoseStack, bufferSource, pPackedLight);
            Vec3 root = Vec3.ZERO;
            pPoseStack.rotateAround(Axis.YP.rotationDegrees(-entity.getViewYRot(pPartialTick)), (float) root.x, (float) root.y, (float) root.z);
            Pose blended = BLENDER.blend(model.getBindPose(), animationState.evaluatePose());
            Pose scriptPose = createScriptPose(model, entity);
            if (scriptPose != null) {
                blended = BLENDER.blend(blended, scriptPose);
            }
            model.applyPose(blended);
            VertexConsumer builder = bufferSource.getBuffer(RenderType.entityCutout(texture));
            model.renderToBuffer(pPoseStack, builder, pPackedLight, OverlayTexture.NO_OVERLAY);
            model.applyPose(model.getBindPose());
        }
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

    @Nullable
    private static Pose createScriptPose(BedrockModel model, InstrumentEntity entity) {
        MidiReceiver receiver = entity.getReceiver();
        Set<Integer> notes = receiver == null ? Set.of() : receiver.getPlayedNotes();
        if (notes.isEmpty()) {
            notes = entity.getActiveNotes();
        }
        if (notes.isEmpty()) {
            return null;
        }

        Instrument instrument = AllInstruments.fromId(entity.getInstrumentId());
        if (instrument == null) {
            return null;
        }

        MidiPoseScriptContext ctx = new MidiPoseScriptContext();
        ctx.setInstrumentId(instrument.getInstrumentId());
        ctx.setNotes(new ArrayList<>(notes));

        Map<String, MidiPoseBuilder.BoneTransform> bonePoses =
                MidiScriptPoseProvider.getInstance().computeModelPose(instrument, ctx);
        if (bonePoses == null || bonePoses.isEmpty()) {
            return null;
        }

        // Resolve bone indices and sort: ArrayPoseBuilder requires strictly ascending order
        var sorted = new ArrayList<Map.Entry<Integer, MidiPoseBuilder.BoneTransform>>();
        for (var entry : bonePoses.entrySet()) {
            int boneIndex = model.getIndex(entry.getKey());
            if (boneIndex >= 0) {
                sorted.add(new AbstractMap.SimpleEntry<>(boneIndex, entry.getValue()));
            }
        }
        sorted.sort(Map.Entry.comparingByKey());

        ZYXBoneTransformFactory factory = new ZYXBoneTransformFactory();
        ArrayPoseBuilder builder = new ArrayPoseBuilder();
        for (var entry : sorted) {
            int boneIndex = entry.getKey();
            var bt = entry.getValue();
            float rxRad = (float) Math.toRadians(bt.rotX());
            float ryRad = (float) Math.toRadians(bt.rotY());
            float rzRad = (float) Math.toRadians(bt.rotZ());
            BoneTransform transform = factory.createBoneTransform(
                boneIndex,
                new Vector3f(bt.transX(), bt.transY(), bt.transZ()),
                new Vector3f(rxRad, ryRad, rzRad),
                new Vector3f(1, 1, 1)
            );
            builder.addBoneTransform(transform);
        }
        return builder.toPose();
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
