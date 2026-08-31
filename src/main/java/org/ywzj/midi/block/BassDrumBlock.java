package org.ywzj.midi.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.all.AllInstruments;
import org.ywzj.midi.audio.NotePlayer;
import org.ywzj.midi.blockentity.TimpaniBlockEntity;
import org.ywzj.midi.item.InstrumentToolItem;
import org.ywzj.midi.pose.action.BassDrumPlayPose;

import java.util.UUID;

public class BassDrumBlock extends HorizontalBlock implements EntityBlock {

    private static final VoxelShape SHAPE_NORTH = Shapes.box(-0.08, 0.01, 0, 1.08, 1.55, 1);
    private static final VoxelShape SHAPE_SOUTH = Shapes.box(-0.08, 0.01, 0, 1.08, 1.55, 1);
    private static final VoxelShape SHAPE_EAST = Shapes.box(0, 0.01, -0.08, 1, 1.55, 1.08);
    private static final VoxelShape SHAPE_WEST = Shapes.box(0, 0.01, -0.08, 1, 1.55, 1.08);
    private static BassDrumPlayPose bassDrumPlayPose;

    public BassDrumBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult ray) {
        if (world.isClientSide && hand.equals(InteractionHand.MAIN_HAND)) {
            var bassDrum = AllInstruments.fromId(YwzjMidi.modLocation("bass_drum"));
            if (InstrumentToolItem.matches(player.getItemInHand(hand), bassDrum)) {
                if (bassDrumPlayPose == null) {
                    bassDrumPlayPose = new BassDrumPlayPose(player);
                }
                bassDrumPlayPose.hit();
                NotePlayer.playNote(UUID.randomUUID(), new Vec3(pos.getX(), pos.getY(), pos.getZ()), bassDrum, 0, 36, 127, 0, player);
            }
        }
        return InteractionResult.PASS;
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TimpaniBlockEntity(blockPos, blockState);
    }

}
