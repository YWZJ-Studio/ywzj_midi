package org.ywzj.midi.instrument.player;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.instrument.receiver.MidiReceiver;
import org.ywzj.midi.util.ComponentUtils;

import javax.sound.midi.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class MidiPlayer {

    protected final Player operator;
    protected final List<Channel> channels = new LinkedList<>();
    protected final HashMap<Long, Integer> bpmChanges = new HashMap<>();
    protected final HashMap<Long, Double> msPerTickChanges = new HashMap<>();
    protected final List<MidiEvent> allEvents = new ArrayList<>();
    protected boolean isPlaying = false;
    protected int bpm = 0;
    protected double msPerTick = 0;
    protected long lastTick = 0;
    protected int step = 0;
    protected final ConcurrentHashMap<Channel, HashMap<Integer, Integer>> channelPlayingNotes = new ConcurrentHashMap<>();
    protected final Lock playLock = new ReentrantLock();

    public MidiPlayer(Player operator) {
        this.operator = operator;
    }

    public boolean open(File file) {
        channels.clear();
        msPerTickChanges.clear();
        allEvents.clear();
        isPlaying = false;
        try {
            Sequence sequence = MidiSystem.getSequence(file);
            Track[] tracks = sequence.getTracks();
            int ticksPerBeat = sequence.getResolution();
            for (Track track : tracks) {
                for (int i = 0; i < track.size(); i++) {
                    if (track.get(i).getMessage() instanceof MetaMessage metaMessage) {
                        if (metaMessage.getType() == 0x51) {
                            byte[] data = metaMessage.getData();
                            int microsecondsPerBeat = ((data[0] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
                            int bpm = Math.round(60000000.0f / microsecondsPerBeat);
                            double msPerTick = 60000.0 / (bpm * ticksPerBeat);
                            bpmChanges.put(track.get(i).getTick(), bpm);
                            msPerTickChanges.put(track.get(i).getTick(), msPerTick);
                        }
                    }
                    allEvents.add(track.get(i));
                }
            }
            allEvents.sort(Comparator.comparingLong(MidiEvent::getTick));
            for (Track track : tracks) {
                Channel channel = new Channel(track);
                if (channel.isInstrument()) {
                    channels.add(channel);
                }
            }
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public void play(int delaySeconds) {
        if (allEvents.size() == 0) {
            return;
        }
        new Thread(() -> {
            if (!playLock.tryLock()) {
                endCallback();
                return;
            }
            isPlaying = true;
            countDown(delaySeconds);
            try {
                channelPlayingNotes.clear();
                msPerTick = 0;
                lastTick = 0;
                step = 0;
                while (step < allEvents.size()) {
                    MidiEvent event = allEvents.get(step);
                    tickWait(event);
                    if (!isPlaying || (operator.level().isClientSide && !operator.isAlive())) {
                        return;
                    }
                    for (Channel channel : channels) {
                        channel.tick(event.getTick(), channelPlayingNotes);
                    }
                    lastTick = event.getTick();
                    handleStep();
                    step += 1;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            } finally {
                isPlaying = false;
                channels.forEach(Channel::reset);
                endCallback();
                playLock.unlock();
            }
        }).start();
    }

    public abstract void handleStep();

    public abstract void endCallback();

    private void countDown(int seconds) {
        for (int i = seconds; i > 0; i--) {
            operator.sendSystemMessage(ComponentUtils.literal(i + ""));
            try {
                Thread.sleep(1000);
            } catch (Exception ignore) {}
        }
    }

    private void tickWait(MidiEvent event) {
        if (msPerTickChanges.get(event.getTick()) != null) {
            bpm = bpmChanges.get(event.getTick());
            msPerTick = msPerTickChanges.get(event.getTick());
        }
        long deltaTick = event.getTick() - lastTick;
        if (deltaTick > 0) {
            try {
                Thread.sleep((long) (msPerTick * deltaTick));
            } catch (Exception ignore) {}
        }
    }

    public void stopPlay() {
        isPlaying = false;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public static class Channel {

        public String instrumentName;
        public int channelId;
        private Instrument instrument;
        private float volume;
        private int index = 0;
        private final Track track;
        private final ConcurrentHashMap<UUID, MidiReceiver> receivers = new ConcurrentHashMap<>();

        public Channel(Track track) {
            this.track = track;
            boolean isInstrument = false;
            String instrumentName = null;
            String soundbankName = null;
            for (int index = 0; index < track.size(); index++) {
                MidiMessage message = track.get(index).getMessage();
                if (message instanceof MetaMessage metaMessage) {
                    int type = metaMessage.getType();
                    if (type == 0x03) {
                        try {
                            instrumentName = new String(metaMessage.getData(), StandardCharsets.UTF_8);
                        } catch (Exception ignore) {}
                    }
                } else if (message instanceof ShortMessage shortMessage) {
                    isInstrument = true;
                    if (shortMessage.getCommand() == ShortMessage.PROGRAM_CHANGE) {
                        this.channelId = shortMessage.getChannel();
                        int program = shortMessage.getData1();
                        try {
                            Soundbank soundbank = MidiSystem.getSynthesizer().getDefaultSoundbank();
                            if (soundbank != null) {
                                javax.sound.midi.Instrument[] instruments = soundbank.getInstruments();
                                if (program >= 0 && program < instruments.length) {
                                    javax.sound.midi.Instrument instrument = instruments[program];
                                    soundbankName = instrument.getName();
                                }
                            }
                        } catch (Exception ignore) {}
                    }
                }
            }
            if (isInstrument) {
                if (instrumentName == null && soundbankName == null) {
                    this.instrumentName = "NA";
                } else {
                    this.instrumentName = instrumentName == null ? soundbankName : instrumentName;
                }
            }
        }

        public void use(Instrument instrument) {
            this.instrument = instrument;
        }

        public void volume(float volume) {
            this.volume = volume;
        }

        public void registerReceiver(LivingEntity player) {
            if (instrument == null) {
                return;
            }
            receivers.put(player.getUUID(), instrument.receiver(player, null));
        }

        public void registerReceiver(MidiReceiver receiver) {
            receivers.put(receiver.getPlayer().getUUID(), receiver);
        }

        public void clearReceiver() {
            receivers.clear();
        }

        public ConcurrentHashMap<UUID, MidiReceiver> getReceivers() {
            return receivers;
        }

        public void tick(long tick, ConcurrentHashMap<Channel, HashMap<Integer, Integer>> channelPlayingNotes) {
            HashMap<Integer, Integer> notesAndVelocity = channelPlayingNotes.getOrDefault(this, new HashMap<>());
            if (index == track.size()) {
                return;
            }
            while (track.get(index).getTick() <= tick) {
                MidiMessage message = track.get(index).getMessage();
                if (message instanceof ShortMessage shortMessage) {
                    if (shortMessage.getCommand() == ShortMessage.NOTE_ON) {
                        try {
                            ShortMessage volShortMessage = (ShortMessage) shortMessage.clone();
                            volShortMessage.setMessage(shortMessage.getStatus(), shortMessage.getChannel(), shortMessage.getData1(), (int) (shortMessage.getData2() * volume));
                            message = volShortMessage;
                            if (shortMessage.getData2() > 0) {
                                notesAndVelocity.put(shortMessage.getData1(), shortMessage.getData2());
                            } else {
                                notesAndVelocity.remove(shortMessage.getData1());
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    } else if (shortMessage.getCommand() == ShortMessage.NOTE_OFF) {
                        notesAndVelocity.remove(shortMessage.getData1());
                    }
                }
                sendAll(message, tick);
                index += 1;
                if (index == track.size()) {
                    channelPlayingNotes.put(this, notesAndVelocity);
                    return;
                }
            }
            if (notesAndVelocity.size() > 0) {
                channelPlayingNotes.put(this, notesAndVelocity);
            } else {
                channelPlayingNotes.remove(this);
            }
        }

        public void sendAll(MidiMessage message, long tick) {
            receivers.values().forEach(receiver -> {
                if (receiver.useLoop()) {
                    receiver.send(message, tick, 0);
                } else {
                    receiver.send(message, tick, 50);
                }
            });
        }

        public void set(long tick) {
            receivers.values().forEach(MidiReceiver::stopAllKeys);
            index = 0;
            while (index < track.size() && track.get(index).getTick() <= tick) {
                MidiEvent event = track.get(index);
                if (event.getMessage() instanceof ShortMessage shortMessage) {
                    if (shortMessage.getCommand() == ShortMessage.PROGRAM_CHANGE) {
                        sendAll(shortMessage, event.getTick());
                    }
                }
                index += 1;
            }
        }

        public void reset() {
            index = 0;
            receivers.values().forEach(MidiReceiver::stopPose);
            receivers.values().forEach(MidiReceiver::stopAllKeys);
        }

        public boolean isInstrument() {
            return instrumentName != null;
        }

    }

}
