package org.ywzj.midi.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.ywzj.midi.client.render.item.InstrumentItemRenderer;

import java.util.function.Consumer;

/**
 * A generic instrument item whose appearance is driven by a data-pack display definition.
 * The display ID is stored in item NBT under TAG_DISPLAY_ID.
 */
public class InstrumentItem extends Item {

    public static final String TAG_DISPLAY_ID = "instrument_display_id";

    public InstrumentItem(Properties pProperties) {
        super(pProperties);
    }

    /**
     * Create an item stack pre-tagged with the given display ID.
     */
    public ItemStack createInstance(ResourceLocation displayId, Component name) {
        ItemStack stack = new ItemStack(this);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_DISPLAY_ID, displayId.toString());
        stack.setHoverName(name);
        return stack;
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
