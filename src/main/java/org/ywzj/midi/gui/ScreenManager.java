package org.ywzj.midi.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.ywzj.midi.all.AllInstruments;
import org.ywzj.midi.blockentity.AABlockEntity;
import org.ywzj.midi.blockentity.PianoBlockEntity;
import org.ywzj.midi.blockentity.TimpaniBlockEntity;
import org.ywzj.midi.entity.FakePlayerEntity;
import org.ywzj.midi.entity.InstrumentEntity;
import org.ywzj.midi.gui.screen.*;
import org.ywzj.midi.gui.waterfall.WaterfallPlayer;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.util.ComponentUtils;
import org.ywzj.midi.util.MathUtils;
import org.ywzj.midi.util.MidiUtils;

import java.util.HashMap;
import java.util.UUID;

import static org.ywzj.midi.entity.FakePlayerEntity.DEFAULT_NAME;

@OnlyIn(Dist.CLIENT)
public class ScreenManager {

    private static final ConductorScreen CONDUCTOR_SCREEN = new ConductorScreen(ComponentUtils.literal("指挥"));
    private static final HashMap<String, MidiInstrumentScreen> instrumentScreens = new HashMap<>();
    private static final HashMap<UUID, MidiInstrumentScreen> entityInstrumentScreens = new HashMap<>();
    private static final HashMap<UUID, ServerMidiScreen> fakePlayerConductorScreens = new HashMap<>();

    public static void openBatonScreen() {
        Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(CONDUCTOR_SCREEN));
    }

    /**
     * Open screen for a block-position-based instrument (piano, timpani, etc.)
     */
    public static void openPianoScreen(BlockPos pos, Instrument instrument, PianoBlockEntity pianoBlockEntity) {
        if (!checkDistance(pos, 3)) return;
        if (pianoBlockEntity.clavichordScreen == null) {
            pianoBlockEntity.clavichordScreen = new ClavichordScreen(instrument, pos, ComponentUtils.literal("钢琴"), "c4", "b6");
        }
        Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(pianoBlockEntity.clavichordScreen));
    }

    /**
     * Generic instrument screen opener for item-based instruments.
     * Dispatches to the correct screen based on instrument type.
     */
    public static void openInstrumentScreen(Instrument instrument, Player player) {
        String name = instrument.getName();
        MidiInstrumentScreen screen = instrumentScreens.get(name);
        if (screen == null) {
            screen = createScreen(instrument, player);
            if (screen != null) {
                instrumentScreens.put(name, screen);
            }
        }
        if (screen != null) {
            final Screen finalScreen = screen;
            Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(finalScreen));
        }
    }

    /**
     * Open screen for an entity-based instrument (piano, timpani, bass drum, etc. placed in world).
     */
    public static void openInstrumentEntityScreen(Instrument instrument, InstrumentEntity entity) {
        MidiInstrumentScreen screen = entityInstrumentScreens.get(entity.getUUID());
        if (screen == null) {
            screen = createEntityScreen(instrument, entity);
            if (screen != null) {
                entityInstrumentScreens.put(entity.getUUID(), screen);
            }
        }
        if (screen != null) {
            entity.setReceiver(screen.receiver);
            screen.instrumentEntityId = entity.getId();
            final MidiInstrumentScreen finalScreen = screen;
            finalScreen.pos = entity.position();
            Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(finalScreen));
        }
    }

    private static MidiInstrumentScreen createEntityScreen(Instrument instrument, InstrumentEntity entity) {
        return createScreenByFamily(instrument, entity.blockPosition(), entity.position());
    }

    private static MidiInstrumentScreen createScreen(Instrument instrument, Player player) {
        return createScreenByFamily(instrument, player.blockPosition(), player.position());
    }

    private static MidiInstrumentScreen createScreenByFamily(Instrument instrument, BlockPos blockPos, Vec3 pos) {
        AllInstruments.Family family = instrument.getFamily();
        if (family == null) {
            return null;
        }

        int lowNote = MidiUtils.notationToNote(instrument.getKeyStart());
        String kbStart, kbEnd;
        if (lowNote >= 48) {
            kbStart = "c4"; kbEnd = "b6";
        } else if (lowNote >= 36) {
            kbStart = "c3"; kbEnd = "b5";
        } else if (lowNote >= 24) {
            kbStart = "c2"; kbEnd = "b4";
        } else if (lowNote >= 12) {
            kbStart = "c1"; kbEnd = "b3";
        } else {
            kbStart = "c0"; kbEnd = "b2";
        }

        var displayName = ComponentUtils.literal(instrument.getName());
        return switch (family) {
            case KEYBOARD, PERCUSSION ->
                new ClavichordScreen(instrument, blockPos, displayName, kbStart, kbEnd);
            case STRING ->
                new ViolScreen(instrument, pos, displayName, kbStart, kbEnd);
            case WOODWIND ->
                new WoodwindScreen(instrument, pos, displayName, kbStart, kbEnd);
            case BRASS ->
                new BrassScreen(instrument, pos, displayName, kbStart, kbEnd);
            default -> null;
        };
    }

    public static void openSpeakerScreen(BlockPos pos, MusicPlayerScreen musicPlayerScreen) {
        if (!checkDistance(pos, 2)) return;
        Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(musicPlayerScreen));
    }

    public static void openMusicPlayerScreen(MusicPlayerScreen musicPlayerScreen) {
        Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(musicPlayerScreen));
    }

    public static void openAABlockScreen(BlockPos pos, AABlockEntity aaBlockEntity) {
        if (aaBlockEntity.aaBlockScreen == null) {
            aaBlockEntity.aaBlockScreen = new AABlockScreen(pos, ComponentUtils.literal("AA775"), aaBlockEntity);
        }
        Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(aaBlockEntity.aaBlockScreen));
    }

    public static void openTimpaniScreen(BlockPos pos, TimpaniBlockEntity timpaniBlockEntity) {
        if (timpaniBlockEntity.timpaniScreen == null) {
            timpaniBlockEntity.timpaniScreen = new TimpaniScreen(pos, timpaniBlockEntity);
        }
        Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(timpaniBlockEntity.timpaniScreen));
    }

    public static void openFakePlayerScreen(FakePlayerEntity fakePlayerEntity) {
        FakePlayerScreen fakePlayerScreen = new FakePlayerScreen(ComponentUtils.literal(DEFAULT_NAME), fakePlayerEntity);
        Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(fakePlayerScreen));
    }

    public static void openFakePlayerConductorScreen(FakePlayerEntity fakePlayerEntity) {
        ServerMidiScreen serverMidiScreen = fakePlayerConductorScreens.computeIfAbsent(
                fakePlayerEntity.getUUID(), k -> new ServerMidiScreen(ComponentUtils.literal("指挥"), fakePlayerEntity));
        Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(serverMidiScreen));
    }

    public static void openWaterfallScreen(WaterfallPlayer waterfallPlayer, Screen parent) {
        WaterfallScreen waterfallScreen = new WaterfallScreen(waterfallPlayer, parent, ComponentUtils.literal("瀑布"));
        Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(waterfallScreen));
    }

    private static boolean checkDistance(BlockPos pos, int distance) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (MathUtils.distance(player.getX(), player.getY(), player.getZ(), pos.getX(), pos.getY(), pos.getZ()) > distance) {
                player.sendSystemMessage(ComponentUtils.translatable("info.ywzj_midi.warn_2"));
                return false;
            }
        }
        return true;
    }

}
