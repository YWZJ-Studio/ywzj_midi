package org.ywzj.midi.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.ywzj.midi.all.AllInstruments;
import org.ywzj.midi.all.AllItems;
import org.ywzj.midi.client.render.item.InstrumentItemRenderer;
import org.ywzj.midi.gui.ScreenManager;
import org.ywzj.midi.instrument.Instrument;
import org.ywzj.midi.util.ItemStackData;

import java.util.function.Consumer;

public class InstrumentItem extends Item {

    public static final String TAG_INSTRUMENT_ID = "instrument_id";

    public InstrumentItem(Properties pProperties) {
        super(pProperties);
    }

    public static ItemStack createInstance(ResourceLocation instrumentId, Component name) {
        ItemStack itemStack = new ItemStack(AllItems.INSTRUMENT_ITEM.get());
        CompoundTag tag = ItemStackData.get(itemStack);
        tag.putString(TAG_INSTRUMENT_ID, instrumentId.toString());
        ItemStackData.set(itemStack, tag);
        itemStack.set(DataComponents.CUSTOM_NAME, name);
        return itemStack;
    }

    static Instrument resolveInstrument(ItemStack stack) {
        String idStr = ItemStackData.get(stack).getString(TAG_INSTRUMENT_ID);
        if (idStr.isEmpty()) return null;
        ResourceLocation id = ResourceLocation.tryParse(idStr);
        if (id == null) return null;
        return AllInstruments.fromId(id);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        if (!level.isClientSide) return InteractionResultHolder.pass(player.getItemInHand(interactionHand));

        ItemStack stack = player.getItemInHand(interactionHand);
        Instrument instrument = resolveInstrument(stack);
        if (instrument == null) return InteractionResultHolder.pass(player.getItemInHand(interactionHand));

        var data = instrument.getData();
        InteractionHand playHand = data.getPlayHand();
        if (playHand != null && interactionHand != playHand) {
            return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
        }

        String tool = data.getTool();
        if (tool != null) {
            InteractionHand toolHand = data.getToolHand();
            if (toolHand == null) toolHand = InteractionHand.MAIN_HAND;
            String heldTool = ItemStackData.get(player.getItemInHand(toolHand)).getString(InstrumentToolItem.TAG_INSTRUMENT_TOOL_ID);
            if (!heldTool.equals(tool)) {
                player.sendSystemMessage(Component.translatable("info.ywzj_midi.need_tool",
                        Component.translatable("item.ywzj_midi." + tool)));
                return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
            }
        }

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
