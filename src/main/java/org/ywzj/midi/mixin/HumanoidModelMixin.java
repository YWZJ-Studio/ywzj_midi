package org.ywzj.midi.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.ywzj.midi.all.AllInstruments;
import org.ywzj.midi.entity.FakePlayerEntity;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.item.InstrumentItem;
import org.ywzj.midi.pose.PoseManager;
import org.ywzj.midi.script.MidiScriptPoseProvider;

@Mixin(value = HumanoidModel.class)
public class HumanoidModelMixin {

    @Shadow
    @Final
    public ModelPart leftArm;

    @Shadow
    @Final
    public ModelPart rightArm;

    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At(value = "TAIL"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void setupAnim(LivingEntity livingEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (livingEntity instanceof Player || livingEntity instanceof FakePlayerEntity) {
            this.setHoldPose(livingEntity);
            PoseManager.PlayPose newPose = PoseManager.poll(livingEntity.getUUID());
            PoseManager.PlayPose cachePose = PoseManager.getCache(livingEntity.getUUID());
            if (newPose != null) {
                this.setPose(newPose);
            } else if (cachePose != null) {
                this.setPose(cachePose);
            }
        }
    }

    private void setPose(PoseManager.PlayPose pose) {
        if (pose.rightArmX != null) {
            this.rightArm.x = pose.rightArmX;
        }
        if (pose.rightArmY != null) {
            this.rightArm.y = pose.rightArmY;
        }
        if (pose.rightArmZ != null) {
            this.rightArm.z = pose.rightArmZ;
        }
        if (pose.rightArmRotX != null) {
            this.rightArm.xRot = pose.rightArmRotX;
        }
        if (pose.rightArmRotY != null) {
            this.rightArm.yRot = pose.rightArmRotY;
        }
        if (pose.rightArmRotZ != null) {
            this.rightArm.zRot = pose.rightArmRotZ;
        }
        if (pose.leftArmX != null) {
            this.leftArm.x = pose.leftArmX;
        }
        if (pose.leftArmY != null) {
            this.leftArm.y = pose.leftArmY;
        }
        if (pose.leftArmZ != null) {
            this.leftArm.z = pose.leftArmZ;
        }
        if (pose.leftArmRotX != null) {
            this.leftArm.xRot = pose.leftArmRotX;
        }
        if (pose.leftArmRotY != null) {
            this.leftArm.yRot = pose.leftArmRotY;
        }
        if (pose.leftArmRotZ != null) {
            this.leftArm.zRot = pose.leftArmRotZ;
        }
    }

    private void setHoldPose(LivingEntity player) {
        PoseManager.PlayPose scriptPose = getScriptHoldPose(player.getMainHandItem(), InteractionHand.MAIN_HAND);
        if (scriptPose != null) {
            this.setPose(scriptPose);
            return;
        }
        scriptPose = getScriptHoldPose(player.getOffhandItem(), InteractionHand.OFF_HAND);
        if (scriptPose != null) {
            this.setPose(scriptPose);
            return;
        }
        PoseManager.PlayPose mainHandHoldPose = PoseManager.getHoldPose(player.getMainHandItem().getItem(), InteractionHand.MAIN_HAND);
        PoseManager.PlayPose offHandHoldPose = PoseManager.getHoldPose(player.getOffhandItem().getItem(), InteractionHand.OFF_HAND);
        if (mainHandHoldPose != null) {
            this.setPose(mainHandHoldPose);
        } else if (offHandHoldPose != null) {
            this.setPose(offHandHoldPose);
        }
    }

    private PoseManager.PlayPose getScriptHoldPose(ItemStack itemStack, InteractionHand hand) {
        if (itemStack.getItem() instanceof InstrumentItem) {
            String i = itemStack.getOrCreateTag().getString(InstrumentItem.TAG_INSTRUMENT_ID);
            Instrument instrument = AllInstruments.fromId(new ResourceLocation(i));
            if (instrument != null) {
                return MidiScriptPoseProvider.getInstance().computeHoldPose(instrument, hand);
            }
        }
        return null;
    }

}
