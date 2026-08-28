package org.ywzj.midi.client.render.item;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.model.SlotModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.ywzj.midi.all.AllItems;
import org.ywzj.midi.client.resource.BaseInstrumentDisplay;
import org.ywzj.midi.client.resource.ClientAssetsManager;
import org.ywzj.midi.item.InstrumentEntityItem;
import org.ywzj.midi.item.InstrumentItem;
import org.ywzj.midi.item.InstrumentToolItem;

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
        CompoundTag tag = itemStack.getOrCreateTag();
        String displayIdStr = null;
        if (itemStack.getItem() instanceof InstrumentItem ||  itemStack.getItem() instanceof InstrumentEntityItem) {
            displayIdStr = tag.getString(InstrumentItem.TAG_INSTRUMENT_ID);
        } else if (itemStack.getItem() instanceof InstrumentToolItem) {
            displayIdStr = tag.getString(InstrumentToolItem.TAG_INSTRUMENT_TOOL_ID);
        }
        if (displayIdStr == null) {
            return;
        }
        ResourceLocation displayId = ResourceLocation.tryParse(displayIdStr);
        if (displayId == null) {
            return;
        }
        BaseInstrumentDisplay display = ClientAssetsManager.INSTANCE.getInstrumentDisplay(displayId).orElse(null);
        if (display != null && display.hasCustomItemRenderer()) {
            poseStack.pushPose();
            {
                poseStack.translate(0.5, 0.5, 0.5);
                BaseInstrumentDisplay.Display itemDisplay = display.getDisplay(transformType);
                if (itemDisplay != null) {
                    BaseInstrumentDisplay.applyDisplay(poseStack, itemDisplay);
                }
                ResourceLocation slotTexture = display.getSlotTexture();
                BedrockModel model = display.getModel();
                ResourceLocation texture = display.getTexture();
                if (slotTexture != null) {
                    if (transformType != ItemDisplayContext.GUI) {
                        poseStack.mulPose(Axis.YN.rotationDegrees(-45f));
                    }
                    VertexConsumer buffer = pBuffer.getBuffer(RenderType.entityTranslucent(slotTexture));
                    SLOT_MODEL.renderToBuffer(poseStack, buffer, pPackedLight, pPackedOverlay, 1f, 1f, 1f, 1f);
                } else if (model != null && texture != null) {
                    VertexConsumer buffer = pBuffer.getBuffer(RenderType.entityCutout(texture));
                    model.renderToBuffer(poseStack, buffer, pPackedLight, pPackedOverlay, 1f, 1f, 1f, 1f);
                }
            }
            poseStack.popPose();
        } else {
            // 若无基岩模型，尝试使用对应java物品来渲染
            var plainItem = AllItems.ITEMS_LOOKUP.get(displayId.getPath());
            if (plainItem != null && plainItem.get() != null) {
                ItemStack fallbackStack = new ItemStack(plainItem.get());
                poseStack.pushPose();
                {
                    poseStack.translate(0.5, 0.5, 0.5);
                    boolean leftHand = transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                            || transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
                    Minecraft.getInstance().getItemRenderer().renderStatic(null, fallbackStack, transformType,
                            leftHand, poseStack, pBuffer, null, pPackedLight, pPackedOverlay, 0);
                }
                poseStack.popPose();
            }
        }
    }

}
