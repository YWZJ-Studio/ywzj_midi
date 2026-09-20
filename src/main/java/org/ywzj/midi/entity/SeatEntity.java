package org.ywzj.midi.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.ywzj.midi.all.AllEntities;

import java.util.List;

public class SeatEntity extends Entity
{

    private BlockPos seatPos;

    public SeatEntity(Level level) {
        super(AllEntities.SEAT.get(), level);
        this.noPhysics = true;
    }

    private SeatEntity(Level level, BlockPos seatPos, Vector3d setPos, Direction direction) {
        this(level);
        this.seatPos = seatPos;
        this.setPos(setPos.x, setPos.y, setPos.z);
        this.setRot(direction.getOpposite().toYRot(), 0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            try {
                if(this.getPassengers().isEmpty() || this.level().isEmptyBlock(seatPos))
                {
                    this.remove(RemovalReason.DISCARDED);
                    this.level().updateNeighbourForOutputSignal(blockPosition(), this.level().getBlockState(blockPosition()).getBlock());
                }
            } catch (Exception exceptione) {
                this.remove(RemovalReason.DISCARDED);
            }
        }
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {}

    @Override
    protected boolean canRide(Entity entity) {
        return true;
    }

    public static InteractionResult create(Level level, BlockPos seatPos, Vector3d setPos, Player player, Direction direction) {
        if(!level.isClientSide()) {
            List<SeatEntity> seats = level.getEntitiesOfClass(SeatEntity.class, new AABB(seatPos.getX(), seatPos.getY(), seatPos.getZ(), seatPos.getX() + 1.0, seatPos.getY() + 1.0, seatPos.getZ() + 1.0));
            if(seats.isEmpty())
            {
                SeatEntity seat = new SeatEntity(level, seatPos, setPos, direction);
                level.addFreshEntity(seat);
                player.startRiding(seat, false);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity entity) {
        Direction original = this.getDirection();
        Direction[] offsets = {original, original.getClockWise(), original.getCounterClockWise(), original.getOpposite()};
        for(Direction dir : offsets)
        {
            Vec3 safeVec = DismountHelper.findSafeDismountLocation(entity.getType(), this.level(), this.blockPosition().relative(dir), false);
            if(safeVec != null)
            {
                return safeVec.add(0, 0.25, 0);
            }
        }
        return super.getDismountLocationForPassenger(entity);
    }

    @Override
    protected void addPassenger(Entity entity) {
        super.addPassenger(entity);
        entity.setYRot(this.getYRot());
    }

    @Override
    public void positionRider(Entity entity, Entity.MoveFunction function) {
        super.positionRider(entity, function);
        this.clampYaw(entity);
    }

    @Override
    public void onPassengerTurned(Entity entity) {
        this.clampYaw(entity);
    }

    private void clampYaw(Entity passenger) {
        passenger.setYBodyRot(this.getYRot());
        float wrappedYaw = Mth.wrapDegrees(passenger.getYRot() - this.getYRot());
        float clampedYaw = Mth.clamp(wrappedYaw, -120.0F, 120.0F);
        passenger.yRotO += clampedYaw - wrappedYaw;
        passenger.setYRot(passenger.getYRot() + clampedYaw - wrappedYaw);
        passenger.setYHeadRot(passenger.getYRot());
    }

    @Override
    public boolean save(CompoundTag nbt) {
        nbt.putInt("x", seatPos.getX());
        nbt.putInt("y", seatPos.getY());
        nbt.putInt("z", seatPos.getZ());
        return super.save(nbt);
    }
    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        seatPos = new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"));
    }

}
