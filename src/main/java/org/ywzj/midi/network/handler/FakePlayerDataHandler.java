package org.ywzj.midi.network.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.ywzj.midi.entity.FakePlayerEntity;
import org.ywzj.midi.network.message.CFakePlayerUpdate;


public class FakePlayerDataHandler {

    public static void onServerMessageReceived(CFakePlayerUpdate message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Entity entity = ((ServerPlayer) ctx.player()).level().getEntity(message.id);
            if (entity instanceof FakePlayerEntity fakePlayerEntity) {
                fakePlayerEntity.getEntityData().set(FakePlayerEntity.SKIN_URL, message.skinUrl);
                fakePlayerEntity.getEntityData().set(FakePlayerEntity.NAME, message.name);
                fakePlayerEntity.getEntityData().set(FakePlayerEntity.IS_SITTING, message.isSitting);
            }
        });
    }

}
