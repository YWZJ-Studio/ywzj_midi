package org.ywzj.midi.network.handler;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.ywzj.midi.entity.FakePlayerEntity;
import org.ywzj.midi.gui.screen.ServerMidiScreen;
import org.ywzj.midi.instrument.player.ServerMidiPlayer;
import org.ywzj.midi.network.message.CPlayMidi;

public class ServerPlayMidiHandler {

    public static void onServerMessageReceived(CPlayMidi message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(((ServerPlayer) ctx.player()).level().getEntity(message.conductorId) instanceof FakePlayerEntity conductor)) {
                return;
            }
            if (conductor.serverMidiPlayer == null) {
                conductor.serverMidiPlayer = new ServerMidiPlayer((ServerPlayer) ctx.player(), conductor);
            }
            ServerMidiPlayer midiPlayer = conductor.serverMidiPlayer;
            if (message.opCode == ServerMidiScreen.OpCode.PLAY.value) {
                midiPlayer.init(message);
                midiPlayer.task(3);
            } else if (message.opCode == ServerMidiScreen.OpCode.STOP.value) {
                midiPlayer.stopPlay();
            } else if (message.opCode == ServerMidiScreen.OpCode.LOOP.value) {
                midiPlayer.setLoop();
            }
        });
     }

}
