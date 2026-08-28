package org.ywzj.midi.block.piano;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.all.AllInstruments;
import org.ywzj.midi.instrument.Instrument;

public class CFXBlock extends PianoBlock {

    private static final VoxelShape SHAPE_NORTH = Shapes.box(-0.04, 0.01, -1, 2, 2, 2);
    private static final VoxelShape SHAPE_SOUTH = Shapes.box(-1, 0.01, -1, 1.04, 2, 2);
    private static final VoxelShape SHAPE_EAST = Shapes.box(-1, 0.01, -0.04, 2, 2, 2);
    private static final VoxelShape SHAPE_WEST = Shapes.box(-1, 0.01, -1, 2, 2, 1.04);

    public CFXBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected Instrument getInstrument() {
        return AllInstruments.fromId(YwzjMidi.modLocation("cfx"));
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return switch (pState.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            case DOWN, UP -> null;
        };
    }

}
