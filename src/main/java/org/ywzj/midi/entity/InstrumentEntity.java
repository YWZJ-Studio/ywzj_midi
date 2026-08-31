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
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.all.AllEntities;
import org.ywzj.midi.all.AllInstruments;
import org.ywzj.midi.all.AllItems;
import org.ywzj.midi.gui.ScreenManager;
import org.ywzj.midi.instrument.receiver.MidiReceiver;
import org.ywzj.midi.item.InstrumentEntityItem;
import org.ywzj.midi.item.InstrumentItem;

import java.util.Collections;
import java.util.Set;

public class InstrumentEntity extends Entity {

    public static final String TAG_INSTRUMENT_ID = "instrument_id";
    public static final String TAG_DISPLAY_ID = "instrument_display_id";
    public static final String TAG_SWITCHABLE_ON = "switchable_on";
    public static final String TAG_INSTRUMENT_NAME = "instrument_name";
    private static final EntityDataAccessor<String> DISPLAY_ID = SynchedEntityData.defineId(InstrumentEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> SWITCHABLE_ON = SynchedEntityData.defineId(InstrumentEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> INSTRUMENT_NAME = SynchedEntityData.defineId(InstrumentEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> INSTRUMENT_ID = SynchedEntityData.defineId(InstrumentEntity.class, EntityDataSerializers.STRING);
    private static final double PUSH_STRENGTH = 0.01D;
    private static final double HORIZONTAL_FRICTION = 0.5D;
    private MidiReceiver receiver;
    private Set<Integer> activeNotes = Collections.emptySet();

    public InstrumentEntity(EntityType<? extends InstrumentEntity> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public InstrumentEntity(Level level, ResourceLocation displayId, String instrumentName) {
        this(AllEntities.GENERIC_INSTRUMENT.get(), level);
        setDisplayId(displayId);
        setInstrumentId(displayId);
        setInstrumentName(instrumentName);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DISPLAY_ID, "");
        this.entityData.define(SWITCHABLE_ON, false);
        this.entityData.define(INSTRUMENT_NAME, "");
        this.entityData.define(INSTRUMENT_ID, "");
    }

    public ResourceLocation getDisplayId() {
        String raw = this.entityData.get(DISPLAY_ID);
        return raw.isEmpty() ? null : ResourceLocation.tryParse(raw);
    }

    public void setDisplayId(ResourceLocation displayId) {
        this.entityData.set(DISPLAY_ID, displayId != null ? displayId.toString() : "");
    }

    public String getInstrumentName() {
        return this.entityData.get(INSTRUMENT_NAME);
    }

    public void setInstrumentName(String name) {
        this.entityData.set(INSTRUMENT_NAME, name != null ? name : "");
    }

    public ResourceLocation getInstrumentId() {
        String raw = this.entityData.get(INSTRUMENT_ID);
        if (!raw.isEmpty()) return ResourceLocation.tryParse(raw);
        String name = getInstrumentName();
        return name.isEmpty() ? null : new ResourceLocation(YwzjMidi.MOD_ID, name);
    }

    public void setInstrumentId(ResourceLocation id) {
        this.entityData.set(INSTRUMENT_ID, id != null ? id.toString() : "");
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
        if (player.isShiftKeyDown()) {
            if (!this.level().isClientSide) {
                dropAsItem();
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (this.level().isClientSide) {
            var instrument = AllInstruments.fromId(getInstrumentId());
            if (instrument != null) {
                ScreenManager.openInstrumentEntityScreen(instrument, this);
            }
        } else {
            setSwitchableOn(!isSwitchableOn());
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    public void dropAsItem() {
        if (this.level().isClientSide) {
            return;
        }
        ResourceLocation instrumentId = getInstrumentId();
        var instrument = instrumentId == null ? null : AllInstruments.fromId(instrumentId);
        if (instrument != null) {
            if ("entity".equals(instrument.getData().getItemType())) {
                this.spawnAtLocation(InstrumentEntityItem.createInstance(instrumentId, instrument.getData().getName()));
            } else if ("item".equals(instrument.getData().getItemType())) {
                this.spawnAtLocation(InstrumentItem.createInstance(instrumentId, instrument.getData().getName()));
            } else {
                var itemSupplier = AllItems.ITEMS_LOOKUP.get(instrumentId.getPath());
                if (itemSupplier != null && itemSupplier.get() != null) {
                    this.spawnAtLocation(new ItemStack(itemSupplier.get()));
                }
            }
        }
        this.discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains(TAG_INSTRUMENT_ID)) {
            setInstrumentId(ResourceLocation.tryParse(tag.getString(TAG_INSTRUMENT_ID)));
        }
        if (tag.contains(TAG_DISPLAY_ID)) {
            ResourceLocation id = ResourceLocation.tryParse(tag.getString(TAG_DISPLAY_ID));
            if (id != null) {
                setDisplayId(id);
            }
        }
        if (tag.contains(TAG_SWITCHABLE_ON)) {
            setSwitchableOn(tag.getBoolean(TAG_SWITCHABLE_ON));
        }
        if (tag.contains(TAG_INSTRUMENT_NAME)) {
            setInstrumentName(tag.getString(TAG_INSTRUMENT_NAME));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        ResourceLocation instrumentId = getInstrumentId();
        if (instrumentId != null) {
            tag.putString(TAG_INSTRUMENT_ID, instrumentId.toString());
        }
        ResourceLocation id = getDisplayId();
        if (id != null) {
            tag.putString(TAG_DISPLAY_ID, id.toString());
        }
        tag.putBoolean(TAG_SWITCHABLE_ON, isSwitchableOn());
        String name = getInstrumentName();
        if (!name.isEmpty()) {
            tag.putString(TAG_INSTRUMENT_NAME, name);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        Vec3 movement = this.getDeltaMovement();
        this.setDeltaMovement(movement.x * HORIZONTAL_FRICTION, movement.y * 0.98D, movement.z * HORIZONTAL_FRICTION);
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

    @Override
    public void push(Entity entity) {
        if (!this.isPushable() || entity.isRemoved() || !entity.isPushable()) {
            return;
        }
        double xDifference = this.getX() - entity.getX();
        double zDifference = this.getZ() - entity.getZ();
        double distance = Math.sqrt(xDifference * xDifference + zDifference * zDifference);
        if (distance < 1.0E-7D) {
            return;
        }
        double push = PUSH_STRENGTH / distance;
        this.setDeltaMovement(this.getDeltaMovement().add(xDifference * push, 0.0D, zDifference * push));
        this.hasImpulse = true;
    }

    public void setReceiver(MidiReceiver receiver) {
        this.receiver = receiver;
        if (receiver != null) receiver.setInstrumentEntityId(getId());
    }

    public MidiReceiver getReceiver() {
        return receiver;
    }

    public Set<Integer> getActiveNotes() {
        return activeNotes;
    }

    public void setActiveNotes(java.util.List<Integer> notes) {
        activeNotes = notes == null || notes.isEmpty() ? Collections.emptySet() : Set.copyOf(notes);
    }

}
