package org.ywzj.midi.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.ywzj.midi.all.AllBlockEntities;
import org.ywzj.midi.gui.screen.ClavichordScreen;

public class PianoBlockEntity extends BlockEntity {

    public ClavichordScreen clavichordScreen;

    public PianoBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(AllBlockEntities.NOTE_BLOCK_ENTITY.get(), p_155229_, p_155230_);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return new CompoundTag();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {}

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        closeReceiver();
    }

    public void closeReceiver() {
        if (clavichordScreen != null) {
            clavichordScreen.closeMidiReceiver();
        }
    }

}
