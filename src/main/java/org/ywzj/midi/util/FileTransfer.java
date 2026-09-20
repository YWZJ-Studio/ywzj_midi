package org.ywzj.midi.util;

import net.minecraft.world.entity.player.Player;
import org.apache.commons.io.FileUtils;
import org.ywzj.midi.network.Channel;
import org.ywzj.midi.network.message.CFileData;
import org.ywzj.midi.storage.MidiFiles;

import javax.sound.midi.MidiSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FileTransfer {

    public static final ConcurrentHashMap<UUID, HashMap<String, ByteArrayOutputStream>> TASKS = new ConcurrentHashMap<>();

    public static void upload(File file, short type) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] networkData = new byte[8192];
            int readSize;
            while ((readSize = fis.read(networkData)) >= 0) {
                Channel.CHANNEL.sendToServer(new CFileData(fis.available() == 0,
                        networkData.length == readSize ? networkData : Arrays.copyOf(networkData, readSize),
                        type,
                        file.getName()));
            }
            fis.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void receive(Player player, CFileData data) {
        HashMap<String, ByteArrayOutputStream> playerTasks = TASKS.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
        if (!playerTasks.containsKey(data.fileName) && playerTasks.size() == 128) {
            return;
        }
        ByteArrayOutputStream bs = playerTasks.computeIfAbsent(data.fileName, k -> new ByteArrayOutputStream());
        try {
            bs.write(data.bytes);
            if (data.eof) {
                byte[] networkBytes = bs.toByteArray();
                if (data.type == 0) {
                    File file = MidiFiles.MID_PATH.toPath().resolve(data.fileName).toFile();
                    try {
                        MidiSystem.getSequence(new ByteArrayInputStream(networkBytes));
                        FileUtils.writeByteArrayToFile(file, networkBytes);
                    } catch (Exception ignore) {}
                }
                bs.close();
                playerTasks.remove(data.fileName);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}
