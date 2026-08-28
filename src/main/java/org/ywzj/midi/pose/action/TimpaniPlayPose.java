package org.ywzj.midi.pose.action;

import net.minecraft.world.entity.LivingEntity;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.all.AllInstruments;
import org.ywzj.midi.pose.PoseManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimpaniPlayPose {

    private final LivingEntity player;
    private int step = 0;
    public final List<PoseManager.PlayPose> strikePoses = new ArrayList<>();
    private final float yHeadRotInit;
    private final float yBodyRotInit;

    public TimpaniPlayPose(LivingEntity player) {
        this.player = player;
        this.yHeadRotInit = player.yHeadRot;
        this.yBodyRotInit = player.yBodyRot;
        PoseManager.PlayPose basePose = new PoseManager.PlayPose(null,null,null,-0.9f,0.3f,0f,null,null,null,-0.9f,-0.3f,0f);
        for (int i = 0; i < 5; i++) {
            basePose.leftArmRotX += 0.06f;
            basePose.rightArmRotX -= 0.06f;
            strikePoses.add(new PoseManager.PlayPose(basePose));
        }
        for (int i = 0; i < 10; i++) {
            basePose.leftArmRotX -= 0.06f;
            basePose.rightArmRotX += 0.06f;
            strikePoses.add(new PoseManager.PlayPose(basePose));
        }
        for (int i = 0; i < 5; i++) {
            basePose.leftArmRotX += 0.06f;
            basePose.rightArmRotX -= 0.06f;
            strikePoses.add(new PoseManager.PlayPose(basePose));
        }
    }

    public void hit(int note) {
        for (int i = 0; i < 9; i++) {
            PoseManager.publish(player, strikePoses.get(step % 20));
            step += 1;
        }
        PoseManager.publish(player, strikePoses.get(step % 20), AllInstruments.fromId(YwzjMidi.modLocation("timpani")), Collections.singletonList(note));
        step += 1;
    }

    public void reset() {
        player.yHeadRot = yHeadRotInit;
        player.yBodyRot = yBodyRotInit;
    }

}
