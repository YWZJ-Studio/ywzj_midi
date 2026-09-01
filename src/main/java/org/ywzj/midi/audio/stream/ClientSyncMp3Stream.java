package org.ywzj.midi.audio.stream;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.compress.utils.IOUtils;
import org.lwjgl.BufferUtils;
import org.ywzj.midi.all.AllItems;
import org.ywzj.midi.audio.sound.ClientSyncMusicSound;
import org.ywzj.midi.item.MusicPlayerItem;
import org.ywzj.midi.network.Channel;
import org.ywzj.midi.network.message.CSyncMusic;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

public class ClientSyncMp3Stream implements AudioStream {

    private final ClientSyncMusicSound soundInstance;
    private final AudioInputStream stream;
    private final byte[] array;
    private int readOffset;
    private int sendOffset;
    private boolean isReading;
    private final ByteArrayOutputStream sendBuffer;

    public ClientSyncMp3Stream(ClientSyncMusicSound soundInstance) throws UnsupportedAudioFileException, IOException {
        this.soundInstance = soundInstance;
        AudioInputStream originalInputStream = new MpegAudioFileReader().getAudioInputStream(soundInstance.url);
        AudioFormat originalFormat = originalInputStream.getFormat();
        AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, originalFormat.getSampleRate(), 16,
                originalFormat.getChannels(), originalFormat.getChannels() * 2, originalFormat.getFrameRate(), false);
        AudioInputStream targetInputStream = AudioSystem.getAudioInputStream(targetFormat, originalInputStream);
        targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, originalFormat.getSampleRate(), 16,
                1, 2, originalFormat.getFrameRate(), false);
        this.stream = AudioSystem.getAudioInputStream(targetFormat, targetInputStream);
        this.array = IOUtils.toByteArray(stream);
        this.readOffset = 0;
        this.sendOffset = 0;
        this.isReading = true;
        this.sendBuffer = new ByteArrayOutputStream();
        nioToOtherClient();
    }

    @Override
    public AudioFormat getFormat() {
        return stream.getFormat();
    }

    @Override
    public ByteBuffer read(int size) {
        byte[] bytes = new byte[size];
        if (array.length > readOffset) {
            bytes = Arrays.copyOfRange(array, readOffset, Math.min(readOffset + size, array.length));
        } else {
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                } catch (Exception ignore) {}
                soundInstance.stopThis();
            }).start();
        }
        readOffset += size;
        sendBuffer.writeBytes(bytes);
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(size);
        byteBuffer.put(bytes);
        byteBuffer.flip();
        return byteBuffer;
    }

    @Override
    public void close() throws IOException {
        stream.close();
        soundInstance.playerInstance.callbackSyncPlayEnd();
        this.isReading = false;
    }

    private void nioToOtherClient() {
        int size = 10240;
        new Thread(() -> {
            while (isReading) {
                if (sendBuffer.size() >= sendOffset + size) {
                    assert Minecraft.getInstance().player != null;
                    ItemStack itemCarried = Minecraft.getInstance().player.inventoryMenu.getCarried();
                    boolean isDeviceCarried = itemCarried.getItem().equals(AllItems.MUSIC_PLAYER.get())
                            && Objects.equals(MusicPlayerItem.getUUID(itemCarried), soundInstance.deviceUuid);
                    byte[] bytes = Arrays.copyOfRange(sendBuffer.toByteArray(), sendOffset, sendOffset + size);
                    Channel.CHANNEL.sendToServer(new CSyncMusic(soundInstance.getPlayPos(),
                            stream.getFormat().getSampleRate(),
                            stream.getFormat().getFrameRate(),
                            soundInstance.musicName,
                            soundInstance.soundUuid,
                            soundInstance.playerInstance.portable,
                            soundInstance.playerInstance.deviceUuid,
                            isDeviceCarried,
                            bytes));
                    sendOffset += size;
                }
                try {
                    Thread.sleep(100);
                } catch (Exception ignore) {}
            }
        }).start();
    }

}
