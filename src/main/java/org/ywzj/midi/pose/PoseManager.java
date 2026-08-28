package org.ywzj.midi.pose;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import org.ywzj.midi.all.AllInstruments;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.network.Channel;
import org.ywzj.midi.network.handler.PoseHandler;
import org.ywzj.midi.network.message.CPoseData;
import org.ywzj.midi.pose.handler.NotesHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PoseManager {

    private final static HashMap<ResourceLocation, PlayPose> MAIN_HAND_HOLD_POSE = new HashMap<>();
    private final static HashMap<ResourceLocation, PlayPose> OFF_HAND_HOLD_POSE = new HashMap<>();
    private final static ConcurrentHashMap<UUID, ConcurrentLinkedQueue<PlayPose>> POSE_QUEUE = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<UUID, Pair<PlayPose, Integer>> POSE_CACHE = new ConcurrentHashMap<>();
    private final static HashMap<ResourceLocation, List<NotesHandler>> POSE_NOTES_HANDLER = new HashMap<>();

    public static void registerHoldPose(Instrument instrument, InteractionHand interactionHand, PlayPose holdPose) {
        registerHoldPose(instrument == null ? null : instrument.getInstrumentId(), interactionHand, holdPose);
    }

    public static void registerHoldPose(ResourceLocation instrumentId, InteractionHand interactionHand, PlayPose holdPose) {
        if (instrumentId == null) return;
        if (interactionHand.equals(InteractionHand.MAIN_HAND)) {
            MAIN_HAND_HOLD_POSE.put(instrumentId, holdPose);
        } else if (interactionHand.equals(InteractionHand.OFF_HAND)) {
            OFF_HAND_HOLD_POSE.put(instrumentId, holdPose);
        }
    }

    public static PlayPose getHoldPose(Item instrumentItem, InteractionHand interactionHand) {
        Instrument instrument = AllInstruments.INSTRUMENTS_LOOKUP.get(instrumentItem);
        if (instrument != null) {
            if (interactionHand.equals(InteractionHand.MAIN_HAND)) {
                return MAIN_HAND_HOLD_POSE.get(instrument.getInstrumentId());
            } else if (interactionHand.equals(InteractionHand.OFF_HAND)) {
                return OFF_HAND_HOLD_POSE.get(instrument.getInstrumentId());
            }
        }
        return null;
    }

    public static void registerNotesHandler(NotesHandler notesHandler) {
        POSE_NOTES_HANDLER.computeIfAbsent(notesHandler.getInstrumentId(), k -> new ArrayList<>())
                .add(notesHandler);
    }

    public static void publish(LivingEntity player, PlayPose pose) {
        if (player.level().isClientSide) {
            publishClient(player.getUUID(), pose);
        } else {
            PoseHandler.broadcastPose(player, new CPoseData(player.getUUID(), pose));
        }
    }

    public static void publish(LivingEntity player, PlayPose pose, Instrument instrument, List<Integer> notes) {
        // Attach metadata before either local queuing or server broadcast.
        pose.setNotes(instrument.getInstrumentId(), notes);
        if (player.level().isClientSide) {
            publishClient(player.getUUID(), pose, instrument, notes);
        } else {
            PoseHandler.broadcastPose(player, new CPoseData(player.getUUID(), pose));
        }
    }

    private static void publishClient(UUID playerUuid, PlayPose pose) {
        if (!(playerUuid.equals(Minecraft.getInstance().player.getUUID())
                && Minecraft.getInstance().options.getCameraType().isFirstPerson())) {
            push(playerUuid, pose);
        }
        Channel.CHANNEL.sendToServer(new CPoseData(playerUuid, pose));
    }

    private static void publishClient(UUID playerUuid, PlayPose pose, Instrument instrument, List<Integer> notes) {
        pose.setNotes(instrument.getInstrumentId(), notes);
        if (!(playerUuid.equals(Minecraft.getInstance().player.getUUID())
                && Minecraft.getInstance().options.getCameraType().isFirstPerson())) {
            push(playerUuid, pose);
        }
        Channel.CHANNEL.sendToServer(new CPoseData(playerUuid, pose));
    }

    public static void push(UUID playerUuid, PlayPose pose) {
        POSE_QUEUE.computeIfAbsent(playerUuid, k -> new ConcurrentLinkedQueue<>());
        if (POSE_QUEUE.get(playerUuid).size() <= 10) {
            POSE_QUEUE.get(playerUuid).add(pose);
        }
    }

    public static PlayPose poll(UUID playerUuid) {
        ConcurrentLinkedQueue<PoseManager.PlayPose> poses = POSE_QUEUE.get(playerUuid);
        if (poses != null && poses.size() > 0) {
            PlayPose pose = poses.poll();
            if (pose != null) {
                POSE_CACHE.put(playerUuid, new Pair<>(pose, 1000));
                if (pose.instrumentId != null && pose.notes != null && !pose.notes.isEmpty()) {
                    Instrument instrument = AllInstruments.fromId(pose.instrumentId);
                    if (instrument != null) {
                        POSE_NOTES_HANDLER.getOrDefault(instrument.getInstrumentId(), List.of())
                                .forEach(notesHandler -> notesHandler.handle(playerUuid, pose.notes));
                    }
                }
            }
            return pose;
        }
        return null;
    }

    public static boolean notEmpty(UUID playerUuid) {
        return POSE_QUEUE.get(playerUuid) != null && POSE_QUEUE.get(playerUuid).size() > 0;
    }

    public static PlayPose getCache(UUID playerUuid) {
        if (POSE_CACHE.get(playerUuid) == null) {
            return null;
        }
        int count = POSE_CACHE.get(playerUuid).getSecond();
        count -= 1;
        if (count < 0) {
            clearClientCache(playerUuid);
            return null;
        }
        POSE_CACHE.put(playerUuid, new Pair<>(POSE_CACHE.get(playerUuid).getFirst(), count));
        return POSE_CACHE.get(playerUuid).getFirst();
    }

    public static void clearCache(LivingEntity player) {
        if (player.level().isClientSide) {
            clearClientCache(player.getUUID());
            Channel.CHANNEL.sendToServer(new CPoseData(player.getUUID(), new PlayPose()));
        } else {
            PoseHandler.broadcastPose(player, new CPoseData(player.getUUID(), new PlayPose()));
        }
    }

    public static void clearClientCache(UUID playerUuid) {
        POSE_CACHE.remove(playerUuid);
    }

    public static class PlayPose {

        public Float leftArmX;
        public Float leftArmY;
        public Float leftArmZ;
        public Float leftArmRotX;
        public Float leftArmRotY;
        public Float leftArmRotZ;
        public Float rightArmX;
        public Float rightArmY;
        public Float rightArmZ;
        public Float rightArmRotX;
        public Float rightArmRotY;
        public Float rightArmRotZ;
        public ResourceLocation instrumentId = null;
        public List<Integer> notes = new ArrayList<>();

        public PlayPose() {}

        public PlayPose(Float leftArmX, Float leftArmY, Float leftArmZ, Float leftArmRotX, Float leftArmRotY, Float leftArmRotZ, Float rightArmX, Float rightArmY, Float rightArmZ, Float rightArmRotX, Float rightArmRotY, Float rightArmRotZ) {
            setPose(leftArmX, leftArmY, leftArmZ, leftArmRotX, leftArmRotY, leftArmRotZ, rightArmX, rightArmY, rightArmZ, rightArmRotX, rightArmRotY, rightArmRotZ);
        }

        public PlayPose(Float leftArmX, Float leftArmY, Float leftArmZ, Float leftArmRotX, Float leftArmRotY, Float leftArmRotZ, Float rightArmX, Float rightArmY, Float rightArmZ, Float rightArmRotX, Float rightArmRotY, Float rightArmRotZ, ResourceLocation instrumentId, List<Integer> notes) {
            setPose(leftArmX, leftArmY, leftArmZ, leftArmRotX, leftArmRotY, leftArmRotZ, rightArmX, rightArmY, rightArmZ, rightArmRotX, rightArmRotY, rightArmRotZ);
            setNotes(instrumentId, notes);
        }

        public PlayPose(PlayPose pose) {
            setPose(pose.leftArmX, pose.leftArmY, pose.leftArmZ, pose.leftArmRotX, pose.leftArmRotY, pose.leftArmRotZ, pose.rightArmX, pose.rightArmY, pose.rightArmZ, pose.rightArmRotX, pose.rightArmRotY, pose.rightArmRotZ);
        }

        public void setNotes(ResourceLocation instrumentId, List<Integer> notes) {
            this.instrumentId = instrumentId;
            this.notes = notes;
        }

        private void setPose(Float leftArmX, Float leftArmY, Float leftArmZ, Float leftArmRotX, Float leftArmRotY, Float leftArmRotZ, Float rightArmX, Float rightArmY, Float rightArmZ, Float rightArmRotX, Float rightArmRotY, Float rightArmRotZ) {
            this.leftArmX = leftArmX;
            this.leftArmY = leftArmY;
            this.leftArmZ = leftArmZ;
            this.leftArmRotX = leftArmRotX;
            this.leftArmRotY = leftArmRotY;
            this.leftArmRotZ = leftArmRotZ;
            this.rightArmX = rightArmX;
            this.rightArmY = rightArmY;
            this.rightArmZ = rightArmZ;
            this.rightArmRotX = rightArmRotX;
            this.rightArmRotY = rightArmRotY;
            this.rightArmRotZ = rightArmRotZ;
        }

    }

}
