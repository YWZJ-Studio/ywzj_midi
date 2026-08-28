package org.ywzj.midi.client.render.item;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.model.SlotModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.ywzj.midi.item.PlainTextureItem;

import javax.annotation.Nonnull;

public class PlainTextureItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final SlotModel PLAIN_TEXTURE_ITEM_MODEL = new SlotModel();

    public PlainTextureItemRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
        super(blockEntityRenderDispatcher, entityModelSet);
    }

    @Override
    public void renderByItem(@Nonnull ItemStack itemStack, @Nonnull ItemDisplayContext transformType,
                             @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                             int packedLight, int packedOverlay) {
        if (!(itemStack.getItem() instanceof PlainTextureItem)) {
            return;
        }
        CompoundTag tag = itemStack.getOrCreateTag();
        ResourceLocation textureLocation = ResourceLocation.tryParse(tag.getString("textureLocation"));
        if (textureLocation == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(textureLocation));
        PLAIN_TEXTURE_ITEM_MODEL.renderToBuffer(
                poseStack, buffer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }

}
