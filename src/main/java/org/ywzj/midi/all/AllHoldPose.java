package org.ywzj.midi.all;

import net.minecraft.world.InteractionHand;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.pose.PoseManager;

public class AllHoldPose {

    static {
        PoseManager.registerHoldPose(YwzjMidi.modLocation("cello"),
                InteractionHand.OFF_HAND,
                new PoseManager.PlayPose(3.2f,null,1f,-1.6f,0f,0.1f,null,null,null,null,null,null));
        PoseManager.registerHoldPose(YwzjMidi.modLocation("double_bass"),
                InteractionHand.OFF_HAND,
                new PoseManager.PlayPose(3.2f,null,1f,-2f,0f,0.1f,null,null,null,null,null,null));
        PoseManager.registerHoldPose(YwzjMidi.modLocation("flute"),
                InteractionHand.MAIN_HAND,
                new PoseManager.PlayPose(null,null,null,null,null,null,null,null,null,-0.6f,null,null));
        PoseManager.registerHoldPose(YwzjMidi.modLocation("bassoon"),
                InteractionHand.MAIN_HAND,
                new PoseManager.PlayPose(6f,4f,-0f,-0.8f,0.7f,0f,-6f,4f,null,-0.9f,-0.8f,0.3f));
        PoseManager.registerHoldPose(YwzjMidi.modLocation("horn"),
                InteractionHand.MAIN_HAND,
                new PoseManager.PlayPose(null,3f,-3f,-1f,0.6f,0f,null,4f,1.5f,-0.6f,0.2f,0.3f));
        PoseManager.registerHoldPose(YwzjMidi.modLocation("trumpet"),
                InteractionHand.MAIN_HAND,
                new PoseManager.PlayPose(null,4f,null,-1.2f,0.5f,0f,null,4f,null,-1.2f,-0.5f,0f));
        PoseManager.registerHoldPose(YwzjMidi.modLocation("trombone"),
                InteractionHand.OFF_HAND,
                new PoseManager.PlayPose(null,4f,null,-1f,0.5f,0f,null,4f,null,-1f,-0.5f,0f));
        PoseManager.registerHoldPose(YwzjMidi.modLocation("tuba"),
                InteractionHand.MAIN_HAND,
                new PoseManager.PlayPose(null,null,null,-0.3f,null,0f,null,null,null,-1.0f,null,0f));
        PoseManager.registerHoldPose(YwzjMidi.modLocation("cymbal"),
                InteractionHand.MAIN_HAND,
                new PoseManager.PlayPose(null,null,null,-1.0f,0f,0.02f,null,null,null,-1.0f,0f,-0.02f));
    }

    public static void preRegister() {}

}
