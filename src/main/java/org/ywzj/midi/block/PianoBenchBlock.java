package org.ywzj.midi.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3d;
import org.ywzj.midi.entity.SeatEntity;

public class PianoBenchBlock extends HorizontalBlock {

    private static final VoxelShape SHAPE_NORTH = Shapes.box(0.62, 0.1, 0.2, 1.44, 0.6, 0.75);
    private static final VoxelShape SHAPE_SOUTH = Shapes.box(-0.44, 0.1, 0.25, 0.38, 0.6, 0.8);
    private static final VoxelShape SHAPE_EAST = Shapes.box(0.25, 0.1, 0.62, 0.8, 0.6, 1.44);
    private static final VoxelShape SHAPE_WEST = Shapes.box(0.2, 0.1, -0.44, 0.75, 0.6, 0.38);

    public PianoBenchBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult ray) {
        Vector3d setPos = null;
        Direction direction = state.getValue(FACING);
        if (direction.equals(Direction.NORTH)) {
            setPos = new Vector3d(pos.getX() + 1, pos.getY() + 0.3, pos.getZ() + 0.5);
        } else if (direction.equals(Direction.SOUTH)) {
            setPos = new Vector3d(pos.getX() - 0, pos.getY() + 0.3, pos.getZ() + 0.5);
        } else if (direction.equals(Direction.WEST)) {
            setPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() - 0);
        } else if (direction.equals(Direction.EAST)) {
            setPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 1);
        }
        if (setPos == null) {
            return InteractionResult.FAIL;
        }
        return SeatEntity.create(world, pos, setPos, player, direction);
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
