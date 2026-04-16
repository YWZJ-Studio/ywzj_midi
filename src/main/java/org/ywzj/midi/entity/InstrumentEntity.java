package org.ywzj.midi.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.ywzj.midi.all.AllEntities;

public class InstrumentEntity extends Entity {

    public static final String TAG_DISPLAY_ID = "instrument_display_id";
    public static final String TAG_SWITCHABLE_ON = "switchable_on";

    private static final EntityDataAccessor<String> DISPLAY_ID =
            SynchedEntityData.defineId(InstrumentEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> SWITCHABLE_ON =
            SynchedEntityData.defineId(InstrumentEntity.class, EntityDataSerializers.BOOLEAN);

    public InstrumentEntity(EntityType<? extends InstrumentEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public InstrumentEntity(Level level, ResourceLocation displayId) {
        this(AllEntities.GENERIC_INSTRUMENT.get(), level);

        //todo:
        displayId = new ResourceLocation("ywzj_midi", "cfx");

        setDisplayId(displayId);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DISPLAY_ID, "");
        this.entityData.define(SWITCHABLE_ON, false);
    }

    public ResourceLocation getDisplayId() {
        String raw = this.entityData.get(DISPLAY_ID);
        return raw.isEmpty() ? null : ResourceLocation.tryParse(raw);
    }

    public void setDisplayId(ResourceLocation displayId) {
        this.entityData.set(DISPLAY_ID, displayId != null ? displayId.toString() : "");
    }

    public boolean isSwitchableOn() {
        return this.entityData.get(SWITCHABLE_ON);
    }

    public void setSwitchableOn(boolean switchableOn) {
        this.entityData.set(SWITCHABLE_ON, switchableOn);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!this.level().isClientSide) {
            setSwitchableOn(!isSwitchableOn());
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains(TAG_DISPLAY_ID)) {
            ResourceLocation id = ResourceLocation.tryParse(tag.getString(TAG_DISPLAY_ID));
            if (id != null) {
                setDisplayId(id);
            }
        }
        if (tag.contains(TAG_SWITCHABLE_ON)) {
            setSwitchableOn(tag.getBoolean(TAG_SWITCHABLE_ON));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        ResourceLocation id = getDisplayId();
        if (id != null) {
            tag.putString(TAG_DISPLAY_ID, id.toString());
        }
        tag.putBoolean(TAG_SWITCHABLE_ON, isSwitchableOn());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean shouldBeSaved() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

}