package org.ywzj.midi.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.ywzj.midi.all.AllBlockEntities;
import org.ywzj.midi.audio.ClientPlayerInstance;
import org.ywzj.midi.gui.screen.MusicPlayerScreen;

import java.util.UUID;

public class SpeakerBlockEntity extends BlockEntity {

    private ClientPlayerInstance clientPlayerInstance;
    public MusicPlayerScreen musicPlayerScreen;

    public SpeakerBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(AllBlockEntities.SPEAKER_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
    }

    public ClientPlayerInstance getClientPlayerInstance() {
        if (clientPlayerInstance == null) {
            clientPlayerInstance = new ClientPlayerInstance();
        }
        return clientPlayerInstance;
    }

    public void updateData(UUID soundUuid, String musicName, String playerName) {
        getPersistentData().putUUID("soundUuid", soundUuid);
        getPersistentData().putString("musicName", musicName);
        getPersistentData().putString("playerName", playerName);
        this.setChanged();
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        super.onDataPacket(net, pkt, registries);
        clientPlayerInstance = getClientPlayerInstance();
        try {
            clientPlayerInstance.soundUuid = pkt.getTag().getUUID("soundUuid");
            clientPlayerInstance.musicName = pkt.getTag().getString("musicName");
            clientPlayerInstance.playerName = pkt.getTag().getString("playerName");
        } catch (Exception exception) {
            clientPlayerInstance.soundUuid = null;
            clientPlayerInstance.musicName = null;
            clientPlayerInstance.playerName = null;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return getPersistentData();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (getLevel() != null && getLevel().isClientSide) {
            if (clientPlayerInstance != null) {
                clientPlayerInstance.stop();
            }
        }
    }

}
