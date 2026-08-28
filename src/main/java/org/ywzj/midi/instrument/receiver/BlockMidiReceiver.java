package org.ywzj.midi.instrument.receiver;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.all.AllInstruments;
import org.ywzj.midi.blockentity.AABlockEntity;

import javax.sound.midi.MidiMessage;

public class BlockMidiReceiver extends MidiReceiver {

    private final AABlockEntity aaBlockEntity;

    public BlockMidiReceiver(AABlockEntity aaBlockEntity) {
        super(AllInstruments.fromId(YwzjMidi.modLocation("aa775")), Minecraft.getInstance().player, new Vec3(aaBlockEntity.getBlockPos().getX(), aaBlockEntity.getBlockPos().getY(), aaBlockEntity.getBlockPos().getZ()));
        this.aaBlockEntity = aaBlockEntity;
    }

    @Override
    public void send(MidiMessage message, long timeStamp, int delay) {
        byte[] midi = message.getMessage();
        try {
            int statusByte = midi[0] & 0xFF;
            int messageType = statusByte >> 4;
            if (messageType == 0x09) {
                int note = midi[1] & 0xFF;
                if (aaBlockEntity.note == note) {
                    aaBlockEntity.clientSendData(true);
                }
            } else if (messageType == 0x08) {
                int note = midi[1] & 0xFF;
                if (aaBlockEntity.note == note) {
                    aaBlockEntity.clientSendData(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopPose() {}

}
