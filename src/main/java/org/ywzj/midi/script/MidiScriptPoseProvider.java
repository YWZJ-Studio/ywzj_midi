package org.ywzj.midi.script;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import org.mozillaa.javascript.*;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.all.AllInstruments;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.pose.PoseManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MidiScriptPoseProvider {

    private static final MidiScriptPoseProvider INSTANCE = new MidiScriptPoseProvider();

    private final ConcurrentHashMap<ResourceLocation, Scriptable> instrumentScopes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ResourceLocation, Function> playPoseFunctions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ResourceLocation, Function> holdPoseFunctions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ResourceLocation, Function> modelPoseFunctions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<ResourceLocation, String> scriptOverrides = new ConcurrentHashMap<>();

    private MidiScriptManager scriptManager;
    private volatile boolean initialized = false;

    private MidiScriptPoseProvider() {}

    public static void setScriptOverride(ResourceLocation instrumentId, String scriptName) {
        scriptOverrides.put(instrumentId, scriptName);
    }

    private static String resolveScriptName(ResourceLocation instrumentId) {
        return scriptOverrides.getOrDefault(instrumentId, instrumentId.getPath());
    }

    private static ResourceLocation resolveScriptLocation(ResourceLocation instrumentId, String scriptName) {
        if (scriptName.indexOf(':') >= 0) {
            ResourceLocation explicit = ResourceLocation.tryParse(scriptName);
            if (explicit != null) return explicit;
        }
        return new ResourceLocation(instrumentId.getNamespace(), scriptName);
    }

    public static MidiScriptPoseProvider getInstance() {
        return INSTANCE;
    }

    public void setScriptManager(MidiScriptManager manager) {
        this.scriptManager = manager;
    }

    public boolean isInitialized() {
        return initialized && scriptManager != null;
    }

    /**
     * Try to lazy-initialize from the script manager. Called on first pose computation or after reload.
     */
    public synchronized void ensureInitialized() {
        if (initialized && !instrumentScopes.isEmpty()) {
            return;
        }
        if (scriptManager == null) {
            return;
        }
        initializeAll();
    }

    private void initializeAll() {
        instrumentScopes.clear();
        playPoseFunctions.clear();
        holdPoseFunctions.clear();
        modelPoseFunctions.clear();

        MidiScriptContextFactory factory = MidiScriptContextFactory.get();
        for (Instrument instrument : AllInstruments.getInstruments()) {
            String scriptName = resolveScriptName(instrument.getInstrumentId());
            ResourceLocation location = resolveScriptLocation(instrument.getInstrumentId(), scriptName);
            scriptManager.getScript(location).ifPresentOrElse(
                script -> initInstrument(factory, instrument.getInstrumentId(), script),
                () -> YwzjMidi.LOGGER.debug("No JS script found for instrument: {}", scriptName)
            );
        }
        initialized = true;
    }

    private void initInstrument(MidiScriptContextFactory factory, ResourceLocation id, Script compiledScript) {
        try (Context cx = factory.enterContext()) {
            Scriptable scope = factory.createScope(cx);

            BaseFunction createPoseBuilderFunc = new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                    MidiPoseBuilder builder = new MidiPoseBuilder();
                    return Context.javaToJS(builder, scope);
                }
            };
            scope.put("createPoseBuilder", scope, createPoseBuilderFunc);

            compiledScript.exec(cx, scope);

            Object playFn = scope.get("getPlayPose", scope);
            if (playFn instanceof Function f) {
                playPoseFunctions.put(id, f);
            }

            Object holdFn = scope.get("getHoldPose", scope);
            if (holdFn instanceof Function f) {
                holdPoseFunctions.put(id, f);
            }

            Object modelFn = scope.get("getModelPose", scope);
            if (modelFn instanceof Function f) {
                modelPoseFunctions.put(id, f);
            }

            instrumentScopes.put(id, scope);
        } catch (Exception e) {
            YwzjMidi.LOGGER.error("Failed to initialize script for instrument: {}", id, e);
        }
    }

    public synchronized void invalidate() {
        instrumentScopes.clear();
        playPoseFunctions.clear();
        holdPoseFunctions.clear();
        modelPoseFunctions.clear();
        initialized = false;
    }

    public PoseManager.PlayPose computePlayPose(Instrument instrument, MidiPoseScriptContext context) {
        return computePose(instrument.getInstrumentId(), playPoseFunctions, context);
    }

    public PoseManager.PlayPose computePlayPose(ResourceLocation instrumentId, MidiPoseScriptContext context) {
        return computePose(instrumentId, playPoseFunctions, context);
    }

    public PoseManager.PlayPose computeHoldPose(Instrument instrument, InteractionHand hand) {
        MidiPoseScriptContext context = new MidiPoseScriptContext();
        context.setInstrumentId(instrument.getInstrumentId());
        context.setHand(hand == InteractionHand.MAIN_HAND ? "main_hand" : "off_hand");
        return computePose(instrument.getInstrumentId(), holdPoseFunctions, context);
    }

    public Map<String, MidiPoseBuilder.BoneTransform> computeModelPose(Instrument instrument, MidiPoseScriptContext context) {
        if (!isInitialized()) {
            ensureInitialized();
        } else {
            ensureScript(instrument.getInstrumentId());
        }

        Function fn = modelPoseFunctions.get(instrument.getInstrumentId());
        Scriptable scope = instrumentScopes.get(instrument.getInstrumentId());
        if (fn == null || scope == null) {
            return null;
        }

        try (var cx = MidiScriptContextFactory.get().enterContext()) {
            Object result = fn.call(cx, scope, scope, new Object[]{context});
            if (result instanceof Wrapper wrapper) {
                result = wrapper.unwrap();
            }
            if (result instanceof MidiPoseBuilder builder) {
                return builder.getBoneTransforms();
            }
        } catch (Exception e) {
            YwzjMidi.LOGGER.warn("Model pose script error for instrument {}: {}", instrument.getInstrumentId(), e.getMessage());
        }
        return null;
    }

    /**
     * Load a script by name, even if it's not a registered instrument (e.g. conductor).
     */
    public synchronized void ensureScript(ResourceLocation id) {
        if (playPoseFunctions.containsKey(id)) {
            return;
        }
        if (scriptManager == null) {
            return;
        }
        ResourceLocation location = resolveScriptLocation(id, resolveScriptName(id));
        scriptManager.getScript(location).ifPresent(script -> {
            initInstrument(MidiScriptContextFactory.get(), id, script);
        });
    }

    private PoseManager.PlayPose computePose(ResourceLocation instrumentId,
                                              Map<ResourceLocation, Function> functionMap,
                                              MidiPoseScriptContext context) {
        if (!isInitialized()) {
            ensureInitialized();
        }

        Function fn = functionMap.get(instrumentId);
        Scriptable scope = instrumentScopes.get(instrumentId);
        if (fn == null || scope == null) {
            return null;
        }

        try (Context cx = MidiScriptContextFactory.get().enterContext()) {
            Object result = fn.call(cx, scope, scope, new Object[]{context});
            if (result instanceof Wrapper wrapper) {
                result = wrapper.unwrap();
            }
            if (result instanceof MidiPoseBuilder builder) {
                return builder.build();
            }
        } catch (Exception e) {
            YwzjMidi.LOGGER.warn("Script error for instrument {}: {}", instrumentId, e.getMessage());
        }
        return null;
    }

}
