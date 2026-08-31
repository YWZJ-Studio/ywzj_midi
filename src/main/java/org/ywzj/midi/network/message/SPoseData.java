package org.ywzj.midi.network.message;

import net.minecraft.network.FriendlyByteBuf;
import org.ywzj.midi.pose.PoseManager;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SPoseData extends CPoseData {

    public SPoseData(CPoseData cPoseData) {
        this.leftArmX = cPoseData.leftArmX;
        this.leftArmY = cPoseData.leftArmY;
        this.leftArmZ = cPoseData.leftArmZ;
        this.leftArmRotX = cPoseData.leftArmRotX;
        this.leftArmRotY = cPoseData.leftArmRotY;
        this.leftArmRotZ = cPoseData.leftArmRotZ;
        this.rightArmX = cPoseData.rightArmX;
        this.rightArmY = cPoseData.rightArmY;
        this.rightArmZ = cPoseData.rightArmZ;
        this.rightArmRotX = cPoseData.rightArmRotX;
        this.rightArmRotY = cPoseData.rightArmRotY;
        this.rightArmRotZ = cPoseData.rightArmRotZ;
        this.instrumentId = cPoseData.instrumentId;
        this.instrumentEntityId = cPoseData.instrumentEntityId;
        this.notesLength = cPoseData.notesLength;
        this.notes = cPoseData.notes;
        this.playerUuid = cPoseData.playerUuid;
    }

    public static SPoseData decode(FriendlyByteBuf buf) {
        return new SPoseData(CPoseData.decode(buf));
    }

    public PoseManager.PlayPose toPlayPose() {
        PoseManager.PlayPose pose = new PoseManager.PlayPose();
        pose.leftArmX = getValue(this.leftArmX);
        pose.leftArmY = getValue(this.leftArmY);
        pose.leftArmZ = getValue(this.leftArmZ);
        pose.leftArmRotX = getValue(this.leftArmRotX);
        pose.leftArmRotY = getValue(this.leftArmRotY);
        pose.leftArmRotZ = getValue(this.leftArmRotZ);
        pose.rightArmX = getValue(this.rightArmX);
        pose.rightArmY = getValue(this.rightArmY);
        pose.rightArmZ = getValue(this.rightArmZ);
        pose.rightArmRotX = getValue(this.rightArmRotX);
        pose.rightArmRotY = getValue(this.rightArmRotY);
        pose.rightArmRotZ = getValue(this.rightArmRotZ);
        pose.instrumentId = this.instrumentId;
        pose.notes = IntStream.of(this.notes).boxed().collect(Collectors.toList());
        pose.instrumentEntityId = this.instrumentEntityId;
        return pose;
    }

}
