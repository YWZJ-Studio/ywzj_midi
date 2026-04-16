package org.ywzj.midi.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.network.handler.*;
import org.ywzj.midi.network.message.*;

import java.util.Optional;

import static net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT;
import static net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER;

public class Channel {

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(YwzjMidi.MOD_ID, YwzjMidi.CHANNEL))
            .networkProtocolVersion(() -> YwzjMidi.PROTOCOL)
            .clientAcceptedVersions(YwzjMidi.PROTOCOL::equals)
            .serverAcceptedVersions(YwzjMidi.PROTOCOL::equals)
            .simpleChannel();

    @SubscribeEvent
    public static void onCommonSetupEvent(FMLCommonSetupEvent event) {

        CHANNEL.registerMessage(PacketId.C_AA775_UPDATE.value(), CNoteBlockUpdate.class,
                CNoteBlockUpdate::encode, CNoteBlockUpdate::decode,
                NoteBlockHandler::onClientMessageReceived,
                Optional.of(PLAY_TO_SERVER));

        CHANNEL.registerMessage(PacketId.C_PLAY_NOTE.value(), CPlayNote.class,
                CPlayNote::encode, CPlayNote::decode,
                PlayNoteHandler::onClientMessageReceived,
                Optional.of(PLAY_TO_SERVER));

        CHANNEL.registerMessage(PacketId.S_PLAY_NOTE.value(), SPlayNote.class,
                SPlayNote::encode, SPlayNote::decode,
                PlayNoteHandler::onServerMessageReceived,
                Optional.of(PLAY_TO_CLIENT));

        CHANNEL.registerMessage(PacketId.C_CHANGE_NOTE.value(), CChangeNote.class,
                CChangeNote::encode, CChangeNote::decode,
                ChangeNoteHandler::onClientMessageReceived,
                Optional.of(PLAY_TO_SERVER));

        CHANNEL.registerMessage(PacketId.S_CHANGE_NOTE.value(), SChangeNote.class,
                SChangeNote::encode, SChangeNote::decode,
                ChangeNoteHandler::onServerMessageReceived,
                Optional.of(PLAY_TO_CLIENT));

        CHANNEL.registerMessage(PacketId.C_PLAY_MUSIC.value(), CSyncMusic.class,
                CSyncMusic::encode, CSyncMusic::decode,
                SyncMusicHandler::onClientMessageReceived,
                Optional.of(PLAY_TO_SERVER));

        CHANNEL.registerMessage(PacketId.S_PLAY_MUSIC.value(), SSyncMusic.class,
                SSyncMusic::encode, SSyncMusic::decode,
                SyncMusicHandler::onServerMessageReceived,
                Optional.of(PLAY_TO_CLIENT));

        CHANNEL.registerMessage(PacketId.C_SYNC_MUSIC.value(), CPlayMusic.class,
                CPlayMusic::encode, CPlayMusic::decode,
                PlayMusicHandler::onClientMessageReceived,
                Optional.of(PLAY_TO_SERVER));

        CHANNEL.registerMessage(PacketId.S_SYNC_MUSIC.value(), SPlayMusic.class,
                SPlayMusic::encode, SPlayMusic::decode,
                PlayMusicHandler::onServerMessageReceived,
                Optional.of(PLAY_TO_CLIENT));

        CHANNEL.registerMessage(PacketId.C_POSE_DATA.value(), CPoseData.class,
                CPoseData::encode, CPoseData::decode,
                PoseHandler::onClientMessageReceived,
                Optional.of(PLAY_TO_SERVER));

        CHANNEL.registerMessage(PacketId.S_POSE_DATA.value(), SPoseData.class,
                SPoseData::encode, SPoseData::decode,
                PoseHandler::onServerMessageReceived,
                Optional.of(PLAY_TO_CLIENT));

        CHANNEL.registerMessage(PacketId.C_FAKE_PLAYER_DATA.value(), CFakePlayerUpdate.class,
                CFakePlayerUpdate::encode, CFakePlayerUpdate::decode,
                FakePlayerDataHandler::onServerMessageReceived,
                Optional.of(PLAY_TO_SERVER));

        CHANNEL.registerMessage(PacketId.C_FILE_DATA.value(), CFileData.class,
                CFileData::encode, CFileData::decode,
                FileDataHandler::onServerMessageReceived,
                Optional.of(PLAY_TO_SERVER));

        CHANNEL.registerMessage(PacketId.C_PLAY_MIDI.value(), CPlayMidi.class,
                CPlayMidi::encode, CPlayMidi::decode,
                ServerPlayMidiHandler::onServerMessageReceived,
                Optional.of(PLAY_TO_SERVER));

        CHANNEL.registerMessage(PacketId.S_SLICED_PACKET.value(), ServerSlicedPacket.class,
                ServerSlicedPacket::encode, ServerSlicedPacket::decode,
                ServerSlicedPacket::handle,
                Optional.of(PLAY_TO_CLIENT));

    }

}

enum PacketId {

    C_AA775_UPDATE(99),
    C_PLAY_NOTE(100),
    S_PLAY_NOTE(101),
    C_CHANGE_NOTE(102),
    S_CHANGE_NOTE(103),
    C_PLAY_MUSIC(104),
    S_PLAY_MUSIC(105),
    C_SYNC_MUSIC(106),
    S_SYNC_MUSIC(107),
    C_POSE_DATA(108),
    S_POSE_DATA(109),
    C_FAKE_PLAYER_DATA(110),
    C_FILE_DATA(111),
    C_PLAY_MIDI(112),
    S_SLICED_PACKET(113);

    private final int id;

    PacketId(int id) {
        this.id = id;
    }

    int value() {
        return id;
    }

}
