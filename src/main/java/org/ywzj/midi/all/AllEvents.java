package org.ywzj.midi.all;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import org.ywzj.midi.audio.ServerSoundManager;
import org.ywzj.midi.client.resource.ClientAssetsManager;
import org.ywzj.midi.entity.InstrumentEntity;
import org.ywzj.midi.item.MusicPlayerItem;
import org.ywzj.midi.resource.InstrumentPackLoader;

import java.util.UUID;

public class AllEvents {

    @EventBusSubscriber
    public static class AllForgeEvents {

        @SubscribeEvent
        public static void onAttackEntity(AttackEntityEvent event) {
            if (!event.getEntity().isShiftKeyDown() || !(event.getTarget() instanceof InstrumentEntity instrument)) {
                return;
            }
            event.setCanceled(true);
            instrument.dropAsItem();
        }

        @SubscribeEvent
        public static void onDrop(ItemTossEvent event) {
            ItemStack itemStack = event.getEntity().getItem();
            if (itemStack.getItem() instanceof MusicPlayerItem) {
                UUID musicPlayerUuid = MusicPlayerItem.getUUID(itemStack);
                if (musicPlayerUuid != null) {
                    ServerSoundManager.PLAY_ENTITIES.put(musicPlayerUuid, event.getEntity());
                }
            }
        }

        @SubscribeEvent
        public static void onPickup(ItemEntityPickupEvent.Post event) {
            ItemStack itemStack = event.getOriginalStack();
            if (itemStack.getItem() instanceof MusicPlayerItem) {
                UUID musicPlayerUuid = MusicPlayerItem.getUUID(itemStack);
                if (musicPlayerUuid != null) {
                    ServerSoundManager.PLAY_ENTITIES.put(musicPlayerUuid, event.getPlayer());
                }
            }
        }

    }

    @EventBusSubscriber
    public static class AllModEvents {

        @SubscribeEvent
        public static void entityAttributes(EntityAttributeCreationEvent event) {
            event.put(AllEntities.FAKE_PLAYER.get(), Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 8.0D).build());
        }

        @SubscribeEvent
        public static void onAddPackFinders(AddPackFindersEvent event) {
            event.addRepositorySource(InstrumentPackLoader.INSTANCE);
        }

    }

    @OnlyIn(Dist.CLIENT)
    @EventBusSubscriber(value = Dist.CLIENT)
    public static class AllClientModEvents {

        @SubscribeEvent
        public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
            ClientAssetsManager.INSTANCE.registerListeners(event::registerReloadListener);
        }

    }

}
