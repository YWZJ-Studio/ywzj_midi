package org.ywzj.midi.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.ywzj.midi.all.AllBlockEntities;
import org.ywzj.midi.network.Channel;
import org.ywzj.midi.network.message.CNoteBlockUpdate;

public class NoteBlockEntity extends BlockEntity {

    public Integer note = 0;

    public NoteBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(AllBlockEntities.NOTE_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("note", note);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        this.note = tag.getInt("note");
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("note", note);
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        note = nbt.getInt("note");
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

    public void clientSendData(boolean on) {
        Channel.CHANNEL.sendToServer(new CNoteBlockUpdate(this.getBlockPos(), note, on));
    }

    public void serverHandleData(ServerPlayer sender, CNoteBlockUpdate message, BlockPos pos) {
        return;
    }

}
