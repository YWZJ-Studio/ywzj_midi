package org.ywzj.midi.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.ywzj.midi.gui.ScreenManager;
import org.ywzj.midi.gui.waterfall.WaterfallPlayer;
import org.ywzj.midi.gui.widget.*;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.instrument.receiver.MidiReceiver;
import org.ywzj.midi.storage.MidiFiles;
import org.ywzj.midi.util.ComponentUtils;
import org.ywzj.midi.util.MathUtils;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public abstract class MidiInstrumentScreen extends Screen {

    private boolean firstRender = true;
    protected final Instrument instrument;
    public Vec3 pos;
    protected MidiDevice.Info[] midiDeviceInfo;
    private boolean isConnected = false;
    public MidiReceiver receiver;
    protected WaterfallPlayer midiPlayer;
    protected ValueSlider progressBar;
    protected CommonButton deviceButton;
    protected CommonButton waterfallButton;
    protected SelectableButton.LinkedSelections<MidiDevice.Info> deviceSelections;
    protected SelectableButton<MidiDevice.Info> deviceSelectButton;
    protected CommonButton midPlayButton;
    protected List<SelectionList.Selection<Path>> midSelections = new ArrayList<>();
    protected SelectionListButton<Path> midSelectButton;
    protected List<SelectionList.Selection<Integer>> variantSelections = new ArrayList<>();
    protected SelectionListButton<Integer> variantSelectButton;
    protected boolean needSave = false;

    public MidiInstrumentScreen(Instrument instrument, Vec3 pos, Component titleIn) {
        super(titleIn);
        this.instrument = instrument;
        this.pos = pos;
        this.midiPlayer = new WaterfallPlayer(this);
        this.receiver = instrument.receiver(Minecraft.getInstance().player, pos);
    }

    public void closeMidiReceiver() {
        if (receiver != null) {
            receiver.close();
        }
    }

    @Override
    protected void init() {
        midiDeviceInfo = MidiSystem.getMidiDeviceInfo();
        List<Path> midPaths = MidiFiles.getMids();
        List<SelectableButton.Selection<MidiDevice.Info>> midiDeviceSelections = new ArrayList<>();
        Arrays.stream(midiDeviceInfo).forEach(info -> midiDeviceSelections.add(new SelectableButton.Selection<>(info, info.getName() + " " + info.getDescription())));
        midSelections.clear();
        midPaths.forEach(midPath -> midSelections.add(new SelectionList.Selection<>(midPath, midPath.getFileName().toString())));
        if (firstRender) {
            firstRender = false;
            deviceSelections = new SelectableButton.LinkedSelections<>(midiDeviceSelections);
            midSelectButton = new SelectionListButton<>(width/2 + 150, height/2 - 80, -1, 20, ComponentUtils.translatable("ui.ywzj_midi.select_mid"), midSelections, this, ComponentUtils.translatable("info.ywzj_midi.suggestion_mid"));
            instrument.getAllVariants().forEach(v -> variantSelections.add(new SelectionList.Selection<>(v.getIndex(), v.getName())));
            variantSelectButton = new SelectionListButton<>(width/2 + 150, height/2 - 20, -1, 20, ComponentUtils.translatable("ui.ywzj_midi.select"), variantSelections, this, ComponentUtils.EMPTY);
            variantSelectButton.setValue(0);
            waterfallButton = new CommonButton(width/2 + 120, height/2 + 10, 60, 20,
                    ComponentUtils.translatable("ui.ywzj_midi.waterfall"), (button) -> ScreenManager.openWaterfallScreen(midiPlayer, MidiInstrumentScreen.this));
            progressBar = new ValueSlider(width/2 - 100, height/2 + 60, 200, 20,
                    ComponentUtils.translatable("ui.ywzj_midi.progress").getString(), 0, 100);
            midiPlayer.setProgressBar(progressBar);
        } else {
            deviceSelections.update(midiDeviceSelections);
            midSelectButton.updatePos(width/2 + 150, height/2 - 80);
            midSelectButton.updateSelections(midSelections);
            variantSelectButton.updatePos(width/2 + 150, height/2 - 20);
            progressBar.updatePos(width/2 - 100, height/2 + 60);
            waterfallButton.updatePos(width/2 + 120, height/2 + 10);
        }
        deviceButton = new CommonButton(width/2 - 50, height/2, 100, 20, ComponentUtils.translatable("ui.ywzj_midi.open_midi_device"), (button) -> {
            if (isConnected) {
                receiver.closeDevice();
                isConnected = false;
                button.setMessage(ComponentUtils.translatable("ui.ywzj_midi.open_midi_device"));
                Minecraft.getInstance().player.sendSystemMessage(ComponentUtils.translatable("info.ywzj_midi.midi_disconnected"));
            } else {
                if (midiDeviceInfo.length == 0) {
                    return;
                }
                if (receiver.initDevice(deviceSelectButton.getValue())) {
                    isConnected = true;
                    button.setMessage(ComponentUtils.translatable("ui.ywzj_midi.midi_connected"));
                    Minecraft.getInstance().player.sendSystemMessage(ComponentUtils.translatable("info.ywzj_midi.connect_succeed"));
                } else {
                    Minecraft.getInstance().player.sendSystemMessage(ComponentUtils.translatable("info.ywzj_midi.connect_failed"));
                }
            }
        });
        deviceSelectButton = new SelectableButton<>(width/2, height/2 + 30, -1, 20, deviceSelections);
        String info = midiPlayer.isPlaying() ? "ui.ywzj_midi.playing" : "ui.ywzj_midi.open_mid_file";
        midPlayButton = new CommonButton(width/2 + 150 - 40, height/2 - 50, 80, 20, ComponentUtils.translatable(info), (button) -> {
            if (midiPlayer.isPlaying()) {
                midiPlayer.stopPlay();
                button.setMessage(ComponentUtils.translatable("ui.ywzj_midi.open_mid_file"));
            } else {
                if (midSelectButton.getValue() == null) {
                    return;
                }
                midiPlayer.open(midSelectButton.getValue().toFile());
                midiPlayer.getChannels().forEach(channel -> {
                    channel.use(instrument);
                    channel.volume(1);
                    channel.registerReceiver(receiver);
                });
                midiPlayer.play(0);
                button.setMessage(ComponentUtils.translatable("ui.ywzj_midi.playing"));
            }
        });
        addRenderableWidget(deviceButton);
        addRenderableWidget(deviceSelectButton);
        addRenderableWidget(midPlayButton);
        addRenderableWidget(midSelectButton);
        addRenderableWidget(variantSelectButton);
        addRenderableWidget(progressBar);
        addRenderableWidget(waterfallButton);
    }

    public void callbackPlayButton() {
        midPlayButton.setMessage(ComponentUtils.translatable("ui.ywzj_midi.open_mid_file"));
    }

    @Override
    public void onClose() {
        super.onClose();
        if (!needSave && receiver != null) {
            receiver.close();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean keyPressed = super.keyPressed(keyCode, scanCode, modifiers);
        if (keyPressed || getFocused() != null)
            return keyPressed;
        InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
        if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            this.onClose();
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        if (instrument.isPortable()) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (!player.isAlive() || MathUtils.distance(player.getX(), player.getY(), player.getZ(), pos.x, pos.y, pos.z) > 3) {
                this.onClose();
            }
        }
    }

}
