package org.ywzj.midi.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.ywzj.midi.all.AllItems;
import org.ywzj.midi.client.render.item.InstrumentItemRenderer;
import org.ywzj.midi.custom.instrument.BaseInstrumentData;
import org.ywzj.midi.gui.ScreenManager;
import org.ywzj.midi.instrument.Instrument;

import java.util.function.Consumer;

public class InstrumentToolItem extends Item {

    public static final String TAG_INSTRUMENT_TOOL_ID = "instrument_tool_id";

    public InstrumentToolItem(Properties properties) {
        super(properties);
    }

    public static ItemStack createInstance(BaseInstrumentData data) {
        ItemStack itemStack = new ItemStack(AllItems.INSTRUMENT_TOOL_ITEM.get());
        itemStack.getOrCreateTag().putString(InstrumentToolItem.TAG_INSTRUMENT_TOOL_ID, data.getTool());
        itemStack.setHoverName(Component.translatable("item.ywzj_midi." + data.getTool()));
        return itemStack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        if (!level.isClientSide) return InteractionResultHolder.pass(player.getItemInHand(interactionHand));

        ItemStack stack = player.getItemInHand(interactionHand);
        String myId = stack.getOrCreateTag().getString(TAG_INSTRUMENT_TOOL_ID);
        Instrument instrument = null;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack candidate = player.getItemInHand(hand);
            if (!(candidate.getItem() instanceof InstrumentItem)) continue;
            Instrument resolved = InstrumentItem.resolveInstrument(candidate);
            if (resolved == null || !myId.equals(resolved.getData().getTool())) continue;
            InteractionHand requiredHand = resolved.getData().getToolHand();
            if (requiredHand == null) requiredHand = InteractionHand.MAIN_HAND;
            if (requiredHand == interactionHand) {
                instrument = resolved;
                break;
            }
        }
        if (instrument == null) return InteractionResultHolder.pass(player.getItemInHand(interactionHand));

        ScreenManager.openInstrumentScreen(instrument, player);
        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
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
