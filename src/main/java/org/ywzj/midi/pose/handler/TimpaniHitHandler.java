package org.ywzj.midi.pose.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.blockentity.TimpaniBlockEntity;
import org.ywzj.midi.entity.FakePlayerEntity;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TimpaniHitHandler extends NotesHandler {

    @Override
    public ResourceLocation getInstrumentId() {
        return YwzjMidi.modLocation("timpani");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handle(UUID playerUuid, List<Integer> notes) {
        Player localPlayer = Minecraft.getInstance().player;
        List<LivingEntity> findPlayers = localPlayer.level().getEntities(localPlayer,
                new AABB(localPlayer.getX()-32.0D, localPlayer.getY()-32.0D, localPlayer.getZ()-32.0D, localPlayer.getX()+32.0D, localPlayer.getY()+32.0D, localPlayer.getZ()+32.0D))
                .stream()
                .filter(entity -> entity instanceof Player || entity instanceof FakePlayerEntity)
                .map(entity -> (LivingEntity) entity)
                .collect(Collectors.toList());
        findPlayers.add(localPlayer);
        findPlayers = findPlayers.stream()
                .filter(otherPlayer -> otherPlayer.getUUID().equals(playerUuid))
                .collect(Collectors.toList());
        if (findPlayers.size() > 0) {
            TimpaniBlockEntity timpaniBlockEntity = getTimpani(findPlayers.get(0), notes.get(0));
            if (timpaniBlockEntity == null) {
                return;
            }
            findPlayers.get(0).lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(timpaniBlockEntity.getBlockPos().getX() + 0.5f, timpaniBlockEntity.getBlockPos().getY() + 1f, timpaniBlockEntity.getBlockPos().getZ() + 0.5f));
        }
    }

    private TimpaniBlockEntity getTimpani(LivingEntity player, int note) {
        for (int x = player.getBlockX() - 2; x <= player.getBlockX() + 2; x += 1) {
            for (int y = player.getBlockY() - 2; y <= player.getBlockY() + 2; y += 1) {
                for (int z = player.getBlockZ() - 2; z <= player.getBlockZ() + 2; z += 1) {
                    BlockEntity blockEntity = player.level().getBlockEntity(new BlockPos(x, y, z));
                    if (blockEntity instanceof TimpaniBlockEntity timpaniBlockEntity) {
                        if (timpaniBlockEntity.note == note) {
                            return timpaniBlockEntity;
                        }
                    }
                }
            }
        }
        return null;
    }

}
