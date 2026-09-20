package org.ywzj.midi.gui.widget;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

public class CommonButton extends Button {

    private final boolean withSound;

    public CommonButton(int x, int y, int width, int height, Component component, Button.OnPress onPress) {
        super(x, y, width, height, component, onPress, DEFAULT_NARRATION);
        this.withSound = true;
    }

    public CommonButton(int x, int y, int width, int height, Component component, Button.OnPress onPress, boolean withSound) {
        super(x, y, width, height, component, onPress, DEFAULT_NARRATION);
        this.withSound = withSound;
    }

    public void updatePos(int x, int y) {
        this.setX(x);
        this.setY(y);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        if (withSound) {
            super.playDownSound(soundManager);
        }
    }

}
