package org.ywzj.midi.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.ywzj.midi.all.AllEntities;
import org.ywzj.midi.entity.FakePlayerEntity;
import org.ywzj.midi.gui.ScreenManager;
import org.ywzj.midi.util.ComponentUtils;

import java.util.List;

public class BatonItem extends Item {

    public BatonItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        if (level.isClientSide && !player.isCrouching()) {
            ScreenManager.openBatonScreen();
        }
        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        if (!level.isClientSide && useOnContext.getHand().equals(InteractionHand.MAIN_HAND) && useOnContext.getPlayer().isCrouching()) {
            FakePlayerEntity fakePlayerEntity = new FakePlayerEntity(AllEntities.FAKE_PLAYER.get(), level);
            fakePlayerEntity.setPos(useOnContext.getClickLocation());
            level.addFreshEntity(fakePlayerEntity);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, context, components, tooltipFlag);
        components.add(ComponentUtils.translatable("tip.ywzj_midi.baton"));
    }

}
