package com.openwheelracing.content.entity;

import com.openwheelracing.content.car.PrototypeCarSetup;
import com.openwheelracing.content.item.PrototypeCarItem;
import com.openwheelracing.registry.OWRBlocks;
import com.openwheelracing.registry.OWRItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class OpenwheelCarEntity extends Entity {
    private static final EntityDataAccessor<Integer> GEAR = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RPM = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SPEED = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TYRE_WEAR = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> CURRENT_LAP_TICKS = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BEST_LAP_TICKS = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CHECKPOINT_ARMED = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.BOOLEAN);

    private static final Vec3 SEAT_OFFSET = new Vec3(0.0, 0.45, 0.0);
    private static final Vec3[] DISMOUNT_OFFSETS = {
        new Vec3(1.1, 0.0, 0.15),
        new Vec3(-1.1, 0.0, 0.15),
        new Vec3(0.0, 0.0, 1.25),
        new Vec3(0.0, 0.0, -1.65)
    };

    private static final double[] GEAR_RATIOS = {0.0, 0.28, 0.43, 0.58, 0.74, 0.91, 1.10};
    private static final double STEERING_DEADZONE = 0.08;
    private static final double MIN_STEERING_SPEED = 0.04;
    private static final double BASE_MAX_SPEED = 1.32;
    private static final double BASE_ACCELERATION = 0.031;
    private static final double BASE_BRAKE_FORCE = 0.072;
    private static final double AIR_DRAG = 0.965;
    private static final double GROUND_DRAG = 0.99;
    private PrototypeCarSetup setup = PrototypeCarSetup.DEFAULT;
    private double previousHorizontalSpeed;
    private long lapStartedAt = -1L;
    private long lastLowTyreWarningAt = -200L;
    private long lastDamageWarningAt = -200L;

    public OpenwheelCarEntity(EntityType<? extends OpenwheelCarEntity> entityType, Level level) {
        super(entityType, level);
        blocksBuilding = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(GEAR, 1);
        builder.define(RPM, 900);
        builder.define(SPEED, 0.0f);
        builder.define(DAMAGE, 0.0f);
        builder.define(TYRE_WEAR, 0.0f);
        builder.define(CURRENT_LAP_TICKS, 0);
        builder.define(BEST_LAP_TICKS, 0);
        builder.define(CHECKPOINT_ARMED, false);
    }

    public void setSetup(PrototypeCarSetup setup) {
        this.setup = setup;
    }

    public PrototypeCarSetup getSetup() {
        return setup;
    }

    public void setDamagePercent(float damage) {
        entityData.set(DAMAGE, Math.max(0.0f, Math.min(100.0f, damage)));
    }

    public void setTyreWearPercent(float tyreWear) {
        entityData.set(TYRE_WEAR, Math.max(0.0f, Math.min(100.0f, tyreWear)));
    }

    public int getGear() {
        return entityData.get(GEAR);
    }

    public int getRpm() {
        return entityData.get(RPM);
    }

    public float getSpeedKmh() {
        return entityData.get(SPEED);
    }

    public float getDamagePercent() {
        return entityData.get(DAMAGE);
    }

    public float getTyreWearPercent() {
        return entityData.get(TYRE_WEAR);
    }

    public int getCurrentLapTicks() {
        return entityData.get(CURRENT_LAP_TICKS);
    }

    public int getBestLapTicks() {
        return entityData.get(BEST_LAP_TICKS);
    }

    public boolean hasCheckpoint() {
        return entityData.get(CHECKPOINT_ARMED);
    }

    public void crossStartFinishLine(Direction markerFacing) {
        if (!isForwardPass(markerFacing)) {
            invalidateLap("reverse pass");
            return;
        }

        long gameTime = level().getGameTime();
        if (lapStartedAt >= 0L) {
            int lapTicks = Math.max(1, (int) (gameTime - lapStartedAt));
            int bestLapTicks = entityData.get(BEST_LAP_TICKS);
            entityData.set(CURRENT_LAP_TICKS, lapTicks);
            if (bestLapTicks == 0 || lapTicks < bestLapTicks) {
                entityData.set(BEST_LAP_TICKS, lapTicks);
            }
            messageDriver(Component.literal("Lap complete: " + formatLapTime(lapTicks)));
        } else {
            messageDriver(Component.literal("Lap started"));
        }

        lapStartedAt = gameTime;
        entityData.set(CHECKPOINT_ARMED, false);
        entityData.set(CURRENT_LAP_TICKS, 0);
    }

    public void crossCheckpoint(Direction markerFacing) {
        if (!isForwardPass(markerFacing)) {
            invalidateLap("reverse checkpoint pass");
            return;
        }
        if (lapStartedAt >= 0L && !entityData.get(CHECKPOINT_ARMED)) {
            entityData.set(CHECKPOINT_ARMED, true);
            messageDriver(Component.literal("Checkpoint"));
        }
    }

    public void shiftUp() {
        if (getGear() < 6) {
            entityData.set(GEAR, getGear() + 1);
            playShiftFeedback(1.1f);
        }
    }

    public void shiftDown() {
        if (getGear() > 1) {
            entityData.set(GEAR, getGear() - 1);
            playShiftFeedback(0.8f);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            tickLapTimer();
            tickMovement();
            tickImpactDamage();
            tickWarnings();
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (level().isClientSide()) {
            return getPassengers().isEmpty() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        }

        return getPassengers().isEmpty() && player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().isEmpty() && passenger instanceof Player;
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity passenger = getFirstPassenger();
        return passenger instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    @Override
    public void positionRider(Entity passenger, MoveFunction callback) {
        if (hasPassenger(passenger)) {
            Vec3 seat = SEAT_OFFSET.yRot((float) -Math.toRadians(getYRot()));
            callback.accept(passenger, getX() + seat.x, getY() + seat.y, getZ() + seat.z);
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        for (Vec3 offset : DISMOUNT_OFFSETS) {
            Vec3 location = position().add(offset.yRot((float) -Math.toRadians(getYRot())));
            if (level().noCollision(passenger, passenger.getBoundingBox().move(location.subtract(passenger.position())))) {
                return location;
            }
        }
        return super.getDismountLocationForPassenger(passenger);
    }

    @Override
    public boolean hurtServer(ServerLevel level, net.minecraft.world.damagesource.DamageSource damageSource, float amount) {
        addDamage(amount * 4.0f);
        if (getDamagePercent() >= 100.0f) {
            destroyIntoMaterials(level);
        }
        return true;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        setup = new PrototypeCarSetup(
            input.getIntOr("Power", PrototypeCarSetup.DEFAULT.power()),
            input.getIntOr("Grip", PrototypeCarSetup.DEFAULT.grip()),
            input.getIntOr("Aero", PrototypeCarSetup.DEFAULT.aero()),
            input.getIntOr("Gearing", PrototypeCarSetup.DEFAULT.gearing())
        );
        entityData.set(GEAR, input.getIntOr("Gear", 1));
        entityData.set(RPM, input.getIntOr("Rpm", 900));
        entityData.set(DAMAGE, (float) input.getDoubleOr("Damage", 0.0));
        entityData.set(TYRE_WEAR, (float) input.getDoubleOr("TyreWear", 0.0));
        entityData.set(CURRENT_LAP_TICKS, input.getIntOr("CurrentLapTicks", 0));
        entityData.set(BEST_LAP_TICKS, input.getIntOr("BestLapTicks", 0));
        entityData.set(CHECKPOINT_ARMED, input.getBooleanOr("CheckpointArmed", false));
        lapStartedAt = input.getLongOr("LapStartedAt", -1L);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("Power", setup.power());
        output.putInt("Grip", setup.grip());
        output.putInt("Aero", setup.aero());
        output.putInt("Gearing", setup.gearing());
        output.putInt("Gear", getGear());
        output.putInt("Rpm", getRpm());
        output.putDouble("Damage", getDamagePercent());
        output.putDouble("TyreWear", getTyreWearPercent());
        output.putInt("CurrentLapTicks", getCurrentLapTicks());
        output.putInt("BestLapTicks", getBestLapTicks());
        output.putBoolean("CheckpointArmed", hasCheckpoint());
        output.putLong("LapStartedAt", lapStartedAt);
    }

    private void tickLapTimer() {
        if (lapStartedAt >= 0L) {
            Entity passenger = getControllingPassenger();
            if (!(passenger instanceof Player player) || !player.isAlive()) {
                invalidateLap("driver left car");
                return;
            }
            if (!isOnTrackSurface()) {
                invalidateLap("four wheels off track");
                return;
            }
            entityData.set(CURRENT_LAP_TICKS, Math.max(0, (int) (level().getGameTime() - lapStartedAt)));
        }
    }

    private double getTrackSurfaceGrip() {
        Block block = level().getBlockState(blockPosition().below()).getBlock();
        if (isFastTrackBlock(block)) {
            return 1.18;
        }
        if (block == OWRBlocks.KERB.get()) {
            return 0.88;
        }
        return 0.74;
    }

    private double getTrackSurfaceDrag() {
        Block block = level().getBlockState(blockPosition().below()).getBlock();
        if (isFastTrackBlock(block)) {
            return 0.996;
        }
        if (block == OWRBlocks.KERB.get()) {
            return 0.990;
        }
        return 0.948;
    }

    private boolean isPitLane() {
        return level().getBlockState(blockPosition().below()).is(OWRBlocks.PIT_LANE.get());
    }

    private boolean isOnTrackSurface() {
        double yaw = Math.toRadians(getYRot());
        Vec3 forward = new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
        Vec3 right = new Vec3(forward.z, 0.0, -forward.x);
        for (double side : new double[] {-0.72, 0.72}) {
            for (double length : new double[] {-0.82, 0.82}) {
                BlockPos pos = BlockPos.containing(position().add(right.scale(side)).add(forward.scale(length))).below();
                Block block = level().getBlockState(pos).getBlock();
                if (isTrackBlock(block)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isTrackBlock(Block block) {
        return isFastTrackBlock(block) || block == OWRBlocks.KERB.get();
    }

    private static boolean isFastTrackBlock(Block block) {
        return block == OWRBlocks.ASPHALT_TRACK.get() || block == OWRBlocks.PIT_LANE.get() || block == OWRBlocks.START_FINISH.get() || block == OWRBlocks.CHECKPOINT.get();
    }

    private void tickMovement() {
        Entity passenger = getControllingPassenger();
        double throttle = 0.0;
        double steering = 0.0;
        double brake = 0.0;

        if (passenger instanceof Player player) {
            throttle = Math.max(0.0f, player.zza);
            brake = Math.max(0.0f, -player.zza);
            steering = player.xxa;
        }

        Vec3 delta = getDeltaMovement();
        double horizontalSpeed = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        double damageFactor = 1.0 - getDamagePercent() / 140.0;
        double tyreFactor = 1.0 - getTyreWearPercent() / 180.0;
        double gearRatio = GEAR_RATIOS[getGear()];
        double maxSpeed = BASE_MAX_SPEED * setup.gearingMultiplier() * setup.aeroMultiplier() * damageFactor;
        if (isPitLane()) {
            maxSpeed = Math.min(maxSpeed, 0.52);
        }
        double acceleration = BASE_ACCELERATION * setup.powerMultiplier() * setup.accelerationMultiplier() * gearRatio * damageFactor;
        double brakeForce = BASE_BRAKE_FORCE * tyreFactor * getTrackSurfaceGrip();

        if (throttle > 0.0 && horizontalSpeed < maxSpeed) {
            Vec3 forward = Vec3.directionFromRotation(0.0f, getYRot()).scale(acceleration * throttle);
            delta = delta.add(forward.x, 0.0, forward.z);
        }

        if (brake > 0.0) {
            delta = new Vec3(delta.x * (1.0 - brakeForce * brake), delta.y, delta.z * (1.0 - brakeForce * brake));
        }

        if (Math.abs(steering) > STEERING_DEADZONE && horizontalSpeed > MIN_STEERING_SPEED) {
            float speedFactor = (float) Math.min(1.0, horizontalSpeed / 0.65);
            float turn = (float) (-steering * (0.9 + 2.0 * tyreFactor * setup.gripMultiplier()) * speedFactor);
            setYRot(getYRot() + turn);
            double surfaceWear = level().getBlockState(blockPosition().below()).is(OWRBlocks.KERB.get()) ? 1.8 : 1.0;
            addTyreWear((float) (Math.abs(steering) * horizontalSpeed * 0.012 * setup.tyreWearMultiplier() * surfaceWear));
        }

        if (horizontalSpeed > 0.01) {
            Vec3 forward = Vec3.directionFromRotation(0.0f, getYRot());
            double forwardSpeed = delta.x * forward.x + delta.z * forward.z;
            double sideX = delta.x - forward.x * forwardSpeed;
            double sideZ = delta.z - forward.z * forwardSpeed;
            double surfaceGrip = getTrackSurfaceGrip();
            double grip = (0.35 + 0.45 * tyreFactor * setup.gripMultiplier()) * surfaceGrip;
            grip = Math.min(grip, 1.0);
            delta = new Vec3(forward.x * forwardSpeed + sideX * (1.0 - grip), delta.y, forward.z * forwardSpeed + sideZ * (1.0 - grip));
        }

        double drag = AIR_DRAG * setup.dragMultiplier() * getTrackSurfaceDrag();
        delta = new Vec3(delta.x * drag, delta.y - 0.04, delta.z * drag);
        if (onGround()) {
            delta = new Vec3(delta.x * GROUND_DRAG, 0.0, delta.z * GROUND_DRAG);
        }

        setDeltaMovement(delta);
        move(MoverType.SELF, getDeltaMovement());

        double newSpeed = Math.sqrt(getDeltaMovement().x * getDeltaMovement().x + getDeltaMovement().z * getDeltaMovement().z);
        entityData.set(SPEED, (float) (newSpeed * 72.0));
        entityData.set(RPM, Math.max(900, Math.min(12500, (int) (900 + newSpeed * 7800 / Math.max(0.22, gearRatio)))));
        previousHorizontalSpeed = horizontalSpeed;
    }

    private void tickImpactDamage() {
        if (horizontalCollision && previousHorizontalSpeed > 0.28) {
            boolean barrierImpact = isTouchingBarrier();
            float severity = (float) ((previousHorizontalSpeed - 0.28) * (barrierImpact ? 18.0 : 40.0));
            addDamage(severity);
            playImpactFeedback(severity);
            setDeltaMovement(getDeltaMovement().scale(barrierImpact ? 0.05 : 0.15));

            Entity passenger = getControllingPassenger();
            if (passenger instanceof Player player) {
                player.hurt(damageSources().flyIntoWall(), Math.max(1.0f, severity * 0.35f));
            }
            if (getDamagePercent() >= 100.0f && level() instanceof ServerLevel serverLevel) {
                destroyIntoMaterials(serverLevel);
            }
        }
    }

    private void tickWarnings() {
        long time = level().getGameTime();
        if (getTyreWearPercent() >= 70.0f && time - lastLowTyreWarningAt > 100L) {
            lastLowTyreWarningAt = time;
            messageDriver(Component.literal("Tyre condition low"));
        }
        if (getDamagePercent() >= 70.0f && time - lastDamageWarningAt > 100L) {
            lastDamageWarningAt = time;
            messageDriver(Component.literal("Car damage critical"));
        }
    }

    private void invalidateLap(String reason) {
        if (lapStartedAt >= 0L) {
            lapStartedAt = -1L;
            entityData.set(CURRENT_LAP_TICKS, 0);
            entityData.set(CHECKPOINT_ARMED, false);
            messageDriver(Component.literal("INVALID LAP: " + reason));
        }
    }

    private boolean isForwardPass(Direction markerFacing) {
        Vec3 carForward = Vec3.directionFromRotation(0.0f, getYRot());
        Vec3 markerForward = new Vec3(markerFacing.getStepX(), 0.0, markerFacing.getStepZ());
        return carForward.dot(markerForward) > 0.35;
    }

    private boolean isTouchingBarrier() {
        for (BlockPos pos : BlockPos.betweenClosed(blockPosition().offset(-1, 0, -1), blockPosition().offset(1, 1, 1))) {
            if (level().getBlockState(pos).is(OWRBlocks.BARRIER.get())) {
                return true;
            }
        }
        return false;
    }

    private void destroyIntoMaterials(ServerLevel serverLevel) {
        spawnAtLocation(serverLevel, new ItemStack(OWRItems.CHASSIS.get()));
        spawnAtLocation(serverLevel, new ItemStack(OWRItems.ENGINE.get()));
        spawnAtLocation(serverLevel, new ItemStack(OWRItems.GEARBOX.get()));
        spawnAtLocation(serverLevel, new ItemStack(OWRItems.RUBBER.get(), Math.max(1, 4 - Math.round(getTyreWearPercent() / 25.0f))));
        serverLevel.explode(this, getX(), getY(), getZ(), 1.8f, Level.ExplosionInteraction.NONE);
        discard();
    }

    private void playShiftFeedback(float pitch) {
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON, SoundSource.PLAYERS, 0.35f, pitch);
    }

    private void messageDriver(Component message) {
        Entity passenger = getControllingPassenger();
        if (passenger instanceof Player player) {
            player.displayClientMessage(message, true);
        }
    }

    private static String formatLapTime(int ticks) {
        int totalCentiseconds = ticks * 5;
        int minutes = totalCentiseconds / 6000;
        int seconds = totalCentiseconds / 100 % 60;
        int centiseconds = totalCentiseconds % 100;
        return String.format("%d:%02d.%02d", minutes, seconds, centiseconds);
    }

    private void playImpactFeedback(float severity) {
        if (level() instanceof ServerLevel serverLevel) {
            float volume = Math.min(1.8f, 0.5f + severity * 0.08f);
            float pitch = Math.max(0.55f, 1.2f - severity * 0.04f);
            serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.METAL_HIT, SoundSource.BLOCKS, volume, pitch);
            serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.35, getZ(), Math.min(18, 4 + (int) severity), 0.35, 0.18, 0.35, 0.03);
            serverLevel.sendParticles(ParticleTypes.CRIT, getX(), getY() + 0.35, getZ(), Math.min(12, 2 + (int) (severity * 0.5f)), 0.25, 0.12, 0.25, 0.15);
        }
    }

    private void addDamage(float amount) {
        entityData.set(DAMAGE, Math.min(100.0f, getDamagePercent() + amount));
    }

    private void addTyreWear(float amount) {
        entityData.set(TYRE_WEAR, Math.min(100.0f, getTyreWearPercent() + amount));
    }
}
