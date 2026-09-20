package org.ywzj.midi.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.ywzj.midi.audio.ClientPlayerInstance;
import org.ywzj.midi.gui.ScreenManager;
import org.ywzj.midi.gui.screen.MusicPlayerScreen;
import org.ywzj.midi.util.ComponentUtils;
import org.ywzj.midi.util.ItemStackData;

import java.util.HashMap;
import java.util.UUID;

public class MusicPlayerItem extends Item {

    public static final HashMap<UUID, MusicPlayerScreen> SCREENS = new HashMap<>();

    public MusicPlayerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        if (!level.isClientSide) {
            genUUID(itemstack);
            genOwner(itemstack, player);
        }
        if (level.isClientSide) {
            UUID uuid = getUUID(itemstack);
            UUID owner = getOwner(itemstack);
            if (uuid == null || owner == null) {
                player.sendSystemMessage(ComponentUtils.translatable("info.ywzj_midi.power_up"));
                return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
            }
            if (!owner.equals(player.getUUID())) {
                player.sendSystemMessage(ComponentUtils.translatable("info.ywzj_midi.warn_1"));
                return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
            }
            if (SCREENS.get(uuid) == null) {
                SCREENS.put(uuid, new MusicPlayerScreen(player.position(), ComponentUtils.literal("播放器"), new ClientPlayerInstance(uuid)));
            }
            ScreenManager.openMusicPlayerScreen(SCREENS.get(getUUID(itemstack)));
        }
        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }

    public static void genUUID(ItemStack itemStack) {
        CompoundTag tag = ItemStackData.get(itemStack);
        if (!tag.contains("uuid")) {
            tag.putUUID("uuid", UUID.randomUUID());
            ItemStackData.set(itemStack, tag);
        }
    }

    public static UUID getUUID(ItemStack itemStack) {
        CompoundTag tag = ItemStackData.get(itemStack);
        if(!tag.contains("uuid")) {
            return null;
        }
        return tag.getUUID("uuid");
    }

    public static void genOwner(ItemStack itemStack, Player player) {
        CompoundTag tag = ItemStackData.get(itemStack);
        if (!tag.contains("owner")) {
            tag.putUUID("owner", player.getUUID());
            ItemStackData.set(itemStack, tag);
        }
    }

    public static UUID getOwner(ItemStack itemStack) {
        CompoundTag tag = ItemStackData.get(itemStack);
        if (!tag.contains("owner")) {
            return null;
        }
        return tag.getUUID("owner");
    }

}
