package org.ywzj.midi.client.render.item;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.model.SlotModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.ywzj.midi.client.resource.BaseInstrumentDisplay;
import org.ywzj.midi.client.resource.ClientAssetsManager;
import org.ywzj.midi.item.InstrumentItem;

import javax.annotation.Nonnull;

public class InstrumentItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final SlotModel SLOT_MODEL = new SlotModel();

    public InstrumentItemRenderer(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
        super(pBlockEntityRenderDispatcher, pEntityModelSet);
    }

    @Override
    public void renderByItem(@Nonnull ItemStack itemStack, @Nonnull ItemDisplayContext transformType,
                             @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource pBuffer,
                             int pPackedLight, int pPackedOverlay) {
        if (!(itemStack.getItem() instanceof InstrumentItem)) {
            return;
        }
        CompoundTag tag = itemStack.getOrCreateTag();
        String displayIdStr = tag.getString(InstrumentItem.TAG_DISPLAY_ID);
        ResourceLocation displayId = ResourceLocation.tryParse(displayIdStr);
        if (displayId == null) {
            return;
        }

        BaseInstrumentDisplay display = ClientAssetsManager.INSTANCE.getInstrumentDisplay(displayId).orElse(null);

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        if (display != null) {
            ResourceLocation slotTexture = display.getSlotTexture();
            BedrockModel model = display.getModel();
            ResourceLocation texture = display.getTexture();

            if (slotTexture != null) {
                // Render slot icon (2D quad)
                if (transformType != ItemDisplayContext.GUI) {
                    poseStack.mulPose(Axis.YN.rotationDegrees(-45f));
                }
                VertexConsumer buffer = pBuffer.getBuffer(RenderType.entityTranslucent(slotTexture));
                SLOT_MODEL.renderToBuffer(poseStack, buffer, pPackedLight, pPackedOverlay, 1f, 1f, 1f, 1f);
            } else if (model != null && texture != null) {
                // Render full bedrock model
                VertexConsumer buffer = pBuffer.getBuffer(RenderType.entityCutout(texture));
                model.renderToBuffer(poseStack, buffer, pPackedLight, pPackedOverlay, 1f, 1f, 1f, 1f);
            }
        }

        poseStack.popPose();
    }

}
