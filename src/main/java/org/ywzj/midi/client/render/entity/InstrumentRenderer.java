package org.ywzj.midi.client.render.entity;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
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

public class InstrumentRenderer extends EntityRenderer<InstrumentEntity> {

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

        pPoseStack.pushPose();
        super.render(entity, pEntityYaw, pPartialTick, pPoseStack, bufferSource, pPackedLight);
        VertexConsumer builder = bufferSource.getBuffer(RenderType.entityCutout(texture));
        model.renderToBuffer(pPoseStack, builder, pPackedLight, OverlayTexture.NO_OVERLAY);
        pPoseStack.popPose();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull InstrumentEntity pEntity) {
        // Not used directly; texture comes from the display definition
        return new ResourceLocation("missingno");
    }

}
