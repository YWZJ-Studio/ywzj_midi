package org.ywzj.midi.gui.widget;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.ywzj.midi.util.ComponentUtils;

import java.util.List;

public class SelectionList<T> extends ObjectSelectionList<SelectionList<T>.SelectionEntry> {

    public final List<Selection<T>> values;
    private final Screen screen;
    private final OnDoubleClick onDoubleClick;

    public SelectionList(List<Selection<T>> values, Screen screen, int width, int height, int y0, int y1, OnDoubleClick onDoubleClick) {
        super(Minecraft.getInstance(), width, y1 - y0, y0, 20);
        this.screen = screen;
        this.values = values;
        this.values.forEach(value -> {
            SelectionEntry selectionEntry = new SelectionEntry(value);
            this.addEntry(selectionEntry);
        });
        if (this.getSelected() != null) {
            this.centerScrollOn(this.getSelected());
        }
        this.onDoubleClick = onDoubleClick;
    }

    public void update(List<Selection<T>> values, int width, int height, int y0, int y1) {
        this.updateSizeAndPosition(width, y1 - y0, y0);
        if (values != null) {
            Selection<T> selectedValue = null;
            if (this.getSelected() != null) {
                selectedValue = this.getSelected().getValue();
            }
            this.clearEntries();
            for (Selection<T> selection : values) {
                SelectionEntry selectionEntry = new SelectionEntry(selection);
                this.addEntry(selectionEntry);
                if (selectedValue != null && selectedValue.value.equals(selectionEntry.value.value)) {
                    this.setSelected(selectionEntry);
                }
            }
        }
    }

    public void findAndSelect(T value) {
        for (SelectionEntry selectionEntry : children()) {
            if (selectionEntry.getValue().value != null) {
                if (selectionEntry.getValue().value.equals(value)) {
                    this.setSelected(selectionEntry);
                }
            }
        }
    }

    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 40;
    }

    public interface OnDoubleClick {
        void onDoubleClick();
    }

    public class SelectionEntry extends ObjectSelectionList.Entry<SelectionEntry> {

        private final Selection<T> value;
        private long lastClickTime;

        public SelectionEntry(Selection<T> value) {
            this.value = value;
        }

        public Selection<T> getValue() {
            return value;
        }

        public void render(GuiGraphics p_282025_, int p_283548_, int p_282485_, int p_282109_, int p_283314_, int p_283303_, int p_281337_, int p_283527_, boolean p_283295_, float p_282169_) {
            p_282025_.drawCenteredString(Minecraft.getInstance().font, this.value.name, SelectionList.this.screen.width / 2, p_282485_ + 4, 16777215);
        }

        public boolean mouseClicked(double p_96122_, double p_96123_, int p_96124_) {
            if (p_96124_ == 0) {
                this.select();
                if (Util.getMillis() - this.lastClickTime < 250L) {
                    SelectionList.this.onDoubleClick.onDoubleClick();
                }
                this.lastClickTime = Util.getMillis();
                return true;
            } else {
                this.lastClickTime = Util.getMillis();
                return false;
            }
        }

        void select() {
            SelectionList.this.setSelected(this);
        }

        public Component getNarration() {
            return ComponentUtils.translatable("narrator.ywzj_midi.select");
        }

    }

    public static class Selection<T> {

        public T value;
        public String name;

        public Selection(T value, String name) {
            this.value = value;
            this.name = name;
        }

    }

}
