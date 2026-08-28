package org.ywzj.midi.gui.waterfall;

import org.ywzj.midi.gui.screen.MidiInstrumentScreen;
import org.ywzj.midi.gui.widget.ValueSlider;
import org.ywzj.midi.instrument.player.InstrumentMidiPlayer;

import javax.sound.midi.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class WaterfallPlayer extends InstrumentMidiPlayer {

    private final List<WaterfallNote> waterNotes = new ArrayList<>();
    private volatile long currentTick;
    private volatile long lastStepTime;
    private volatile double currentMsPerTick;
    private ValueSlider progressBar;

    public WaterfallPlayer(MidiInstrumentScreen screen) {
        super(screen);
    }

    public WaterfallPlayer(Runnable endCallback) {
        super(endCallback);
    }

    @Override
    public boolean open(File file) {
        waterNotes.clear();
        currentTick = 0;
        // Pre-parse notes from all tracks before super.open() consumes them
        try {
            Sequence sequence = MidiSystem.getSequence(file);
            for (Track track : sequence.getTracks()) {
                parseWaterNotes(track);
            }
            waterNotes.sort(Comparator.comparingLong(WaterfallNote::startTick));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.open(file);
    }

    public void setProgressBar(ValueSlider progressBar) {
        this.progressBar = progressBar;
    }

    @Override
    public void handleStep() {
        if (step < allEvents.size()) {
            currentTick = allEvents.get(step).getTick();
        }
        lastStepTime = System.currentTimeMillis();
        currentMsPerTick = msPerTick;
        updateProgressBar();
    }

    private void updateProgressBar() {
        if (progressBar == null || allEvents.isEmpty()) return;
        if (progressBar.isUpdated()) {
            int seekStep = (int) (allEvents.size() * ((float) progressBar.value / progressBar.maxValue));
            if (seekStep >= 0 && seekStep < allEvents.size()) {
                step = seekStep;
                long targetTick = allEvents.get(step).getTick();
                for (var channel : channels) {
                    channel.set(targetTick);
                }
                for (long lookupTick = targetTick; lookupTick >= 0; lookupTick -= 1) {
                    if (msPerTickChanges.get(lookupTick) != null) {
                        msPerTick = msPerTickChanges.get(lookupTick);
                        break;
                    }
                }
                lastTick = targetTick;
                currentTick = targetTick;
            }
        } else if (step % 10 == 0) {
            progressBar.updateValue((double) step / allEvents.size());
        }
    }

    public List<WaterfallNote> getWaterNotes() {
        return waterNotes;
    }

    public long getCurrentTick() {
        return currentTick;
    }

    public long getLastStepTime() {
        return lastStepTime;
    }

    public double getCurrentMsPerTick() {
        return currentMsPerTick;
    }

    private void parseWaterNotes(Track track) {
        // key: (channel << 8) | note, value: pending note info
        record Pending(int note, int velocity, long startTick, int channel) {}
        var pending = new HashMap<Integer, Pending>();

        for (int i = 0; i < track.size(); i++) {
            MidiEvent event = track.get(i);
            if (!(event.getMessage() instanceof ShortMessage sm)) continue;

            int cmd = sm.getCommand();
            int data1 = sm.getData1();
            int data2 = sm.getData2();

            if (cmd == ShortMessage.NOTE_ON && data2 > 0) {
                int key = (sm.getChannel() << 8) | data1;
                pending.put(key, new Pending(data1, data2, event.getTick(), sm.getChannel()));
            } else if (cmd == ShortMessage.NOTE_OFF || (cmd == ShortMessage.NOTE_ON && data2 == 0)) {
                int key = (sm.getChannel() << 8) | data1;
                Pending p = pending.remove(key);
                if (p != null) {
                    long duration = event.getTick() - p.startTick;
                    if (duration > 0) {
                        waterNotes.add(new WaterfallNote(p.note, p.velocity, p.startTick, duration, p.channel));
                    }
                }
            }
        }
    }
}
