package org.ywzj.midi.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.ywzj.midi.all.AllInstruments;
import org.ywzj.midi.entity.FakePlayerEntity;
import org.ywzj.midi.gui.ScreenManager;
import org.ywzj.midi.gui.widget.*;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.instrument.player.ConductorMidiPlayer;
import org.ywzj.midi.instrument.player.MidiPlayer;
import org.ywzj.midi.storage.ConductorConfig;
import org.ywzj.midi.storage.MidiFiles;
import org.ywzj.midi.util.ComponentUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ConductorScreen extends Screen {

    private boolean firstRender = true;
    private Player conductor;
    private final static List<SelectionList.Selection<Instrument>> INSTRUMENT_SELECTIONS = new ArrayList<>();
    private ConductorMidiPlayer conductorMidiPlayer;
    protected List<SelectionList.Selection<Path>> midSelections = new ArrayList<>();
    protected SelectionListButton<Path> midSelectButton;
    private Path openedMid;
    private CommonButton playButton;
    public ValueSlider progressBar;
    private CommonButton conductorButton;
    private CommonButton waterfallButton;
    public boolean needConductor = true;
    public SelectionsButton<String> channelFilterButton;
    private final List<CommonButton> instrumentNameButtons = new ArrayList<>();
    private final List<SelectionListButton<Instrument>> instrumentSelectButtons = new ArrayList<>();
    private final List<SelectionsButton<LivingEntity>> playersSelectButtons = new ArrayList<>();
    public final List<ValueSlider> volumeSliders = new ArrayList<>();
    public final HashMap<ValueSlider, MidiPlayer.Channel> volumeSliderToChannel = new HashMap<>();
    private int pageNo = 1;
    private int pageSize = 8;

    public ConductorScreen(Component titleIn) {
        super(titleIn);
    }

    @Override
    protected void init() {
        INSTRUMENT_SELECTIONS.clear();
        AllInstruments.getInstruments().stream()
                .filter(instrument -> !"aa775".equals(instrument.getName()))
                .forEach(instrument -> INSTRUMENT_SELECTIONS.add(new SelectionList.Selection<>(instrument, instrument.getName())));
        INSTRUMENT_SELECTIONS.add(new SelectionList.Selection<>(null, "NA"));
        List<SelectionList.Selection<LivingEntity>> players = new ArrayList<>();
        List<Path> midPaths = MidiFiles.getMids();
        conductor = Minecraft.getInstance().player;
        conductor.level().getEntities(conductor,
                new AABB(conductor.getX()-32.0D, conductor.getY()-32.0D, conductor.getZ()-32.0D, conductor.getX()+32.0D, conductor.getY()+32.0D, conductor.getZ()+32.0D))
                .stream()
                .filter(entity -> entity instanceof Player || entity instanceof FakePlayerEntity)
                .forEach(player -> players.add(new SelectionList.Selection(player, player.getName().getString())));
        players.add(new SelectionList.Selection<>(conductor, conductor.getName().getString()));
        midSelections.clear();
        midPaths.forEach(midPath -> midSelections.add(new SelectionList.Selection<>(midPath, midPath.getFileName().toString())));
        if (firstRender) {
            firstRender = false;
            conductorMidiPlayer = new ConductorMidiPlayer(this);
            midSelectButton = new SelectionListButton<>(width/2 + 130, height/2 - 80, -1, 20, ComponentUtils.translatable("ui.ywzj_midi.select_mid"), midSelections, this, ComponentUtils.translatable("info.ywzj_midi.suggestion_mid"));
            progressBar = new ValueSlider(width/2 + 130 - 80, height/2 + 10, 160, 20, ComponentUtils.translatable("ui.ywzj_midi.progress").getString(), 0, 100);
            conductorMidiPlayer.setProgressBar(progressBar);
            waterfallButton = new CommonButton(width/2 + 130 - 40, height/2 + 100, 60, 20,
                    ComponentUtils.literal("瀑布"), (button) -> ScreenManager.openWaterfallScreen(conductorMidiPlayer, ConductorScreen.this));
            conductorButton = new CommonButton(width/2 + 130 - 40, height/2 + 40, 80, 20, ComponentUtils.translatable(needConductor ? "ui.ywzj_midi.conductor_on" : "ui.ywzj_midi.conductor_off"), (button) -> {
                needConductor = !needConductor;
                button.setMessage(ComponentUtils.translatable(needConductor ? "ui.ywzj_midi.conductor_on" : "ui.ywzj_midi.conductor_off"));
            });
        } else {
            midSelectButton.updatePos(width/2 + 130, height/2 - 80);
            midSelectButton.updateSelections(midSelections);
            progressBar.updatePos(width/2 + 130 - 80, height/2 + 10);
            waterfallButton.updatePos(width/2 + 130 - 40, height/2 + 100);
            conductorButton.updatePos(width/2 + 130 - 40, height/2 + 40);
            updateParts();
            updateChannelFilterButton();
        }
        CommonButton midOpenButton = new CommonButton(width / 2 + 130 - 40, height / 2 - 50, 80, 20, ComponentUtils.translatable("ui.ywzj_midi.open_mid_file"), (button) -> {
            if (midSelectButton.getValue() == null) {
                return;
            }
            clearParts();
            if (conductorMidiPlayer.open(midSelectButton.getValue().toFile())) {
                ConductorConfig.MidConfig midConfig = ConductorConfig.get(midSelectButton.getValue().getFileName().toString());
                parseParts(players, pageSize, midConfig);
                parseChannelFilterButton(midConfig);
                displayPage(1, pageSize);
                pageNo = 1;
                openedMid = midSelectButton.getValue();
            }
        });
        String info = conductorMidiPlayer.isPlaying() ? "ui.ywzj_midi.playing" : "ui.ywzj_midi.to_play";
        playButton = new CommonButton(width/2 + 130 - 40, height/2 - 20, 80, 20, ComponentUtils.translatable(info), (button) -> {
            if (midSelectButton.getValue() == null || !midSelectButton.getValue().equals(openedMid)) {
                return;
            }
            if (conductorMidiPlayer.isPlaying()) {
                conductorMidiPlayer.stopPlay();
                button.setMessage(ComponentUtils.translatable("ui.ywzj_midi.to_play"));
            } else {
                List<ConductorMidiPlayer.Channel> channels = conductorMidiPlayer.getChannels();
                List<ConductorConfig.ChannelUnit> units = new ArrayList<>();
                for (int index = 0; index < channels.size(); index++) {
                    channels.get(index).use(instrumentSelectButtons.get(index).getValue());
                    volumeSliderToChannel.put(volumeSliders.get(index), channels.get(index));
                    channels.get(index).volume((float) volumeSliders.get(index).value / volumeSliders.get(index).maxValue);
                    channels.get(index).clearReceiver();
                    for (LivingEntity player : playersSelectButtons.get(index).getValues()) {
                        channels.get(index).registerReceiver(player);
                    }
                    var selInstrument = instrumentSelectButtons.get(index).getValue();
                    units.add(new ConductorConfig.ChannelUnit(selInstrument == null ? null : selInstrument.getInstrumentId().toString(),
                            volumeSliders.get(index).value,
                            playersSelectButtons.get(index).getValues().stream()
                                    .map(player -> player.getName().getString())
                                    .collect(Collectors.toList()))
                    );
                }
                ConductorConfig.MidConfig midConfig = new ConductorConfig.MidConfig();
                midConfig.setChannelUnits(units);
                if (channelFilterButton != null) {
                    midConfig.setChannelFilter(channelFilterButton.getValues());
                }
                ConductorConfig.save(midSelectButton.getValue().getFileName().toString(), midConfig);
                conductorMidiPlayer.play(5);
                button.setMessage(ComponentUtils.translatable("ui.ywzj_midi.playing"));
            }
        });
        CommonButton prePageButton = new CommonButton(width / 2 + 20, height / 2 + 90, 20, 20, ComponentUtils.literal("<"), (button) -> {
            if (pageNo - 1 < 1) {
                return;
            }
            pageNo -= 1;
            displayPage(pageNo, pageSize);
        });
        CommonButton nextPageButton = new CommonButton(width / 2 + 45, height / 2 + 90, 20, 20, ComponentUtils.literal(">"), (button) -> {
            if (pageNo * pageSize >= conductorMidiPlayer.getChannels().size()) {
                return;
            }
            pageNo += 1;
            displayPage(pageNo, pageSize);
        });
        addRenderableWidget(midSelectButton);
        addRenderableWidget(midOpenButton);
        addRenderableWidget(playButton);
        addRenderableWidget(progressBar);
        addRenderableWidget(waterfallButton);
        addRenderableWidget(conductorButton);
        addRenderableWidget(prePageButton);
        addRenderableWidget(nextPageButton);
    }

    private void parseParts(List<SelectionList.Selection<LivingEntity>> players, int pageSize, ConductorConfig.MidConfig midConfig) {
        int x = width/2 - 250;
        int y = height/2 - 120;
        List<ConductorMidiPlayer.Channel> channels = conductorMidiPlayer.getChannels();
        for (int index = 0; index < channels.size(); index++) {
            if (index % pageSize == 0) {
                x = width/2 - 250;
                y = height/2 - 120;
            }
            ConductorMidiPlayer.Channel channel = channels.get(index);
            CommonButton instrumentNameButton = new CommonButton(x + 40, y, 80, 20, ComponentUtils.literal(channel.instrumentName), (button) -> {});
            instrumentNameButton.active = false;
            instrumentNameButtons.add(instrumentNameButton);
            SelectionListButton<Instrument> instrumentSelectButton = new SelectionListButton<>(x + 40 + 105, y, 40, 20, ComponentUtils.translatable("ui.ywzj_midi.select_instrument"), INSTRUMENT_SELECTIONS, this, ComponentUtils.EMPTY);
            instrumentSelectButtons.add(instrumentSelectButton);
            SelectionsButton<LivingEntity> playerSelectionsButton = new SelectionsButton<>(x + 40 + 85 + 45, y, 40, 20, ComponentUtils.translatable("ui.ywzj_midi.select_players"), players, null, conductor,this);
            playersSelectButtons.add(playerSelectionsButton);
            ValueSlider volumeSlider = new ValueSlider(x + 40 + 85 + 45 + 45, y, 50, 20, "Vol", 127, 127);
            volumeSliders.add(volumeSlider);
            if (midConfig != null && midConfig.getChannelUnits().size() > index) {
                ConductorConfig.ChannelUnit unit = midConfig.getChannelUnits().get(index);
                instrumentSelectButton.setValue(unit.getInstrumentIdAsResourceLocation() == null ? null : AllInstruments.fromId(unit.getInstrumentIdAsResourceLocation()));
                List<LivingEntity> targetPlayers = players.stream()
                            .filter(playerSelection -> unit.getPlayerNames().contains(playerSelection.name))
                            .map(playerSelection -> playerSelection.value)
                            .collect(Collectors.toList());
                if (targetPlayers.size() > 0) {
                    playerSelectionsButton.setValues(targetPlayers);
                }
                volumeSlider.updateValue((double) unit.getVol() / 127);
            }
            instrumentNameButton.visible = false;
            instrumentSelectButton.visible = false;
            playerSelectionsButton.visible = false;
            volumeSlider.visible = false;
            addRenderableWidget(instrumentNameButton);
            addRenderableWidget(instrumentSelectButton);
            addRenderableWidget(playerSelectionsButton);
            addRenderableWidget(volumeSlider);
            y += 30;
        }
    }

    private void updateParts() {
        int x = width/2 - 250;
        int y = height/2 - 120;
        List<ConductorMidiPlayer.Channel> channels = conductorMidiPlayer.getChannels();
        for (int index = 0; index < channels.size(); index++) {
            if (index % pageSize == 0) {
                x = width/2 - 250;
                y = height/2 - 120;
            }
            instrumentNameButtons.get(index).setX(x + 40);
            instrumentNameButtons.get(index).setY(y);
            instrumentSelectButtons.get(index).setX(x + 40 + 85);
            instrumentSelectButtons.get(index).setY(y);
            playersSelectButtons.get(index).setX(x + 40 + 85 + 45);
            playersSelectButtons.get(index).setY(y);
            volumeSliders.get(index).setX(x + 40 + 85 + 45 + 45);
            volumeSliders.get(index).setY(y);
            addRenderableWidget(instrumentNameButtons.get(index));
            addRenderableWidget(instrumentSelectButtons.get(index));
            addRenderableWidget(playersSelectButtons.get(index));
            addRenderableWidget(volumeSliders.get(index));
            y += 30;
        }
        progressBar.setX(width/2 + 130 - 80);
        progressBar.setY(height/2 + 10);
    }

    private void clearParts() {
        instrumentNameButtons.forEach(this::removeWidget);
        instrumentSelectButtons.forEach(this::removeWidget);
        playersSelectButtons.forEach(this::removeWidget);
        volumeSliders.forEach(this::removeWidget);
        instrumentNameButtons.clear();
        instrumentSelectButtons.clear();
        playersSelectButtons.clear();
        volumeSliders.clear();
    }

    private void displayPage(int pageNo, int pageSize) {
        List<ConductorMidiPlayer.Channel> channels = conductorMidiPlayer.getChannels();
        if (pageNo < 1 || (pageNo - 1) * pageSize >= channels.size() || channels.size() == 0) {
            return;
        }
        instrumentNameButtons.forEach(b -> b.visible = false);
        instrumentSelectButtons.forEach(b -> b.visible = false);
        playersSelectButtons.forEach(b -> b.visible = false);
        volumeSliders.forEach(b -> b.visible = false);
        for (int index = (pageNo - 1) * pageSize; index < Math.min(pageNo * pageSize, channels.size()); index++) {
            instrumentNameButtons.get(index).visible = true;
            instrumentSelectButtons.get(index).visible = true;
            playersSelectButtons.get(index).visible = true;
            volumeSliders.get(index).visible = true;
        }
    }

    private void parseChannelFilterButton(ConductorConfig.MidConfig midConfig) {
        if (channelFilterButton != null) {
            removeWidget(channelFilterButton);
        }
        List<ConductorMidiPlayer.Channel> channels = conductorMidiPlayer.getChannels();
        List<SelectionList.Selection<String>> parts = new ArrayList<>();
        channels.forEach(channel -> parts.add(new SelectionList.Selection<>(channel.instrumentName, channel.instrumentName)));
        channelFilterButton = new SelectionsButton<>(width/2 + 130 - 40, height/2 + 70, 80, 20, ComponentUtils.translatable("ui.ywzj_midi.channel_filter"), parts, midConfig == null ? null : midConfig.getChannelFilter(), this);
        addRenderableWidget(channelFilterButton);
    }

    private void updateChannelFilterButton() {
        if (channelFilterButton != null) {
            channelFilterButton.updatePos(width/2 + 130 - 40, height/2 + 70);
            addRenderableWidget(channelFilterButton);
        }
    }

    public void callbackPlayButton() {
        playButton.setMessage(ComponentUtils.translatable("ui.ywzj_midi.to_play"));
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

}
