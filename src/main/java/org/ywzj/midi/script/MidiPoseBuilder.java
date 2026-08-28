package org.ywzj.midi.script;

import org.ywzj.midi.pose.PoseManager;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class MidiPoseBuilder {

    private Float leftArmX, leftArmY, leftArmZ;
    private Float leftArmRotX, leftArmRotY, leftArmRotZ;
    private Float rightArmX, rightArmY, rightArmZ;
    private Float rightArmRotX, rightArmRotY, rightArmRotZ;
    private final Map<String, BoneTransform> boneTransforms = new LinkedHashMap<>();

    public record BoneTransform(float rotX, float rotY, float rotZ, float transX, float transY, float transZ) {}

    public void setLeftArmRotation(Double rx, Double ry, Double rz) {
        if (rx != null) this.leftArmRotX = rx.floatValue();
        if (ry != null) this.leftArmRotY = ry.floatValue();
        if (rz != null) this.leftArmRotZ = rz.floatValue();
    }

    public void setRightArmRotation(Double rx, Double ry, Double rz) {
        if (rx != null) this.rightArmRotX = rx.floatValue();
        if (ry != null) this.rightArmRotY = ry.floatValue();
        if (rz != null) this.rightArmRotZ = rz.floatValue();
    }

    public void setLeftArmPosition(Double x, Double y, Double z) {
        if (x != null) this.leftArmX = x.floatValue();
        if (y != null) this.leftArmY = y.floatValue();
        if (z != null) this.leftArmZ = z.floatValue();
    }

    public void setRightArmPosition(Double x, Double y, Double z) {
        if (x != null) this.rightArmX = x.floatValue();
        if (y != null) this.rightArmY = y.floatValue();
        if (z != null) this.rightArmZ = z.floatValue();
    }

    public void setLeftArm(Double rx, Double ry, Double rz, Double tx, Double ty, Double tz) {
        setLeftArmRotation(rx, ry, rz);
        setLeftArmPosition(tx, ty, tz);
    }

    public void setRightArm(Double rx, Double ry, Double rz, Double tx, Double ty, Double tz) {
        setRightArmRotation(rx, ry, rz);
        setRightArmPosition(tx, ty, tz);
    }

    public void setBone(String name,
                        Double rotX, Double rotY, Double rotZ,
                        Double transX, Double transY, Double transZ) {
        float rx = rotX != null ? rotX.floatValue() : 0;
        float ry = rotY != null ? rotY.floatValue() : 0;
        float rz = rotZ != null ? rotZ.floatValue() : 0;
        float tx = transX != null ? transX.floatValue() : 0;
        float ty = transY != null ? transY.floatValue() : 0;
        float tz = transZ != null ? transZ.floatValue() : 0;
        boneTransforms.put(name, new BoneTransform(rx, ry, rz, tx, ty, tz));
    }

    public void setRotation(String name, Double rotX, Double rotY, Double rotZ) {
        BoneTransform existing = boneTransforms.get(name);
        float tx = existing != null ? existing.transX() : 0;
        float ty = existing != null ? existing.transY() : 0;
        float tz = existing != null ? existing.transZ() : 0;
        setBone(name, rotX, rotY, rotZ, (double) tx, (double) ty, (double) tz);
    }

    public void setTranslation(String name, Double transX, Double transY, Double transZ) {
        BoneTransform existing = boneTransforms.get(name);
        float rx = existing != null ? existing.rotX() : 0;
        float ry = existing != null ? existing.rotY() : 0;
        float rz = existing != null ? existing.rotZ() : 0;
        setBone(name, (double) rx, (double) ry, (double) rz, transX, transY, transZ);
    }

    public Map<String, BoneTransform> getBoneTransforms() {
        if (boneTransforms.isEmpty()) {
            return Collections.emptyMap();
        }
        return new LinkedHashMap<>(boneTransforms);
    }

    public void clear() {
        leftArmX = null; leftArmY = null; leftArmZ = null;
        leftArmRotX = null; leftArmRotY = null; leftArmRotZ = null;
        rightArmX = null; rightArmY = null; rightArmZ = null;
        rightArmRotX = null; rightArmRotY = null; rightArmRotZ = null;
        boneTransforms.clear();
    }

    public PoseManager.PlayPose build() {
        return new PoseManager.PlayPose(
            leftArmX, leftArmY, leftArmZ,
            leftArmRotX, leftArmRotY, leftArmRotZ,
            rightArmX, rightArmY, rightArmZ,
            rightArmRotX, rightArmRotY, rightArmRotZ
        );
    }
}
