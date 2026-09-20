package org.ywzj.midi.network.handler;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.ywzj.midi.network.message.CFileData;
import org.ywzj.midi.util.FileTransfer;


public class FileDataHandler {

    public static void onServerMessageReceived(CFileData message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> FileTransfer.receive((ServerPlayer) ctx.player(), message));
    }

}
