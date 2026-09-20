package org.ywzj.midi.render.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import org.ywzj.midi.entity.FakePlayerEntity;
import org.ywzj.midi.render.model.FakePlayerModel;
import org.ywzj.midi.storage.FakePlayerSkin;

public class FakePlayerRenderer extends LivingEntityRenderer<FakePlayerEntity, FakePlayerModel> {

    public FakePlayerRenderer(EntityRendererProvider.Context context) {
        super(context, new FakePlayerModel(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),context.getModelManager()));
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
    }

    @Override
    public void render(FakePlayerEntity entity, float p_115456_, float p_115457_, PoseStack matrixStack, MultiBufferSource p_115459_, int p_115460_) {
        matrixStack.pushPose();
        if (entity.isBaby()) {
            matrixStack.scale(0.5f,0.5f,0.5f);
        } else {
            matrixStack.scale(0.9375F, 0.9375F, 0.9375F);
        }
        if (entity.isSitting()) {
            matrixStack.translate(0,-0.68f,0);
        }
        super.render(entity, p_115456_, p_115457_, matrixStack, p_115459_, p_115460_);
        matrixStack.popPose();
    }

    @Override
    protected boolean shouldShowName(FakePlayerEntity entity) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(FakePlayerEntity fakePlayerEntity) {
        ResourceLocation location = FakePlayerSkin.SKINS.get(fakePlayerEntity.getEntityData().get(FakePlayerEntity.NAME));
        return location == null ? Minecraft.getInstance().player.getSkin().texture() : location;
    }

}
