package org.ywzj.midi.pose.action;

import net.minecraft.world.entity.LivingEntity;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.all.AllInstruments;
import org.ywzj.midi.pose.PoseManager;
import org.ywzj.midi.pose.handler.TromboneSlideHandler;

import java.util.Collections;

public class TrombonePlayPose extends BrassPlayPose {

    public TrombonePlayPose(LivingEntity player) {
        super(player);
        super.playPose = new PoseManager.PlayPose(null,4f,null,-1.7f,0.5f,0f,null,4f,null, -1.7f, -0.5f,0f);
    }

    @Override
    public void play(int note) {
        Integer slide = TromboneSlideHandler.noteToSlide(note);
        playPose.rightArmRotY = (float) (slide * 0.02 - 0.5f);
        playPose.rightArmZ = -(float) slide;
        PoseManager.publish(player, playPose, AllInstruments.fromId(YwzjMidi.modLocation("trombone")), Collections.singletonList(note));
    }

}
