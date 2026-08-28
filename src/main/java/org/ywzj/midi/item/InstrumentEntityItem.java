package org.ywzj.midi.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.ywzj.midi.all.AllItems;
import org.ywzj.midi.client.render.item.InstrumentItemRenderer;
import org.ywzj.midi.entity.InstrumentEntity;
import org.ywzj.midi.instrument.Instrument;

import java.util.function.Consumer;

public class InstrumentEntityItem extends Item {

    public static final String TAG_INSTRUMENT_ID = "instrument_id";

    public InstrumentEntityItem(Properties properties) {
        super(properties);
    }

    public static ItemStack createInstance(ResourceLocation instrumentId, Component name) {
        ItemStack itemStack = new ItemStack(AllItems.INSTRUMENT_ENTITY_ITEM.get());
        itemStack.getOrCreateTag().putString(InstrumentItem.TAG_INSTRUMENT_ID, instrumentId.toString());
        itemStack.setHoverName(name);
        return itemStack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player == null) return InteractionResult.FAIL;

        Instrument instrument = InstrumentItem.resolveInstrument(stack);
        if (instrument == null) return InteractionResult.FAIL;

        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockPos placePos = clickedPos.relative(face);

        if (!level.isClientSide) {
            Vec3 center = Vec3.atCenterOf(placePos);
            InstrumentEntity entity = new InstrumentEntity(level, instrument.getInstrumentId(), instrument.getName());
            entity.setPos(center.x, placePos.getY(), center.z);
            entity.setYRot(player.getYRot());
            level.addFreshEntity(entity);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private InstrumentItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                Minecraft mc = Minecraft.getInstance();
                if (renderer == null) {
                    renderer = new InstrumentItemRenderer(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
                }
                return renderer;
            }
        });
    }

}
