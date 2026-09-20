package org.ywzj.midi.audio;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.ywzj.midi.all.AllConfigs;
import org.ywzj.midi.all.AllItems;
import org.ywzj.midi.blockentity.SpeakerBlockEntity;
import org.ywzj.midi.item.MusicPlayerItem;
import org.ywzj.midi.network.Channel;
import org.ywzj.midi.network.message.CSyncMusic;
import org.ywzj.midi.network.message.MusicMessage;
import org.ywzj.midi.network.message.SSyncMusic;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ServerSoundManager {

    private final static ConcurrentHashMap<UUID, SoundInfo> SOUNDS = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<UUID, Entity> PLAY_ENTITIES = new ConcurrentHashMap<>();

    public static void dispatch(ServerPlayer sender, MusicMessage message) {
        List<ServerPlayer> players = sender.level().getServer().getPlayerList().getPlayers().stream()
                .filter(serverPlayer -> !serverPlayer.equals(sender) && serverPlayer.level().dimension().equals(sender.level().dimension()) && serverPlayer.distanceTo(sender) < 64)
                .collect(Collectors.toList());
        players = players.subList(0, Math.min(players.size(), AllConfigs.common.maxSyncMusicTo.get()));
        players.add(sender);
        SoundInfo info = SOUNDS.get(message.soundUuid);
        if (info == null) {
            info = new SoundInfo(message, sender.getName().getString());
            SOUNDS.put(message.soundUuid, info);
        }
        if (!info.on) {
            return;
        }
        info.on = message.on;
        if (!info.on) {
            SOUNDS.remove(message.soundUuid);
        }
        for (ServerPlayer player : players) {
            if (!info.portable) {
                BlockEntity be = sender.level().getBlockEntity(new BlockPos((int)info.x, (int)info.y, (int)info.z));
                if (!(be instanceof SpeakerBlockEntity speakerBlockEntity)) {
                    message.on = false;
                    Channel.CHANNEL.sendToPlayer(player, message.getServerMessage());
                    continue;
                } else {
                    speakerBlockEntity.updateData(message.soundUuid, info.musicName, info.playerName);
                }
            } else {
                PLAY_ENTITIES.putIfAbsent(info.deviceUuid, sender);
                Entity playEntity = PLAY_ENTITIES.get(info.deviceUuid);
                if (playEntity.isRemoved()) {
                    message.on = false;
                    PLAY_ENTITIES.remove(info.deviceUuid);
                    Channel.CHANNEL.sendToPlayer(player, message.getServerMessage());
                    continue;
                } else if (playEntity instanceof Player) {
                    boolean isDeviceInBag = ((Player)playEntity).getInventory().items.stream()
                        .anyMatch(itemStack -> {
                            if (itemStack.getItem().equals(AllItems.MUSIC_PLAYER.get())) {
                                UUID deviceUuid = MusicPlayerItem.getUUID(itemStack);
                                return deviceUuid != null && deviceUuid.equals(message.deviceUuid);
                            }
                            return false;
                        });
                    UUID deviceUuid = MusicPlayerItem.getUUID(((Player)playEntity).getOffhandItem());
                    isDeviceInBag |= deviceUuid != null && deviceUuid.equals(message.deviceUuid);
                    isDeviceInBag |= message.isDeviceCarried;
                    if (!isDeviceInBag) {
                        message.on = false;
                        PLAY_ENTITIES.remove(info.deviceUuid);
                        Channel.CHANNEL.sendToPlayer(player, message.getServerMessage());
                        continue;
                    }
                }
                info.x = playEntity.getX();
                info.y = playEntity.getY();
                info.z = playEntity.getZ();
            }
            message.x = info.x;
            message.y = info.y;
            message.z = info.z;
            if (!message.on) {
                PLAY_ENTITIES.remove(info.deviceUuid);
                Channel.CHANNEL.sendToPlayer(player, message.getServerMessage());
                continue;
            }
            if (player.equals(sender) && message instanceof CSyncMusic) {
                SSyncMusic noBytesMessage = (SSyncMusic) message.getServerMessage();
                noBytesMessage.bytes = new byte[0];
                Channel.CHANNEL.sendToPlayer(player, noBytesMessage);
            } else {
                Channel.CHANNEL.sendToPlayer(player, message.getServerMessage());
            }
        }
    }

}

class SoundInfo {

    public double x;
    public double y;
    public double z;
    public boolean on;
    public String musicName;
    public String playerName;
    public boolean portable;
    public UUID deviceUuid;

    public SoundInfo(MusicMessage message, String playerName) {
        this.x = message.x;
        this.y = message.y;
        this.z = message.z;
        this.on = true;
        this.musicName = message.musicName;
        this.playerName = playerName;
        this.portable = message.portable;
        this.deviceUuid = message.deviceUuid;
    }

}
