package org.ywzj.midi.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.network.message.*;

public record MidiPacket(int id, byte[] data) implements CustomPacketPayload {

    public static final int C_AA775_UPDATE = 99;
    public static final int C_PLAY_NOTE = 100;
    public static final int S_PLAY_NOTE = 101;
    public static final int C_CHANGE_NOTE = 102;
    public static final int S_CHANGE_NOTE = 103;
    public static final int C_PLAY_MUSIC = 104;
    public static final int S_PLAY_MUSIC = 105;
    public static final int C_SYNC_MUSIC = 106;
    public static final int S_SYNC_MUSIC = 107;
    public static final int C_POSE_DATA = 108;
    public static final int S_POSE_DATA = 109;
    public static final int C_FAKE_PLAYER_DATA = 110;
    public static final int C_FILE_DATA = 111;
    public static final int C_PLAY_MIDI = 112;
    public static final int S_SLICED_PACKET = 113;

    public static final Type<MidiPacket> TYPE = new Type<>(YwzjMidi.modLocation("main"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MidiPacket> STREAM_CODEC =
            StreamCodec.of((buf, packet) -> packet.encode(buf), MidiPacket::decode);

    public static MidiPacket wrap(Object message) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        int id;
        if (message instanceof CNoteBlockUpdate value) { id = C_AA775_UPDATE; value.encode(buffer); }
        else if (message instanceof SPlayNote value) { id = S_PLAY_NOTE; value.encode(buffer); }
        else if (message instanceof CPlayNote value) { id = C_PLAY_NOTE; value.encode(buffer); }
        else if (message instanceof SChangeNote value) { id = S_CHANGE_NOTE; value.encode(buffer); }
        else if (message instanceof CChangeNote value) { id = C_CHANGE_NOTE; value.encode(buffer); }
        else if (message instanceof SSyncMusic value) { id = S_PLAY_MUSIC; value.encode(buffer); }
        else if (message instanceof CSyncMusic value) { id = C_PLAY_MUSIC; value.encode(buffer); }
        else if (message instanceof SPlayMusic value) { id = S_SYNC_MUSIC; value.encode(buffer); }
        else if (message instanceof CPlayMusic value) { id = C_SYNC_MUSIC; value.encode(buffer); }
        else if (message instanceof SPoseData value) { id = S_POSE_DATA; value.encode(buffer); }
        else if (message instanceof CPoseData value) { id = C_POSE_DATA; value.encode(buffer); }
        else if (message instanceof CFakePlayerUpdate value) { id = C_FAKE_PLAYER_DATA; value.encode(buffer); }
        else if (message instanceof CFileData value) { id = C_FILE_DATA; value.encode(buffer); }
        else if (message instanceof CPlayMidi value) { id = C_PLAY_MIDI; value.encode(buffer); }
        else if (message instanceof ServerSlicedPacket value) { id = S_SLICED_PACKET; ServerSlicedPacket.encode(value, buffer); }
        else throw new IllegalArgumentException("Unsupported MIDI packet: " + message.getClass().getName());

        byte[] data = new byte[buffer.readableBytes()];
        buffer.readBytes(data);
        return new MidiPacket(id, data);
    }

    private void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(id);
        buffer.writeByteArray(data);
    }

    private static MidiPacket decode(FriendlyByteBuf buffer) {
        return new MidiPacket(buffer.readVarInt(), buffer.readByteArray());
    }

    public static boolean isServerbound(int id) {
        return switch (id) {
            case C_AA775_UPDATE, C_PLAY_NOTE, C_CHANGE_NOTE, C_PLAY_MUSIC, C_SYNC_MUSIC,
                    C_POSE_DATA, C_FAKE_PLAYER_DATA, C_FILE_DATA, C_PLAY_MIDI -> true;
            case S_PLAY_NOTE, S_CHANGE_NOTE, S_PLAY_MUSIC, S_SYNC_MUSIC, S_POSE_DATA,
                    S_SLICED_PACKET -> false;
            default -> throw new IllegalArgumentException("Unknown MIDI packet id: " + id);
        };
    }

    public Object decodeMessage() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
        return switch (id) {
            case C_AA775_UPDATE -> CNoteBlockUpdate.decode(buffer);
            case C_PLAY_NOTE -> CPlayNote.decode(buffer);
            case S_PLAY_NOTE -> SPlayNote.decode(buffer);
            case C_CHANGE_NOTE -> CChangeNote.decode(buffer);
            case S_CHANGE_NOTE -> SChangeNote.decode(buffer);
            case C_PLAY_MUSIC -> CSyncMusic.decode(buffer);
            case S_PLAY_MUSIC -> SSyncMusic.decode(buffer);
            case C_SYNC_MUSIC -> CPlayMusic.decode(buffer);
            case S_SYNC_MUSIC -> SPlayMusic.decode(buffer);
            case C_POSE_DATA -> CPoseData.decode(buffer);
            case S_POSE_DATA -> SPoseData.decode(buffer);
            case C_FAKE_PLAYER_DATA -> CFakePlayerUpdate.decode(buffer);
            case C_FILE_DATA -> CFileData.decode(buffer);
            case C_PLAY_MIDI -> CPlayMidi.decode(buffer);
            case S_SLICED_PACKET -> ServerSlicedPacket.decode(buffer);
            default -> throw new IllegalArgumentException("Unknown MIDI packet id: " + id);
        };
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
