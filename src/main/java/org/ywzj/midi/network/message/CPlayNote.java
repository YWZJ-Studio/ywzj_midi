package org.ywzj.midi.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class CPlayNote {

    public double x;
    public double y;
    public double z;
    public ResourceLocation instrumentId;
    public int variantId;
    public int note;
    public int velocity;
    public int delay;
    public boolean on;
    public UUID uuid;

    public CPlayNote() {}

    public CPlayNote(UUID uuid) {
        this.x = -1;
        this.y = -1;
        this.z = -1;
        this.instrumentId = new ResourceLocation("n", "n");
        this.variantId = -1;
        this.note = -1;
        this.velocity = -1;
        this.delay = -1;
        this.on = false;
        this.uuid = uuid;
    }

    public CPlayNote(Vec3 pos, ResourceLocation instrumentId, int variantId, int note, int velocity, int delay, UUID uuid) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.instrumentId = instrumentId;
        this.variantId = variantId;
        this.note = note;
        this.velocity = velocity;
        this.delay = delay;
        this.on = true;
        this.uuid = uuid;
    }

    public static CPlayNote decode(FriendlyByteBuf buf) {
        CPlayNote data = new CPlayNote();
        data.x = buf.readDouble();
        data.y = buf.readDouble();
        data.z = buf.readDouble();
        data.instrumentId = buf.readResourceLocation();
        data.variantId = buf.readInt();
        data.note = buf.readInt();
        data.velocity = buf.readInt();
        data.delay = buf.readInt();
        data.on = buf.readBoolean();
        data.uuid = buf.readUUID();
        return data;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeResourceLocation(instrumentId);
        buf.writeInt(variantId);
        buf.writeInt(note);
        buf.writeInt(velocity);
        buf.writeInt(delay);
        buf.writeBoolean(on);
        buf.writeUUID(uuid);
    }

}
