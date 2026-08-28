package org.ywzj.midi.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.ywzj.midi.gui.waterfall.WaterfallNote;
import org.ywzj.midi.gui.waterfall.WaterfallPlayer;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class WaterfallScreen extends Screen {

    private static final ResourceLocation KEYBOARD_TEX =
            new ResourceLocation("ywzj_midi", "textures/ui/keyboard.png");
    private static final int TEX_W = 1822;
    private static final int TEX_H = 154;

    private static final int KEYBOARD_WIDTH = 40;
    private static final int KEY_LINE_WIDTH = 2;

    private static final int MIN_NOTE = 12;
    private static final int MAX_NOTE = 108;
    private static final int KEY_COUNT = MAX_NOTE - MIN_NOTE + 1; // 97

    private static final long LOOKAHEAD_TICKS = 1920; // ~4 beats at 480 PPQ

    private static final int[] CHANNEL_COLORS = {
            0xFF4A90D9, // blue
            0xFF50B86C, // green
            0xFFE8843C, // orange
            0xFF9B59B6, // purple
            0xFFE74C8B, // pink
            0xFF1ABC9C, // teal
            0xFFF1C40F, // yellow
            0xFFE67E22, // dark orange
            0xFF3498DB, // light blue
            0xFF2ECC71, // emerald
            0xFFE91E63, // deep pink
            0xFF00BCD4, // cyan
            0xFFFF9800, // amber
            0xFF8BC34A, // light green
            0xFF795548, // brown
            0xFF607D8B, // blue grey
    };

    private final WaterfallPlayer waterfallPlayer;
    private final Screen parent;

    public WaterfallScreen(WaterfallPlayer waterfallPlayer, Screen parent, Component title) {
        super(title);
        this.waterfallPlayer = waterfallPlayer;
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int screenHeight = this.height;
        int screenWidth = this.width;
        float keyHeight = (float) screenHeight / KEY_COUNT;
        int waterfallLeft = KEYBOARD_WIDTH + KEY_LINE_WIDTH;
        int waterfallWidth = screenWidth - waterfallLeft;

        if (waterfallWidth <= 0) return;

        float pxPerTick = waterfallWidth / (float) LOOKAHEAD_TICKS;
        long currentTick = waterfallPlayer.getCurrentTick();

        // Interpolate tick based on real elapsed time for smooth animation
        if (waterfallPlayer.isPlaying()) {
            double msPerTick = waterfallPlayer.getCurrentMsPerTick();
            if (msPerTick > 0) {
                long elapsed = System.currentTimeMillis() - waterfallPlayer.getLastStepTime();
                currentTick += (long) (elapsed / msPerTick);
            }
        }

        long windowEnd = currentTick + LOOKAHEAD_TICKS;
        List<WaterfallNote> notes = waterfallPlayer.getWaterNotes();
        RenderSystem.enableBlend();

        // Draw waterfall notes
        for (WaterfallNote note : notes) {
            long start = note.startTick();
            long end = start + note.duration();

            if (end < currentTick) continue;
            if (start > windowEnd) continue;

            long visibleStart = Math.max(start, currentTick);
            long visibleEnd = Math.min(end, windowEnd);

            float noteX = waterfallLeft + (visibleStart - currentTick) * pxPerTick;
            float noteW = Math.max(1, (visibleEnd - visibleStart) * pxPerTick);

            int noteIdx = note.note() - MIN_NOTE;
            if (noteIdx < 0 || noteIdx >= KEY_COUNT) continue;

            float noteY = noteIdx * keyHeight;
            float noteH = Math.max(1, keyHeight);

            int color = CHANNEL_COLORS[note.channel() % CHANNEL_COLORS.length];
            int alpha = 0xC0;
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;

            if (currentTick >= start && currentTick < end) {
                r = Math.min(255, r + 40);
                g = Math.min(255, g + 40);
                b = Math.min(255, b + 40);
            }

            int fillColor = (alpha << 24) | (r << 16) | (g << 8) | b;
            graphics.fill((int) noteX, (int) noteY, (int) (noteX + noteW), (int) (noteY + noteH), fillColor);

            int borderColor = (0x80 << 24) | ((r / 2) << 16) | ((g / 2) << 8) | (b / 2);
            graphics.fill((int) noteX, (int) noteY, (int) (noteX + noteW), (int) noteY + 1, borderColor);
            graphics.fill((int) noteX, (int) (noteY + noteH) - 1, (int) (noteX + noteW), (int) (noteY + noteH), borderColor);
        }

        // Draw full keyboard texture rotated 90° on the left side
        RenderSystem.setShaderTexture(0, KEYBOARD_TEX);
        graphics.pose().pushPose();
        graphics.pose().translate(KEYBOARD_WIDTH, 0, 0);
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(90));
        float sx = (float) screenHeight / TEX_W;
        float sy = (float) KEYBOARD_WIDTH / TEX_H;
        graphics.pose().scale(sx, sy, 1);
        graphics.blit(KEYBOARD_TEX, 0, 0, 0, 0, TEX_W, TEX_H, TEX_W, TEX_H);
        graphics.pose().popPose();

        // Draw the trigger line
        graphics.fill(KEYBOARD_WIDTH, 0, KEYBOARD_WIDTH + KEY_LINE_WIDTH, screenHeight, 0xFFFFFFFF);

        // Draw C note labels
        int textColor = 0xFF888888;
        for (int note = MIN_NOTE; note <= MAX_NOTE; note++) {
            if (note % 12 == 0) {
                int octave = note / 12 - 1;
                String label = "C" + octave;
                int noteIdx = note - MIN_NOTE;
                int labelY = (int) (noteIdx * keyHeight + keyHeight / 2 - 4);
                graphics.drawString(this.font, label, 4, labelY, textColor);
            }
        }

        // Current tick indicator
        String tickInfo = "Tick: " + currentTick;
        graphics.drawString(this.font, tickInfo, waterfallLeft + 4, 4, 0xFFFFFFFF);

        RenderSystem.disableBlend();

        // Render widgets (none by default, but subclasses or future additions may add them)
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        super.onClose();
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
