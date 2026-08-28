package org.ywzj.midi.script;

import org.mozillaa.javascript.*;

public final class MidiScriptContextFactory extends ContextFactory {

    private static final MidiScriptContextFactory INSTANCE;
    static {
        ClassShutter shutter = className ->
                className.startsWith("org.ywzj.midi")
                        || className.startsWith("java.lang.")
                        || className.startsWith("java.util.");
        INSTANCE = new MidiScriptContextFactory(null, shutter);
    }
    private final Scriptable globalScope;
    private final WrapFactory wrapFactory;
    private final ClassShutter classShutter;

    public MidiScriptContextFactory(WrapFactory wrapFactory, ClassShutter classShutter) {
        this.wrapFactory = wrapFactory;
        this.classShutter = classShutter;
        try (Context ctx = this.enterContext()) {
            Scriptable scope = ctx.initSafeStandardObjects();
            if (scope instanceof ScriptableObject so) {
                so.sealObject();
            }
            this.globalScope = scope;
        }
    }

    public static MidiScriptContextFactory get() {
        return INSTANCE;
    }

    public Scriptable createScope(Context ctx) {
        Scriptable scope = ctx.newObject(globalScope);
        scope.setPrototype(globalScope);
        scope.setParentScope(null);
        return scope;
    }

    @Override
    public Context makeContext() {
        Context ctx = super.makeContext();
        ctx.setInterpretedMode(false);
        if (wrapFactory != null) {
            ctx.setWrapFactory(wrapFactory);
        }
        if (classShutter != null) {
            ctx.setClassShutter(classShutter);
        }
        return ctx;
    }

}
