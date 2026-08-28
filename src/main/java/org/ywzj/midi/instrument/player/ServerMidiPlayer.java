package org.ywzj.midi.instrument.player;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.ywzj.midi.all.AllInstruments;
import org.ywzj.midi.entity.FakePlayerEntity;
import org.ywzj.midi.network.message.CPlayMidi;
import org.ywzj.midi.pose.action.ConductorPose;
import org.ywzj.midi.storage.ConductorConfig;
import org.ywzj.midi.storage.MidiFiles;
import org.ywzj.midi.util.FileTransfer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ServerMidiPlayer extends MidiPlayer {

    private final ConductorPose conductorPose;
    private boolean loop = false;
    private int playIndex = 0;
    private List<String> midPlayList;
    private List<String> configList;
    private List<String> channelFilter;
    private final FakePlayerEntity conductor;

    public ServerMidiPlayer(Player operator, FakePlayerEntity conductor) {
        super(operator);
        this.conductor = conductor;
        this.conductorPose = new ConductorPose(conductor);
    }

    public void init(CPlayMidi message) {
        Gson gson = new Gson();
        java.lang.reflect.Type type = new TypeToken<List<String>>(){}.getType();
        midPlayList = gson.fromJson(message.midFileNames, type);
        configList = gson.fromJson(message.configs, type);
        playIndex = 0;
        stopPlay();
    }

    public void task(int delaySeconds) {
        String midFileName = midPlayList.get(playIndex);
        String config = configList.get(playIndex);
        int maxTry = 5;
        while (FileTransfer.TASKS.get(operator.getUUID()).get(midFileName) != null && maxTry > 0) {
            maxTry -= 1;
            try {
                Thread.sleep(1000);
            } catch (Exception ignore) {}
            if (maxTry == 0) {
                return;
            }
        }
        boolean result = open(MidiFiles.MID_PATH.toPath().resolve(midFileName).toFile());
        if (result) {
            Gson gson = new Gson();
            java.lang.reflect.Type type = new TypeToken<ConductorConfig.MidConfig>(){}.getType();
            ConductorConfig.MidConfig midConfig = gson.fromJson(config, type);
            List<LivingEntity> players = conductor.level().getEntities(conductor,
                    new AABB(conductor.getX()-32.0D, conductor.getY()-32.0D, conductor.getZ()-32.0D, conductor.getX()+32.0D, conductor.getY()+32.0D, conductor.getZ()+32.0D))
                    .stream()
                    .filter(entity -> entity instanceof Player || entity instanceof FakePlayerEntity)
                    .map(entity -> (LivingEntity) entity)
                    .collect(Collectors.toList());
            List<MidiPlayer.Channel> channels = getChannels();
            for (int index = 0; index < channels.size(); index++) {
                if (midConfig != null && midConfig.getChannelUnits().size() > index) {
                    ConductorConfig.ChannelUnit unit = midConfig.getChannelUnits().get(index);
                    List<LivingEntity> targetPlayers = players.stream()
                            .filter(p -> unit.getPlayerNames().contains(p.getName().getString()))
                            .collect(Collectors.toList());
                    if (targetPlayers.size() == 0) {
                        targetPlayers.add(conductor);
                    }
                    channels.get(index).use(AllInstruments.fromId(unit.getInstrumentIdAsResourceLocation()));
                    channels.get(index).volume((float) unit.getVol() / 127);
                    channels.get(index).clearReceiver();
                    for (LivingEntity p : targetPlayers) {
                        channels.get(index).registerReceiver(p);
                    }
                    channelFilter = midConfig.getChannelFilter();
                }
            }
            play(delaySeconds);
            playIndex = (playIndex + 1) % midPlayList.size();
        }
    }

    public void setLoop() {
        this.loop = true;
    }

    @Override
    public void handleStep() {
        handlePose();
    }

    @Override
    public void endCallback() {
        conductorPose.stop();
        if (playIndex != 0 || loop) {
            task(0);
        }
    }

    @Override
    public void stopPlay() {
        loop = false;
        playIndex = 0;
        isPlaying = false;
    }

    private void handlePose() {
        channelPlayingNotes.keySet().stream().filter(channel -> channelFilter.contains(channel.instrumentName)).forEach(channelPlayingNotes::remove);
        if (channelPlayingNotes.size() > 1) {
            conductorPose.pause(false);
            if (!conductorPose.loop) {
                conductorPose.loop();
            }
            conductorPose.setBpm(bpm);
            AtomicReference<LivingEntity> lookAtPlayer = new AtomicReference<>();
            int maxVol = 0;
            for (Map.Entry<MidiPlayer.Channel, HashMap<Integer, Integer>> channelHashMapEntry : channelPlayingNotes.entrySet()) {
                Optional<Integer> vol = channelHashMapEntry.getValue().values().stream().max(Integer::compare);
                if (vol.isPresent()) {
                    if (vol.get() > maxVol) {
                        maxVol = vol.get();
                        channelHashMapEntry.getKey().getReceivers().values().stream().findAny().ifPresent(midiReceiver -> lookAtPlayer.set(midiReceiver.getPlayer()));
                    }
                }
            }
            conductorPose.setVelocity(maxVol);
            if (!conductorPose.player.equals(lookAtPlayer.get())) {
                conductorPose.setLookAtPlayer(lookAtPlayer.get());
            }
        } else {
            conductorPose.pause(true);
        }
    }

}
