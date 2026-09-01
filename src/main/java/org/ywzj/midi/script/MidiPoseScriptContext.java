package org.ywzj.midi.script;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MidiPoseScriptContext {

    private int note;
    private int velocity;
    private List<Integer> notes;
    private ResourceLocation instrumentId;
    private long tick;
    private String hand;
    private final Map<String, Object> state = new HashMap<>();

    public MidiPoseScriptContext() {}

    public MidiPoseScriptContext(int note, int velocity, ResourceLocation instrumentId) {
        this.note = note;
        this.velocity = velocity;
        this.instrumentId = instrumentId;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public int getNote() {
        return note;
    }

    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    public int getVelocity() {
        return velocity;
    }

    public void setNotes(List<Integer> notes) {
        this.notes = notes;
    }

    public List<Integer> getNotes() {
        return notes;
    }

    public void setInstrumentId(ResourceLocation instrumentId) {
        this.instrumentId = instrumentId;
    }

    public ResourceLocation getInstrumentId() {
        return instrumentId;
    }

    public void setTick(long tick) {
        this.tick = tick;
    }

    public long getTick() {
        return tick;
    }

    public void setHand(String hand) {
        this.hand = hand;
    }

    public String getHand() {
        return hand;
    }

    public boolean isMainHand() {
        return "main_hand".equals(hand);
    }

    public boolean isOffHand() {
        return "off_hand".equals(hand);
    }

    public float getFloat(String name, float defaultValue) {
        Object value = state.get(name);
        if (value instanceof Number num) {
            return num.floatValue();
        }
        return defaultValue;
    }

    public void setFloat(String name, float value) {
        state.put(name, value);
    }

    public boolean getBool(String name, boolean defaultValue) {
        Object value = state.get(name);
        if (value instanceof Boolean b) {
            return b;
        }
        return defaultValue;
    }

    public void setBool(String name, boolean value) {
        state.put(name, value);
    }

    public int getInt(String name, int defaultValue) {
        Object value = state.get(name);
        if (value instanceof Number num) {
            return num.intValue();
        }
        return defaultValue;
    }

    public void setInt(String name, int value) {
        state.put(name, value);
    }

    public void resetState() {
        state.clear();
    }

}
