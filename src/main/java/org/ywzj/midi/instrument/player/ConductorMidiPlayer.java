package org.ywzj.midi.instrument.player;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.ywzj.midi.gui.screen.ConductorScreen;
import org.ywzj.midi.gui.waterfall.WaterfallPlayer;
import org.ywzj.midi.gui.widget.ValueSlider;
import org.ywzj.midi.pose.action.ConductorPose;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ConductorMidiPlayer extends WaterfallPlayer {

    private final ConductorScreen conductorScreen;
    protected final ConductorPose conductorPose = new ConductorPose(Minecraft.getInstance().player);

    public ConductorMidiPlayer(ConductorScreen conductorScreen) {
        super(conductorScreen::callbackPlayButton);
        this.conductorScreen = conductorScreen;
    }

    @Override
    public void handleStep() {
        super.handleStep();
        handlePose();
        updateVolume();
    }

    @Override
    public void endCallback() {
        conductorPose.stop();
        conductorScreen.callbackPlayButton();
    }

    private void handlePose() {
        if (!conductorScreen.needConductor) {
            conductorPose.setLookAtPlayer(null);
            return;
        }
        channelPlayingNotes.keySet().stream().filter(channel -> conductorScreen.channelFilterButton.getValues().contains(channel.instrumentName)).forEach(channelPlayingNotes::remove);
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

    public void updateVolume() {
        conductorScreen.volumeSliders.stream()
                .filter(ValueSlider::isUpdated)
                .forEach(volumeSlider -> conductorScreen.volumeSliderToChannel.get(volumeSlider).volume((float) volumeSlider.value / volumeSlider.maxValue));
    }

}
