package org.ywzj.midi.entity;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.ywzj.midi.all.AllEntities;
import org.ywzj.midi.gui.ScreenManager;
import org.ywzj.midi.instrument.player.ServerMidiPlayer;
import org.ywzj.midi.network.Channel;
import org.ywzj.midi.network.message.CFakePlayerUpdate;
import org.ywzj.midi.storage.FakePlayerSkin;
import org.ywzj.midi.util.ComponentUtils;

public class FakePlayerEntity extends Mob {

    public ServerMidiPlayer serverMidiPlayer;
    public static final String DEFAULT_NAME = "Dummy";
    public static final EntityDataAccessor<String> NAME = SynchedEntityData.defineId(FakePlayerEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> SKIN_URL = SynchedEntityData.defineId(FakePlayerEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Boolean> IS_SITTING = SynchedEntityData.defineId(FakePlayerEntity.class, EntityDataSerializers.BOOLEAN);

    public FakePlayerEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    public void updateClientToServer() {
        Channel.CHANNEL.sendToServer(new CFakePlayerUpdate(this.entityData.get(NAME), this.entityData.get(SKIN_URL), this.entityData.get(IS_SITTING), this.getId()));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        if (player.level().isClientSide && interactionHand.equals(InteractionHand.MAIN_HAND)) {
            if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem().equals(Items.NAME_TAG)) {
                ScreenManager.openFakePlayerScreen(this);
            }
            if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem().equals(Items.BOOK)) {
                ScreenManager.openFakePlayerConductorScreen(this);
            }
            return InteractionResult.PASS;
        }
        if (!player.level().isClientSide && interactionHand.equals(InteractionHand.MAIN_HAND)) {
            if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem().equals(Items.NAME_TAG)
                    || player.getItemInHand(InteractionHand.MAIN_HAND).getItem().equals(Items.BOOK) ) {
                return InteractionResult.PASS;
            }
            if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem().equals(Items.AIR)) {
                this.lookAt(EntityAnchorArgument.Anchor.EYES, EntityAnchorArgument.Anchor.EYES.apply(player));
            }
            if (!player.isCrouching()) {
                if (this.getItemInHand(InteractionHand.MAIN_HAND).getItem().equals(player.getItemInHand(InteractionHand.MAIN_HAND).getItem())) {
                    this.setItemInHand(InteractionHand.OFF_HAND, this.getItemInHand(InteractionHand.MAIN_HAND));
                    this.setItemInHand(InteractionHand.MAIN_HAND, Items.AIR.getDefaultInstance());
                } else {
                    this.setItemInHand(InteractionHand.MAIN_HAND, player.getItemInHand(InteractionHand.MAIN_HAND));
                }
            } else {
                this.entityData.set(IS_SITTING, !this.entityData.get(IS_SITTING));
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public Component getName() {
        return ComponentUtils.literal(this.entityData.get(NAME));
    }

    public EntityType<?> getType() {
        return AllEntities.FAKE_PLAYER.get();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(NAME, DEFAULT_NAME);
        builder.define(SKIN_URL, "");
        builder.define(IS_SITTING, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putString("name", this.entityData.get(NAME));
        nbt.putString("skinUrl", this.entityData.get(SKIN_URL));
        nbt.putBoolean("isSitting", this.entityData.get(IS_SITTING));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        this.entityData.set(NAME, nbt.getString("name").equals("") ? DEFAULT_NAME : nbt.getString("name"));
        this.entityData.set(SKIN_URL, nbt.getString("skinUrl"));
        this.entityData.set(IS_SITTING, nbt.getBoolean("isSitting"));
        if (!(this.level() instanceof ServerLevel)) {
            FakePlayerSkin.handle(this.entityData.get(NAME), this.entityData.get(SKIN_URL));
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        super.onSyncedDataUpdated(entityDataAccessor);
        if (!(this.level() instanceof ServerLevel)) {
            FakePlayerSkin.handle(this.entityData.get(NAME), this.entityData.get(SKIN_URL));
        }
    }

    public boolean isSitting() {
        return this.entityData.get(IS_SITTING);
    }

    @Override
    public boolean shouldBeSaved() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double v) {
        return false;
    }

}
