package org.ywzj.midi.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.ywzj.midi.blockentity.SpeakerBlockEntity;
import org.ywzj.midi.gui.ScreenManager;
import org.ywzj.midi.gui.screen.MusicPlayerScreen;
import org.ywzj.midi.util.ComponentUtils;

public class SpeakerBlock extends HorizontalBlock implements EntityBlock {

    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 21, 15);

    public SpeakerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult ray) {
        if (world.isClientSide) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof SpeakerBlockEntity speakerBlockEntity) {
                if (speakerBlockEntity.musicPlayerScreen == null) {
                    speakerBlockEntity.musicPlayerScreen = new MusicPlayerScreen(new Vec3(pos.getX(), pos.getY(), pos.getZ()), ComponentUtils.literal("播放器"), speakerBlockEntity.getClientPlayerInstance());
                }
                ScreenManager.openSpeakerScreen(pos, speakerBlockEntity.musicPlayerScreen);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SpeakerBlockEntity(pPos, pState);
    }

}
