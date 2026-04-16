package org.ywzj.midi.all;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.util.MidiUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class AllSounds {

    public static final ConcurrentHashMap<String, DeferredRegister<SoundEvent>> SOUNDS = new ConcurrentHashMap<>();

    public static final HashMap<String, LinkedHashMap<String, SoundEvent>> INSTRUMENT_WITH_SOUNDS = new LinkedHashMap<>();

    public static RegistryObject<SoundEvent> MUSIC = registerSoundEvent("music");

    public static void registerKeys(String namespace, String instrumentName, int keyStart, int keyEnd, String variant) {
        if (keyStart <= 0 || keyEnd <= 0) {
            return;
        }
        for (int note = keyStart; note <= keyEnd; note++) {
            registerKeysSoundEvent(namespace, instrumentName, note, variant);
        }
    }

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUNDS.computeIfAbsent(YwzjMidi.MOD_ID, k -> DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, YwzjMidi.MOD_ID)).register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(YwzjMidi.MOD_ID, name)));
    }

    private static void registerKeysSoundEvent(String namespace, String instrumentName, int note, String variant) {
        String notation = MidiUtils.noteToNotation(note);
        String soundName = instrumentName + "_" + notation;
        if (variant != null && variant.length() > 0) {
            soundName += "_" + variant;
        }
        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(new ResourceLocation(namespace, soundName));
        LinkedHashMap<String, SoundEvent> map = INSTRUMENT_WITH_SOUNDS.getOrDefault(instrumentName, new LinkedHashMap<>());
        map.put(soundName, soundEvent);
        INSTRUMENT_WITH_SOUNDS.put(instrumentName, map);
        DeferredRegister<SoundEvent> soundDeferredRegister = SOUNDS.computeIfAbsent(namespace, k -> DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, namespace));
        soundDeferredRegister.register(soundName, () -> soundEvent);
    }

    public static void register(IEventBus eventBus, String namespace) {
        if (SOUNDS.get(namespace) != null) {
            SOUNDS.get(namespace).register(eventBus);
        }
    }

}
