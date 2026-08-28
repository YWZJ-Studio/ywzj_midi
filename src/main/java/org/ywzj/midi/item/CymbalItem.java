package org.ywzj.midi.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.all.AllInstruments;
import org.ywzj.midi.all.AllItems;
import org.ywzj.midi.audio.NotePlayer;
import org.ywzj.midi.pose.action.CymbalPlayPose;
import org.ywzj.midi.util.MidiUtils;

import java.util.UUID;

public class CymbalItem extends Item {

    private static CymbalPlayPose cymbalPlayPose;

    public CymbalItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        if (level.isClientSide && !player.isCrouching()
                && interactionHand.equals(InteractionHand.MAIN_HAND)) {
            if (!player.getItemInHand(InteractionHand.OFF_HAND).getItem().equals(AllItems.ITEMS_LOOKUP.get("cymbal").get())) {
                player.sendSystemMessage(Component.translatable("info.ywzj_midi.need_cymbal"));
                return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
            }
            if (cymbalPlayPose == null) {
                cymbalPlayPose = new CymbalPlayPose(player);
            }
            cymbalPlayPose.hit();
            NotePlayer.playNote(UUID.randomUUID(), new Vec3(player.getX(), player.getY(), player.getZ()), AllInstruments.fromId(YwzjMidi.modLocation("cymbal")), 0, MidiUtils.notationToNote("c1"), 127, 0, player);
        }
        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }

}
