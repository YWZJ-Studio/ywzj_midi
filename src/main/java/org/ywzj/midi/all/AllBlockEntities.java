package org.ywzj.midi.all;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.blockentity.NoteBlockEntity;
import org.ywzj.midi.blockentity.SpeakerBlockEntity;

public class AllBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, YwzjMidi.MOD_ID);

    public static final RegistryObject<BlockEntityType<NoteBlockEntity>> NOTE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("note_block_entity", () ->
                    BlockEntityType.Builder.of(NoteBlockEntity::new,
                            AllBlocks.SPEAKER_BLOCK.get(),
                            AllBlocks.BASS_DRUM_BLOCK.get(),
                            AllBlocks.TIMPANI_BLOCK.get(),
                            AllBlocks.AA775_BLOCK.get(),
                            AllBlocks.U1H_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<SpeakerBlockEntity>> SPEAKER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("speaker_block_entity", () ->
                    BlockEntityType.Builder.of(SpeakerBlockEntity::new,
                            AllBlocks.SPEAKER_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }

}
