package org.ywzj.midi.network.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.ywzj.midi.blockentity.NoteBlockEntity;
import org.ywzj.midi.network.message.CNoteBlockUpdate;


public class NoteBlockHandler {

    public static void onClientMessageReceived(CNoteBlockUpdate message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> processClientMessage((ServerPlayer) ctx.player(), message));
    }

    private static void processClientMessage(ServerPlayer sender, CNoteBlockUpdate message) {
        BlockPos pos = new BlockPos(message.x, message.y, message.z);
        BlockEntity blockEntity = sender.level().getBlockEntity(pos);
        if (blockEntity instanceof NoteBlockEntity noteBlockEntity) {
            noteBlockEntity.note = message.note;
            noteBlockEntity.serverHandleData(sender, message, pos);
            blockEntity.setChanged();
        }
    }

}
