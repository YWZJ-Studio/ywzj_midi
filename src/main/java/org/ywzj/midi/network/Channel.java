package org.ywzj.midi.network;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.network.handler.*;
import org.ywzj.midi.network.message.*;

public final class Channel {

    public static final Sender CHANNEL = new Sender();

    private Channel() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(YwzjMidi.PROTOCOL);
        registrar.playBidirectional(MidiPacket.TYPE, MidiPacket.STREAM_CODEC, Channel::handle);
    }

    private static void handle(MidiPacket packet, IPayloadContext context) {
        boolean serverbound = MidiPacket.isServerbound(packet.id());
        if ((serverbound && context.flow() != PacketFlow.SERVERBOUND)
                || (!serverbound && context.flow() != PacketFlow.CLIENTBOUND)) {
            context.disconnect(Component.literal("Invalid MIDI packet direction"));
            return;
        }
        Object message = packet.decodeMessage();
        switch (packet.id()) {
            case MidiPacket.C_AA775_UPDATE -> NoteBlockHandler.onClientMessageReceived((CNoteBlockUpdate) message, context);
            case MidiPacket.C_PLAY_NOTE -> PlayNoteHandler.onClientMessageReceived((CPlayNote) message, context);
            case MidiPacket.S_PLAY_NOTE -> PlayNoteHandler.onServerMessageReceived((SPlayNote) message, context);
            case MidiPacket.C_CHANGE_NOTE -> ChangeNoteHandler.onClientMessageReceived((CChangeNote) message, context);
            case MidiPacket.S_CHANGE_NOTE -> ChangeNoteHandler.onServerMessageReceived((SChangeNote) message, context);
            case MidiPacket.C_PLAY_MUSIC -> SyncMusicHandler.onClientMessageReceived((CSyncMusic) message, context);
            case MidiPacket.S_PLAY_MUSIC -> SyncMusicHandler.onServerMessageReceived((SSyncMusic) message, context);
            case MidiPacket.C_SYNC_MUSIC -> PlayMusicHandler.onClientMessageReceived((CPlayMusic) message, context);
            case MidiPacket.S_SYNC_MUSIC -> PlayMusicHandler.onServerMessageReceived((SPlayMusic) message, context);
            case MidiPacket.C_POSE_DATA -> PoseHandler.onClientMessageReceived((CPoseData) message, context);
            case MidiPacket.S_POSE_DATA -> PoseHandler.onServerMessageReceived((SPoseData) message, context);
            case MidiPacket.C_FAKE_PLAYER_DATA -> FakePlayerDataHandler.onServerMessageReceived((CFakePlayerUpdate) message, context);
            case MidiPacket.C_FILE_DATA -> FileDataHandler.onServerMessageReceived((CFileData) message, context);
            case MidiPacket.C_PLAY_MIDI -> ServerPlayMidiHandler.onServerMessageReceived((CPlayMidi) message, context);
            case MidiPacket.S_SLICED_PACKET -> ServerSlicedPacket.handle((ServerSlicedPacket) message, context);
            default -> throw new IllegalArgumentException("Unknown MIDI packet id: " + packet.id());
        }
    }

    public static final class Sender {
        public void sendToServer(Object message) {
            PacketDistributor.sendToServer(MidiPacket.wrap(message));
        }

        public void sendToPlayer(ServerPlayer player, Object message) {
            PacketDistributor.sendToPlayer(player, MidiPacket.wrap(message));
        }

        public void sendToAllPlayers(Object message) {
            PacketDistributor.sendToAllPlayers(MidiPacket.wrap(message));
        }
    }

}
