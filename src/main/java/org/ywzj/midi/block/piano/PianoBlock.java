package org.ywzj.midi.block.piano;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.ywzj.midi.block.HorizontalBlock;
import org.ywzj.midi.blockentity.PianoBlockEntity;
import org.ywzj.midi.gui.ScreenManager;
import org.ywzj.midi.instrument.Instrument;

public abstract class PianoBlock extends HorizontalBlock implements EntityBlock {

    public PianoBlock(Properties properties) {
        super(properties);
    }

    protected abstract Instrument getInstrument();

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult ray) {
        if (world.isClientSide) {
            PianoBlockEntity pianoBlockEntity = (PianoBlockEntity) world.getBlockEntity(pos);
            if (pianoBlockEntity == null) {
                throw new RuntimeException("Can't find Piano in " + pos);
            }
            ScreenManager.openPianoScreen(pos, getInstrument(), pianoBlockEntity);
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PianoBlockEntity(pPos, pState);
    }

}
