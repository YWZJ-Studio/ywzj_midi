package org.ywzj.midi.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.ywzj.midi.network.SliceReassembler;

import java.util.UUID;

public record ServerSlicedPacket(UUID id, int index, int total, byte[] data) {

    public static void encode(ServerSlicedPacket pkt, FriendlyByteBuf buf) {
        buf.writeUUID(pkt.id);
        buf.writeInt(pkt.index);
        buf.writeInt(pkt.total);
        buf.writeVarInt(pkt.data.length);
        buf.writeBytes(pkt.data);
    }

    public static ServerSlicedPacket decode(FriendlyByteBuf buf) {
        UUID id = buf.readUUID();
        int index = buf.readInt();
        int total = buf.readInt();
        int len = buf.readVarInt();
        byte[] data = new byte[len];
        buf.readBytes(data);
        return new ServerSlicedPacket(id, index, total, data);
    }

    public static void handle(ServerSlicedPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> SliceReassembler.receiveSlice(pkt.id, pkt.index, pkt.total, pkt.data));
    }

}
