package org.ywzj.midi.gui.waterfall;

public record WaterfallNote(int note, int velocity, long startTick, long duration, int channel) {}
