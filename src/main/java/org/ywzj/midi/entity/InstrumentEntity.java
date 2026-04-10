package org.ywzj.midi.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.ywzj.midi.all.AllEntities;

public class InstrumentEntity extends Entity {

    public static final String TAG_DISPLAY_ID = "instrument_display_id";

    private static final EntityDataAccessor<String> DISPLAY_ID =
            SynchedEntityData.defineId(InstrumentEntity.class, EntityDataSerializers.STRING);

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
    }

    public ResourceLocation getDisplayId() {
        String raw = this.entityData.get(DISPLAY_ID);
        return raw.isEmpty() ? null : ResourceLocation.tryParse(raw);
    }

    public void setDisplayId(ResourceLocation displayId) {
        this.entityData.set(DISPLAY_ID, displayId != null ? displayId.toString() : "");
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains(TAG_DISPLAY_ID)) {
            ResourceLocation id = ResourceLocation.tryParse(tag.getString(TAG_DISPLAY_ID));
            if (id != null) {
                setDisplayId(id);
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        ResourceLocation id = getDisplayId();
        if (id != null) {
            tag.putString(TAG_DISPLAY_ID, id.toString());
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        super.tick();
    }

}
