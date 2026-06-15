package com.openwheelracing.content.entity;

import com.mojang.logging.LogUtils;
import com.openwheelracing.content.car.CarLivery;
import com.openwheelracing.content.car.PrototypeCarSetup;
import com.openwheelracing.content.item.PrototypeCarItem;
import com.openwheelracing.content.race.OWRLapRecords;
import com.openwheelracing.registry.OWRBlocks;
import com.openwheelracing.registry.OWRItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class OpenwheelCarEntity extends Entity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final EntityDataAccessor<Integer> GEAR = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RPM = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SPEED = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TYRE_WEAR = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TYRE_SLIP = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> CURRENT_LAP_TICKS = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BEST_LAP_TICKS = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CHECKPOINT_ARMED = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> PIT_STOP_TICKS = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ABS_ENABLED = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> LIVERY = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.INT);

    private static final int PIT_STOP_DURATION = 60; // 3 seconds
    private static final int PIT_RUBBER_COST = 2;    // rubber items consumed per stop

    // Seat offset: eye height = car Y + (-0.62) + player eye height (1.62) ≈ 1.0 above ground
    private static final Vec3 SEAT_OFFSET = new Vec3(0.0, -0.62, 0.1);
    private static final Vec3[] DISMOUNT_OFFSETS = {
        new Vec3(1.1, 0.0, 0.15),
        new Vec3(-1.1, 0.0, 0.15),
        new Vec3(0.0, 0.0, 1.25),
        new Vec3(0.0, 0.0, -1.65)
    };

    private static final int MAX_GEAR = 8;
    private static final double SPEED_TO_BLOCKS_PER_TICK = 1.0 / 72.0;
    private static final double[] GEAR_TOP_SPEEDS = {0.0, 80.0 * SPEED_TO_BLOCKS_PER_TICK, 120.0 * SPEED_TO_BLOCKS_PER_TICK, 150.0 * SPEED_TO_BLOCKS_PER_TICK, 190.0 * SPEED_TO_BLOCKS_PER_TICK, 235.0 * SPEED_TO_BLOCKS_PER_TICK, 275.0 * SPEED_TO_BLOCKS_PER_TICK, 310.0 * SPEED_TO_BLOCKS_PER_TICK, 350.0 * SPEED_TO_BLOCKS_PER_TICK};
    private static final double CAR_MASS_KG = 805.0;
    private static final double GRAVITY = 9.81;
    private static final double PHYSICS_DT = 1.0 / 20.0;
    private static final int PHYSICS_SUBSTEPS = 4;
    private static final double WHEELBASE = 3.60;
    private static final double FRONT_AXLE_DISTANCE = WHEELBASE * (1.0 - 0.46);
    private static final double REAR_AXLE_DISTANCE = WHEELBASE * 0.46;
    private static final double YAW_INERTIA = 1100.0;
    private static final double FRONT_STATIC_WEIGHT = 0.46;
    private static final double CG_HEIGHT = 0.27;
    private static final double AIR_DENSITY = 1.225;
    private static final double DRAG_AREA = 1.45;
    private static final double DOWNFORCE_AREA = 7.0;
    private static final double FRONT_AERO_BALANCE = 0.43;
    private static final double ROLLING_RESISTANCE = 0.012;
    private static final double ASPHALT_MU_LATERAL = 2.25;
    private static final double ASPHALT_MU_LONGITUDINAL = 2.30;
    private static final double KINETIC_MU_RATIO = 0.94;
    private static final double LOAD_SENSITIVITY = 0.035;
    private static final double FRONT_CORNERING_STIFFNESS = 210_000.0;
    private static final double REAR_CORNERING_STIFFNESS = 285_000.0;
    private static final double MAX_BRAKE_FORCE = 40_000.0;
    private static final double BRAKE_FRONT_BIAS = 0.58;
    private static final double MIN_POWER_SPEED = 5.0;
    private static final double PEAK_POWER_WATTS = 780_000.0;
    private static final double IDLE_RPM = 900.0;
    private static final double LAUNCH_RPM = 4000.0;
    private static final double LAUNCH_CLUTCH_SPEED = 0.42;
    private static final double REDLINE_RPM = 13000.0;
    private static final double STEERING_DEADZONE = 0.08;
    private static final double LOW_SPEED_STEER_ANGLE = Math.toRadians(24.0);
    private static final double HIGH_SPEED_STEER_ANGLE = Math.toRadians(2.2);
    private static final double LOW_SPEED_STEERING_RACK_RATE = Math.toRadians(120.0);
    private static final double HIGH_SPEED_STEERING_RACK_RATE = Math.toRadians(12.0);
    private static final double STEERING_SPEED_SCALE = 20.0;
    private static final double TRACTION_CONTROL_SLIP_TARGET = 0.92;
    private static final double SLIP_ANGLE_DEADBAND = Math.toRadians(0.15);
    private static final double FRONT_TYRE_RELAXATION_LENGTH = 0.42;
    private static final double REAR_TYRE_RELAXATION_LENGTH = 0.45;
    private static final double[] TRACK_WHEEL_SIDE_OFFSETS = {-1.34, 1.34};
    private static final double[] TRACK_WHEEL_LENGTH_OFFSETS = {-2.95, 1.55};
    private static final double[] TRACK_PATCH_SIDE_OFFSETS = {-0.18, 0.0, 0.18};
    private static final double[] TRACK_PATCH_LENGTH_OFFSETS = {-0.32, 0.0, 0.32};
    private PrototypeCarSetup setup = PrototypeCarSetup.DEFAULT;
    private double previousHorizontalSpeed;
    private long lapStartedAt = -1L;
    private long lastStartFinishMarker;
    private long lastStartFinishTriggerAt = -20L;
    private long lastLowTyreWarningAt = -200L;
    private long lastDamageWarningAt = -200L;
    // Checkpoint positions (packed BlockPos longs) visited in the current lap, in order
    private final java.util.LinkedList<Long> visitedCheckpoints = new java.util.LinkedList<>();
    private final java.util.HashSet<Long> visitedCheckpointSet = new java.util.HashSet<>();
    // Last driver input received from client; cleared each tick after use
    private float inputThrottle;
    private float inputBrake;
    private float inputSteering;
    private long lastMovementDebugAt = -20L;
    private boolean wasRiddenLastTick;
    private double steeringAngle;
    private double yawRate;
    private double relaxedFrontLatForce;
    private double relaxedRearLatForce;
    private double debugVelocityLong;
    private double debugVelocityLat;
    private double debugDriveForce;
    private double debugDragForce;
    private double debugFrontLatForce;
    private double debugRearLatForce;
    private double debugFrontLongForce;
    private double debugRearLongForce;
    private double debugFrontLoad;
    private double debugRearLoad;
    private double debugFrontDemand;
    private double debugRearDemand;
    private double debugFrontSlipAngle;
    private double debugRearSlipAngle;
    private double debugDownforce;

    public void applyDriveInput(float throttle, float brake, float steering) {
        this.inputThrottle = throttle;
        this.inputBrake = brake;
        this.inputSteering = steering;
        if (!level().isClientSide() && level().getGameTime() - lastMovementDebugAt >= 20L) {
            lastMovementDebugAt = level().getGameTime();
            LOGGER.info("OWR car input id={} pos=({}, {}, {}) passenger={} throttle={} brake={} steering={} delta={}",
                getId(),
                String.format("%.3f", getX()),
                String.format("%.3f", getY()),
                String.format("%.3f", getZ()),
                getControllingPassenger() == null ? "none" : getControllingPassenger().getScoreboardName(),
                throttle,
                brake,
                steering,
                formatVec(getDeltaMovement()));
        }
    }

    public OpenwheelCarEntity(EntityType<? extends OpenwheelCarEntity> entityType, Level level) {
        super(entityType, level);
        blocksBuilding = false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(GEAR, 1);
        builder.define(RPM, 900);
        builder.define(SPEED, 0.0f);
        builder.define(DAMAGE, 0.0f);
        builder.define(TYRE_WEAR, 0.0f);
        builder.define(TYRE_SLIP, 0.0f);
        builder.define(CURRENT_LAP_TICKS, 0);
        builder.define(BEST_LAP_TICKS, 0);
        builder.define(CHECKPOINT_ARMED, false);
        builder.define(PIT_STOP_TICKS, 0);
        builder.define(ABS_ENABLED, true);
        builder.define(LIVERY, 0);
    }

    @Override
    public float maxUpStep() {
        return 0.5f;
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

    public void setLivery(int livery) {
        entityData.set(LIVERY, Math.max(0, Math.min(CarLivery.count() - 1, livery)));
    }

    public int getLivery() {
        return entityData.get(LIVERY);
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

    public float getTyreSlipIntensity() {
        return entityData.get(TYRE_SLIP);
    }

    public boolean isAbsEnabled() {
        return entityData.get(ABS_ENABLED);
    }

    public void setAbsEnabled(boolean enabled) {
        entityData.set(ABS_ENABLED, enabled);
    }

    public void toggleAbs() {
        setAbsEnabled(!isAbsEnabled());
    }

    public float getFrontWheelSteerDegrees() {
        return (float) Math.toDegrees(steeringAngle);
    }

    public double getDebugVelocityLong() { return debugVelocityLong; }
    public double getDebugVelocityLat() { return debugVelocityLat; }
    public double getDebugYawRate() { return yawRate; }
    public double getDebugDriveForce() { return debugDriveForce; }
    public double getDebugDragForce() { return debugDragForce; }
    public double getDebugFrontLatForce() { return debugFrontLatForce; }
    public double getDebugRearLatForce() { return debugRearLatForce; }
    public double getDebugFrontLongForce() { return debugFrontLongForce; }
    public double getDebugRearLongForce() { return debugRearLongForce; }
    public double getDebugFrontLoad() { return debugFrontLoad; }
    public double getDebugRearLoad() { return debugRearLoad; }
    public double getDebugFrontDemand() { return debugFrontDemand; }
    public double getDebugRearDemand() { return debugRearDemand; }
    public double getDebugFrontSlipAngleDegrees() { return Math.toDegrees(debugFrontSlipAngle); }
    public double getDebugRearSlipAngleDegrees() { return Math.toDegrees(debugRearSlipAngle); }
    public double getDebugDownforce() { return debugDownforce; }

    public Vec3 getWheelSoundPosition(double sideOffset, double lengthOffset) {
        double yaw = Math.toRadians(getYRot());
        Vec3 forward = new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
        Vec3 right = new Vec3(forward.z, 0.0, -forward.x);
        return position().add(right.scale(sideOffset)).add(forward.scale(lengthOffset)).add(0.0, 0.25, 0.0);
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

    public boolean isInPitStop() {
        return entityData.get(PIT_STOP_TICKS) > 0;
    }

    public int getPitStopTicks() {
        return entityData.get(PIT_STOP_TICKS);
    }

    public boolean tryStartPitStop(Player player) {
        if (!isPitLane()) {
            messageDriver(Component.literal("Pit stop only available in pit lane"));
            return false;
        }
        double speed = Math.sqrt(getDeltaMovement().x * getDeltaMovement().x + getDeltaMovement().z * getDeltaMovement().z);
        if (speed > 0.05) {
            messageDriver(Component.literal("Come to a stop before pit service"));
            return false;
        }
        if (isInPitStop()) {
            return false;
        }
        int rubberAvailable = player.getInventory().countItem(OWRItems.RUBBER.get());
        if (rubberAvailable < PIT_RUBBER_COST) {
            messageDriver(Component.literal("Need " + PIT_RUBBER_COST + " rubber for pit stop"));
            return false;
        }
        player.getInventory().clearOrCountMatchingItems(item -> item.is(OWRItems.RUBBER.get()), PIT_RUBBER_COST, player.inventoryMenu.getCraftSlots());
        entityData.set(PIT_STOP_TICKS, PIT_STOP_DURATION);
        messageDriver(Component.literal("Pit stop: servicing..."));
        return true;
    }

    public void crossStartFinishLine(BlockPos pos, Direction markerFacing) {
        long packed = pos.asLong();
        if (packed == lastStartFinishMarker && level().getGameTime() == lastStartFinishTriggerAt) {
            return;
        }
        lastStartFinishMarker = packed;
        lastStartFinishTriggerAt = level().getGameTime();
        if (!isForwardPass(markerFacing)) {
            invalidateLap("reverse pass");
            return;
        }

        long gameTime = level().getGameTime();
        if (lapStartedAt >= 0L) {
            completeLap(gameTime);
        } else {
            messageDriver(Component.literal("Lap started"));
        }

        startLap(gameTime, null);
    }

    public void crossCheckpoint(BlockPos pos, Direction markerFacing) {
        if (!isForwardPass(markerFacing)) {
            invalidateLap("reverse checkpoint pass");
            return;
        }
        if (lapStartedAt < 0L) {
            return;
        }
        long packed = pos.asLong();
        if (!visitedCheckpointSet.add(packed)) {
            return;
        }
        visitedCheckpoints.add(packed);
        entityData.set(CHECKPOINT_ARMED, true);
        messageDriver(Component.literal("CP " + visitedCheckpoints.size()));
    }

    private void startLap(long gameTime, @Nullable Component message) {
        lapStartedAt = gameTime;
        resetLapProgress();
        if (message != null) {
            messageDriver(message);
        }
    }

    private void completeLap(long gameTime) {
        int lapTicks = Math.max(1, (int)(gameTime - lapStartedAt));
        entityData.set(CURRENT_LAP_TICKS, lapTicks);
        boolean personalBest = updatePlayerBestLap(lapTicks);
        messageDriver(Component.literal("Lap: " + formatLapTime(lapTicks)
            + " | CPs: " + visitedCheckpoints.size()
            + (personalBest ? " | Personal best" : "")));
    }

    private void resetLapProgress() {
        visitedCheckpoints.clear();
        visitedCheckpointSet.clear();
        entityData.set(CHECKPOINT_ARMED, false);
        entityData.set(CURRENT_LAP_TICKS, 0);
    }

    public void shiftUp() {
        if (getGear() < MAX_GEAR) {
            entityData.set(GEAR, getGear() + 1);
            playShiftFeedback(1.1f);
            messageDriver(Component.literal("Gear " + getGear()));
            logShift("up");
        }
    }

    public void shiftDown() {
        if (getGear() > 1) {
            entityData.set(GEAR, getGear() - 1);
            playShiftFeedback(0.8f);
            messageDriver(Component.literal("Gear " + getGear()));
            logShift("down");
        }
    }

    public void shiftLocal(int direction) {
        if (direction > 0 && getGear() < MAX_GEAR) {
            entityData.set(GEAR, getGear() + 1);
        } else if (direction < 0 && getGear() > 1) {
            entityData.set(GEAR, getGear() - 1);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            boolean ridden = getControllingPassenger() != null;
            if (wasRiddenLastTick && !ridden) {
                LOGGER.info("OWR car dismounted id={} pos=({}, {}, {}) delta={} speedKmh={} horizontalCollision={} verticalCollision={} onGround={}",
                    getId(),
                    String.format("%.3f", getX()),
                    String.format("%.3f", getY()),
                    String.format("%.3f", getZ()),
                    formatVec(getDeltaMovement()),
                    String.format("%.2f", getSpeedKmh()),
                    horizontalCollision,
                    verticalCollision,
                    onGround());
            }
            wasRiddenLastTick = ridden;
            tickLapTimer();
            tickPitStop();
            tickMovement(true);
            tickImpactDamage();
            tickWarnings();
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (level().isClientSide()) {
            return InteractionResult.PASS;
        }

        // Sneak + empty hand on empty car → pick up as item
        if (getPassengers().isEmpty() && player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()) {
            ItemStack item = PrototypeCarItem.create(setup, getDamagePercent(), getTyreWearPercent(), getLivery());
            if (!player.addItem(item)) {
                player.drop(item, false);
            }
            discard();
            return InteractionResult.CONSUME;
        }

        // Seated driver sneak-right-clicks to request pit stop
        if (hasPassenger(player) && player.isShiftKeyDown()) {
            tryStartPitStop(player);
            return InteractionResult.CONSUME;
        }

        if (getPassengers().isEmpty() && player.startRiding(this)) {
            prepareForDriver(player);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
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
            double riderX = getX() + seat.x;
            double riderY = getY() + seat.y;
            double riderZ = getZ() + seat.z;
            if (!level().isClientSide() && level().getGameTime() - lastMovementDebugAt >= 20L) {
                LOGGER.info("OWR car rider id={} carPos=({}, {}, {}) riderPos=({}, {}, {}) seat={} carDelta={}",
                    getId(),
                    String.format("%.3f", getX()),
                    String.format("%.3f", getY()),
                    String.format("%.3f", getZ()),
                    String.format("%.3f", riderX),
                    String.format("%.3f", riderY),
                    String.format("%.3f", riderZ),
                    formatVec(seat),
                    formatVec(getDeltaMovement()));
            }
            callback.accept(passenger, riderX, riderY, riderZ);
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
        setLivery(input.getIntOr("Livery", 0));
        entityData.set(CURRENT_LAP_TICKS, input.getIntOr("CurrentLapTicks", 0));
        entityData.set(BEST_LAP_TICKS, input.getIntOr("BestLapTicks", 0));
        entityData.set(CHECKPOINT_ARMED, input.getBooleanOr("CheckpointArmed", false));
        steeringAngle = input.getDoubleOr("SteeringAngle", 0.0);
        yawRate = input.getDoubleOr("YawRate", 0.0);
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
        output.putInt("Livery", getLivery());
        output.putInt("CurrentLapTicks", getCurrentLapTicks());
        output.putInt("BestLapTicks", getBestLapTicks());
        output.putBoolean("CheckpointArmed", hasCheckpoint());
        output.putDouble("SteeringAngle", steeringAngle);
        output.putDouble("YawRate", yawRate);
        output.putLong("LapStartedAt", lapStartedAt);
    }

    private void tickPitStop() {
        int ticks = entityData.get(PIT_STOP_TICKS);
        if (ticks <= 0) {
            return;
        }
        // Block inputs during service
        inputThrottle = 0;
        inputBrake = 0;
        inputSteering = 0;

        ticks--;
        entityData.set(PIT_STOP_TICKS, ticks);

        if (ticks == 0) {
            entityData.set(DAMAGE, 0.0f);
            entityData.set(TYRE_WEAR, 0.0f);
            messageDriver(Component.literal("Pit stop complete — car serviced"));
        }
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

    // ── Surface profiles ──────────────────────────────────────────────────────
    private enum SurfaceProfile {
        //                          grip   drag   sinkDrag  wearMult  lapValid
        ASPHALT(                    1.00,  0.997,  0.00,     1.0,     true),
        CONCRETE(                   0.92,  0.995,  0.00,     1.1,     true),
        KERB(                       0.78,  0.991,  0.01,     1.8,     true),
        PIT_LANE(                   0.95,  0.996,  0.00,     0.6,     true),
        DIRT(                       0.58,  0.952,  0.05,     1.4,     false),
        GRASS(                      0.42,  0.930,  0.08,     1.6,     false),
        GRAVEL(                     0.45,  0.940,  0.16,     2.0,     false),
        SAND(                       0.28,  0.900,  0.28,     2.4,     false);

        final double grip;
        final double drag;
        final double sinkDrag;
        final double wearMult;
        final boolean countsAsTrack;

        SurfaceProfile(double grip, double drag, double sinkDrag, double wearMult, boolean countsAsTrack) {
            this.grip           = grip;
            this.drag           = drag;
            this.sinkDrag       = sinkDrag;
            this.wearMult       = wearMult;
            this.countsAsTrack  = countsAsTrack;
        }
    }

    private SurfaceProfile getSurfaceAt(Vec3 pos) {
        BlockPos basePos = BlockPos.containing(pos.x, getBoundingBox().minY - 0.05, pos.z);
        Block block = level().getBlockState(basePos).getBlock();
        if (block == OWRBlocks.ASPHALT_TRACK.get()
                || block == OWRBlocks.ASPHALT_TRACK_SLAB.get()
                || block == OWRBlocks.START_FINISH.get()
                || block == OWRBlocks.CHECKPOINT.get()) return SurfaceProfile.ASPHALT;
        if (block == OWRBlocks.PIT_LANE.get()
                || block == OWRBlocks.PIT_LANE_SLAB.get()) return SurfaceProfile.PIT_LANE;
        if (block == OWRBlocks.KERB.get()) return SurfaceProfile.KERB;
        if (isConcreteBlock(block)) return SurfaceProfile.CONCRETE;
        if (block == Blocks.GRASS_BLOCK
                || block == Blocks.DIRT_PATH
                || block == Blocks.PODZOL
                || block == Blocks.MYCELIUM
                || block == Blocks.MOSS_BLOCK) return SurfaceProfile.GRASS;
        if (block == Blocks.GRAVEL) return SurfaceProfile.GRAVEL;
        if (block == Blocks.SAND
                || block == Blocks.RED_SAND
                || block == Blocks.SUSPICIOUS_SAND) return SurfaceProfile.SAND;
        return SurfaceProfile.DIRT;
    }

    private static boolean isConcreteBlock(Block block) {
        return block == Blocks.WHITE_CONCRETE
                || block == Blocks.ORANGE_CONCRETE
                || block == Blocks.MAGENTA_CONCRETE
                || block == Blocks.LIGHT_BLUE_CONCRETE
                || block == Blocks.YELLOW_CONCRETE
                || block == Blocks.LIME_CONCRETE
                || block == Blocks.PINK_CONCRETE
                || block == Blocks.GRAY_CONCRETE
                || block == Blocks.LIGHT_GRAY_CONCRETE
                || block == Blocks.CYAN_CONCRETE
                || block == Blocks.PURPLE_CONCRETE
                || block == Blocks.BLUE_CONCRETE
                || block == Blocks.BROWN_CONCRETE
                || block == Blocks.GREEN_CONCRETE
                || block == Blocks.RED_CONCRETE
                || block == Blocks.BLACK_CONCRETE;
    }

    private SurfaceProfile getCurrentSurface() {
        return getSurfaceAt(position());
    }

    private boolean isPitLane() {
        return getCurrentSurface() == SurfaceProfile.PIT_LANE;
    }

    private boolean isOnTrackSurface() {
        double yaw = Math.toRadians(getYRot());
        Vec3 forward = new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
        Vec3 right = new Vec3(forward.z, 0.0, -forward.x);
        for (double side : TRACK_WHEEL_SIDE_OFFSETS) {
            for (double length : TRACK_WHEEL_LENGTH_OFFSETS) {
                if (!wheelPatchTouchesTrack(forward, right, side, length)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean wheelPatchTouchesTrack(Vec3 forward, Vec3 right, double wheelSide, double wheelLength) {
        for (double sidePatch : TRACK_PATCH_SIDE_OFFSETS) {
            for (double lengthPatch : TRACK_PATCH_LENGTH_OFFSETS) {
                Vec3 samplePos = position()
                    .add(right.scale(wheelSide + sidePatch))
                    .add(forward.scale(wheelLength + lengthPatch));
                if (getSurfaceAt(samplePos).countsAsTrack) {
                    return true;
                }
            }
        }
        return false;
    }

    private void scanLapMarkers() {
        double yaw = Math.toRadians(getYRot());
        Vec3 forward = new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
        Vec3 right = new Vec3(forward.z, 0.0, -forward.x);
        for (double side : TRACK_WHEEL_SIDE_OFFSETS) {
            for (double length : TRACK_WHEEL_LENGTH_OFFSETS) {
                for (double sidePatch : TRACK_PATCH_SIDE_OFFSETS) {
                    for (double lengthPatch : TRACK_PATCH_LENGTH_OFFSETS) {
                        Vec3 samplePos = position()
                            .add(right.scale(side + sidePatch))
                            .add(forward.scale(length + lengthPatch));
                        BlockPos pos = BlockPos.containing(samplePos.x, getBoundingBox().minY - 0.05, samplePos.z);
                        Block block = level().getBlockState(pos).getBlock();
                        if (block == OWRBlocks.START_FINISH.get()) {
                            crossStartFinishLine(pos, level().getBlockState(pos).getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING));
                            return;
                        }
                        if (block == OWRBlocks.CHECKPOINT.get()) {
                            crossCheckpoint(pos, level().getBlockState(pos).getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING));
                            return;
                        }
                    }
                }
            }
        }
    }


    public void tickLocalClientMovement(float throttle, float brake, float steering) {
        Entity passenger = getControllingPassenger();
        if (level().isClientSide() && passenger != null) {
            float previousYaw = getYRot();
            tickMovement(throttle, brake, steering, false);
            float yawDelta = getYRot() - previousYaw;
            positionRider(passenger);
            passenger.setYRot(passenger.getYRot() + yawDelta);
            passenger.setYHeadRot(passenger.getYRot());
            passenger.setYBodyRot(passenger.getYRot());
        }
    }

    private void tickMovement(boolean debugMovement) {
        double throttle = inputThrottle;
        double steering = inputSteering;
        double brake = inputBrake;
        inputThrottle = 0;
        inputBrake = 0;
        inputSteering = 0;
        tickMovement(throttle, brake, steering, debugMovement);
    }

    private void tickMovement(double throttle, double brake, double steering, boolean debugMovement) {

        Vec3 delta = getDeltaMovement();
        double horizontalSpeed = Math.sqrt(delta.x * delta.x + delta.z * delta.z);

        double damageFactor    = 1.0 - getDamagePercent()   / 140.0;
        double tyreFactor      = 1.0 - getTyreWearPercent() / 180.0;
        int gear = Math.max(1, Math.min(MAX_GEAR, getGear()));
        if (gear != getGear()) {
            entityData.set(GEAR, gear);
        }
        double gearTopSpeed = GEAR_TOP_SPEEDS[gear];
        double maxSpeed = GEAR_TOP_SPEEDS[MAX_GEAR];
        if (isPitLane()) {
            gearTopSpeed = Math.min(gearTopSpeed, 0.52);
            maxSpeed = Math.min(maxSpeed, 0.52);
        }

        SurfaceProfile surface = getCurrentSurface();
        Vec3 forward = Vec3.directionFromRotation(0.0f, getYRot());
        Vec3 right = new Vec3(forward.z, 0.0, -forward.x);
        double velocityLong = (delta.x * forward.x + delta.z * forward.z) * 20.0;
        double velocityLat = (delta.x * right.x + delta.z * right.z) * 20.0;
        double speedMetersPerSecond = Math.sqrt(velocityLong * velocityLong + velocityLat * velocityLat);
        if (speedMetersPerSecond < 0.35 && throttle == 0.0 && brake == 0.0) {
            velocityLat = 0.0;
            yawRate = 0.0;
            resetTyreRelaxation();
        }
        boolean autoClutch = throttle > 0.0 && gear == 1 && horizontalSpeed < LAUNCH_CLUTCH_SPEED;
        int engineRpm = calculateRpm(horizontalSpeed, gear, gearTopSpeed, autoClutch);
        double power = enginePowerWatts(engineRpm) * setup.powerMultiplier() * damageFactor;
        double tyreSlip = 0.0;

        double steerInput = Math.abs(steering) > STEERING_DEADZONE ? steering : 0.0;
        double speedRatio = speedMetersPerSecond / STEERING_SPEED_SCALE;
        double speedSteerT = square(speedRatio) / (1.0 + square(speedRatio));
        double steeringLock = LOW_SPEED_STEER_ANGLE + (HIGH_SPEED_STEER_ANGLE - LOW_SPEED_STEER_ANGLE) * speedSteerT;
        double rackRate = LOW_SPEED_STEERING_RACK_RATE + (HIGH_SPEED_STEERING_RACK_RATE - LOW_SPEED_STEERING_RACK_RATE) * speedSteerT;
        double targetSteeringAngle = steerInput * steeringLock;
        double maxSteeringStep = rackRate * PHYSICS_DT;
        steeringAngle += clamp(targetSteeringAngle - steeringAngle, -maxSteeringStep, maxSteeringStep);

        double downforce = 0.5 * AIR_DENSITY * DOWNFORCE_AREA * speedMetersPerSecond * speedMetersPerSecond;
        double aeroDrag = 0.5 * AIR_DENSITY * DRAG_AREA * speedMetersPerSecond * speedMetersPerSecond;
        double staticFrontLoad = CAR_MASS_KG * GRAVITY * FRONT_STATIC_WEIGHT;
        double staticRearLoad = CAR_MASS_KG * GRAVITY * (1.0 - FRONT_STATIC_WEIGHT);
        double aeroFrontLoad = downforce * FRONT_AERO_BALANCE;
        double aeroRearLoad = downforce * (1.0 - FRONT_AERO_BALANCE);

        double driveForceRequest = throttle > 0.0 && horizontalSpeed < gearTopSpeed
            ? power * throttle / Math.max(MIN_POWER_SPEED, Math.abs(velocityLong))
            : 0.0;
        double forwardRollingFraction = Math.abs(velocityLong) / Math.max(1.0, speedMetersPerSecond);
        if (speedMetersPerSecond > 3.0 && forwardRollingFraction < 0.45) {
            driveForceRequest *= forwardRollingFraction / 0.45;
        }
        if (autoClutch) {
            double staticRearTraction = ASPHALT_MU_LONGITUDINAL * surface.grip * staticRearLoad;
            driveForceRequest = Math.min(driveForceRequest, staticRearTraction * 0.72);
        }
        double brakeForceEstimate = brake * Math.min(MAX_BRAKE_FORCE, ASPHALT_MU_LONGITUDINAL * surface.grip * (CAR_MASS_KG * GRAVITY + downforce));
        if (isPitLane() && horizontalSpeed >= maxSpeed) {
            driveForceRequest = 0.0;
            brakeForceEstimate = Math.max(brakeForceEstimate, 6_000.0);
        }
        double rollingForce = ROLLING_RESISTANCE * (CAR_MASS_KG * GRAVITY + downforce);
        double sinkDragForce = surface.sinkDrag * (CAR_MASS_KG * GRAVITY + downforce);
        double preliminaryAx = (driveForceRequest - brakeForceEstimate - Math.signum(velocityLong) * (aeroDrag + rollingForce + sinkDragForce)) / CAR_MASS_KG;
        double loadTransfer = CAR_MASS_KG * preliminaryAx * CG_HEIGHT / WHEELBASE;
        double normalFront = Math.max(150.0, staticFrontLoad + aeroFrontLoad - loadTransfer);
        double normalRear = Math.max(150.0, staticRearLoad + aeroRearLoad + loadTransfer);
        double referenceFrontLoad = CAR_MASS_KG * GRAVITY * FRONT_STATIC_WEIGHT;
        double referenceRearLoad = CAR_MASS_KG * GRAVITY * (1.0 - FRONT_STATIC_WEIGHT);
        double surfaceMuLat = ASPHALT_MU_LATERAL * surface.grip;
        double surfaceMuLong = ASPHALT_MU_LONGITUDINAL * surface.grip;
        double tyreWearGrip = Math.max(0.45, tyreFactor);
        double muLatFront = loadSensitiveMu(surfaceMuLat * tyreWearGrip, normalFront, referenceFrontLoad);
        double muLatRear = loadSensitiveMu(surfaceMuLat * tyreWearGrip, normalRear, referenceRearLoad);
        double muLongFront = loadSensitiveMu(surfaceMuLong * tyreWearGrip, normalFront, referenceFrontLoad);
        double muLongRear = loadSensitiveMu(surfaceMuLong * tyreWearGrip, normalRear, referenceRearLoad);

        double brakeCapacityFront = muLongFront * normalFront;
        double brakeCapacityRear = muLongRear * normalRear;
        double brakeForceRequest = brake * MAX_BRAKE_FORCE;
        if (isPitLane() && horizontalSpeed >= maxSpeed) {
            brakeForceRequest = Math.max(brakeForceRequest, 6_000.0);
        }
        double brakeFront = brakeForceRequest * BRAKE_FRONT_BIAS;
        double brakeRear = brakeForceRequest * (1.0 - BRAKE_FRONT_BIAS);
        double brakeSign = velocityLong >= 0.0 ? 1.0 : -1.0;
        double frontLongRequest = -brakeSign * brakeFront;
        double rearLongRequest = driveForceRequest - brakeSign * brakeRear;
        double slipSpeed = Math.max(6.0, Math.abs(velocityLong));
        double rollingForceRamp = Math.max(0.0, Math.min(1.0, (speedMetersPerSecond - 1.5) / 8.5));
        double rollingForceScale = rollingForceRamp * rollingForceRamp * (3.0 - 2.0 * rollingForceRamp);
        double frontSlipAngle = Math.abs(velocityLat) < 0.04 && Math.abs(yawRate) < 0.01 && Math.abs(steeringAngle) < SLIP_ANGLE_DEADBAND ? 0.0 : Math.atan2(velocityLat + yawRate * FRONT_AXLE_DISTANCE, slipSpeed) - steeringAngle;
        double rearSlipAngle = Math.abs(velocityLat) < 0.04 && Math.abs(yawRate) < 0.01 ? 0.0 : Math.atan2(velocityLat - yawRate * REAR_AXLE_DISTANCE, slipSpeed);
        double frontLatTarget = lateralTyreForceRequest(frontSlipAngle, FRONT_CORNERING_STIFFNESS * rollingForceScale, muLatFront * normalFront);
        double rearLatTarget = lateralTyreForceRequest(rearSlipAngle, REAR_CORNERING_STIFFNESS * rollingForceScale, muLatRear * normalRear);
        double frontRelaxation = tyreRelaxationGain(speedMetersPerSecond, FRONT_TYRE_RELAXATION_LENGTH);
        double rearRelaxation = tyreRelaxationGain(speedMetersPerSecond, REAR_TYRE_RELAXATION_LENGTH);
        relaxedFrontLatForce += (frontLatTarget - relaxedFrontLatForce) * frontRelaxation;
        relaxedRearLatForce += (rearLatTarget - relaxedRearLatForce) * rearRelaxation;
        if (Math.abs(frontLatTarget) < 1.0) {
            relaxedFrontLatForce = 0.0;
        }
        if (Math.abs(rearLatTarget) < 1.0) {
            relaxedRearLatForce = 0.0;
        }
        double frontLatRequest = relaxedFrontLatForce;
        double rearLatRequest = relaxedRearLatForce;
        double rearLatCapacity = muLatRear * normalRear;
        double rearLongCapacity = muLongRear * normalRear;
        if (isAbsEnabled() && brake > 0.0) {
            frontLongRequest = absLimitedBrakeForce(frontLongRequest, frontLatRequest, brakeCapacityFront, muLatFront * normalFront);
            double rearBrakeRequest = rearLongRequest - driveForceRequest;
            double rearBrakeLimited = absLimitedBrakeForce(rearBrakeRequest, rearLatRequest, brakeCapacityRear, rearLatCapacity);
            rearLongRequest = driveForceRequest + rearBrakeLimited;
        }
        double rearLatForTractionControl = Math.abs(rearLatRequest) < rearLatCapacity * 0.08 ? 0.0 : rearLatRequest;
        double rearTractionControlLimit = Math.sqrt(Math.max(0.0, square(rearLongCapacity * TRACTION_CONTROL_SLIP_TARGET) - square(rearLatForTractionControl)));
        rearLongRequest = clamp(rearLongRequest, -rearTractionControlLimit, rearTractionControlLimit);
        double frontDemand = combinedSlipDemand(frontLongRequest, frontLatRequest, muLongFront * normalFront, muLatFront * normalFront);
        double rearDemand = combinedSlipDemand(rearLongRequest, rearLatRequest, muLongRear * normalRear, muLatRear * normalRear);
        double frontPatchLongSlip = frontLongRequest / Math.max(1.0, FRONT_CORNERING_STIFFNESS * PHYSICS_DT);
        double rearPatchLongSlip = rearLongRequest / Math.max(1.0, REAR_CORNERING_STIFFNESS * PHYSICS_DT);
        double frontPatchLatSlip = velocityLat + yawRate * FRONT_AXLE_DISTANCE - velocityLong * steeringAngle;
        double rearPatchLatSlip = velocityLat - yawRate * REAR_AXLE_DISTANCE;
        double frontLongForce = tyreForceLongitudinal(frontDemand, frontLongRequest, frontPatchLongSlip, frontPatchLatSlip, muLongFront * normalFront);
        double rearLongForce = tyreForceLongitudinal(rearDemand, rearLongRequest, rearPatchLongSlip, rearPatchLatSlip, muLongRear * normalRear);
        double frontLatForce = tyreForceLateral(frontDemand, frontLatRequest, frontPatchLongSlip, frontPatchLatSlip, muLatFront * normalFront);
        double rearLatForce = tyreForceLateral(rearDemand, rearLatRequest, rearPatchLongSlip, rearPatchLatSlip, muLatRear * normalRear);
        double frontSaturation = frontDemand;
        double rearSaturation = rearDemand;
        double dragForce = -Math.signum(velocityLong) * (aeroDrag + rollingForce + sinkDragForce);
        debugVelocityLong = velocityLong;
        debugVelocityLat = velocityLat;
        debugDriveForce = driveForceRequest;
        debugDragForce = dragForce;
        debugFrontLatForce = frontLatForce;
        debugRearLatForce = rearLatForce;
        debugFrontLongForce = frontLongForce;
        debugRearLongForce = rearLongForce;
        double actualAxForLoad = (rearLongForce + frontLongForce + dragForce) / CAR_MASS_KG;
        double actualLoadTransfer = CAR_MASS_KG * actualAxForLoad * CG_HEIGHT / WHEELBASE;
        double actualNormalFront = Math.max(150.0, staticFrontLoad + aeroFrontLoad - actualLoadTransfer);
        double actualNormalRear = Math.max(150.0, staticRearLoad + aeroRearLoad + actualLoadTransfer);
        debugFrontLoad = actualNormalFront;
        debugRearLoad = actualNormalRear;
        debugFrontDemand = frontDemand;
        debugRearDemand = rearDemand;
        debugFrontSlipAngle = frontSlipAngle;
        debugRearSlipAngle = rearSlipAngle;
        debugDownforce = downforce;

        double longitudinalForce = rearLongForce + frontLongForce + dragForce;
        double lateralForce = frontLatForce + rearLatForce;
        double accelerationLong = longitudinalForce / CAR_MASS_KG + yawRate * velocityLat;
        double accelerationLat = lateralForce / CAR_MASS_KG - yawRate * velocityLong;
        double yawMoment = FRONT_AXLE_DISTANCE * frontLatForce - REAR_AXLE_DISTANCE * rearLatForce;
        double yawAcceleration = yawMoment / YAW_INERTIA;

        double previousKineticEnergy = 0.5 * CAR_MASS_KG * (velocityLong * velocityLong + velocityLat * velocityLat) + 0.5 * YAW_INERTIA * yawRate * yawRate;
        double previousVelocityLong = velocityLong;
        double yawDelta = 0.0;
        double subDt = PHYSICS_DT / PHYSICS_SUBSTEPS;
        for (int substep = 0; substep < PHYSICS_SUBSTEPS; substep++) {
            double subAccelerationLong = longitudinalForce / CAR_MASS_KG + yawRate * velocityLat;
            double subAccelerationLat = lateralForce / CAR_MASS_KG - yawRate * velocityLong;
            velocityLong += subAccelerationLong * subDt;
            if (previousVelocityLong > 0.0 && velocityLong < 0.0 && driveForceRequest <= brakeForceRequest) {
                velocityLong = 0.0;
            } else if (previousVelocityLong < 0.0 && velocityLong > 0.0 && throttle == 0.0) {
                velocityLong = 0.0;
            }
            velocityLat += subAccelerationLat * subDt;
            yawRate += yawAcceleration * subDt;
            yawDelta += yawRate * subDt;
        }
        double newKineticEnergy = 0.5 * CAR_MASS_KG * (velocityLong * velocityLong + velocityLat * velocityLat) + 0.5 * YAW_INERTIA * yawRate * yawRate;
        double positivePowerForce = Math.max(0.0, rearLongForce + frontLongForce);
        double workSpeed = Math.max(0.0, (previousVelocityLong + velocityLong) * 0.5);
        double allowedEnergy = previousKineticEnergy + positivePowerForce * workSpeed * PHYSICS_DT;
        if (newKineticEnergy > allowedEnergy && newKineticEnergy > 0.0) {
            double energyScale = Math.sqrt(allowedEnergy / newKineticEnergy);
            velocityLong *= energyScale;
            velocityLat *= energyScale;
            yawRate *= energyScale;
            yawDelta *= energyScale;
        }
        if (steerInput == 0.0 && Math.abs(velocityLat) < 0.08 && Math.abs(yawRate) < 0.025) {
            velocityLat = 0.0;
            yawRate = 0.0;
            resetTyreRelaxation();
        }
        setYRot(getYRot() + (float) Math.toDegrees(yawDelta));

        double frontExcess = Math.max(0.0, frontSaturation - 1.0);
        double rearExcess = Math.max(0.0, rearSaturation - 1.0);
        double slipMetric = Math.abs(frontSlipAngle) * 0.7 + Math.abs(rearSlipAngle) * 0.9 + frontExcess + rearExcess;
        tyreSlip = Math.max(tyreSlip, Math.min(1.0, slipMetric * Math.min(1.0, speedMetersPerSecond / 18.0)));
        if (speedMetersPerSecond > 1.0) {
            addTyreWear((float) (slipMetric * speedMetersPerSecond * 0.00035 * setup.tyreWearMultiplier() * surface.wearMult));
        }

        forward = Vec3.directionFromRotation(0.0f, getYRot());
        right = new Vec3(forward.z, 0.0, -forward.x);
        double newY = onGround() ? 0.0 : delta.y - 0.04;
        delta = new Vec3(
            (forward.x * velocityLong + right.x * velocityLat) / 20.0,
            newY,
            (forward.z * velocityLong + right.z * velocityLat) / 20.0
        );

        setDeltaMovement(delta);
        Vec3 beforeMove = position();
        move(MoverType.SELF, delta);
        Vec3 actualMovement = position().subtract(beforeMove);
        setDeltaMovement(actualMovement);
        scanLapMarkers();

        boolean shouldDebugMovement = debugMovement && (getControllingPassenger() != null || throttle != 0.0 || brake != 0.0 || steering != 0.0 || horizontalSpeed > 0.01 || actualMovement.horizontalDistance() > 0.01);
        if (shouldDebugMovement && level().getGameTime() - lastMovementDebugAt >= 20L) {
            lastMovementDebugAt = level().getGameTime();
            LOGGER.info("OWR car move id={} passenger={} posBefore={} posAfter={} requested={} actual={} input=({}, {}, {}) gear={} surface={} collisions(h={}, v={}) onGround={} speedBefore={} speedAfter={}",
                getId(),
                getControllingPassenger() == null ? "none" : getControllingPassenger().getScoreboardName(),
                formatVec(beforeMove),
                formatVec(position()),
                formatVec(delta),
                formatVec(actualMovement),
                String.format("%.2f", throttle),
                String.format("%.2f", brake),
                String.format("%.2f", steering),
                gear,
                surface,
                horizontalCollision,
                verticalCollision,
                onGround(),
                String.format("%.4f", horizontalSpeed),
                String.format("%.4f", actualMovement.horizontalDistance()));
        }

        double newSpeed = Math.sqrt(actualMovement.x * actualMovement.x + actualMovement.z * actualMovement.z);
        int rpm = calculateRpm(newSpeed, gear, gearTopSpeed, throttle > 0.0 && gear == 1 && newSpeed < LAUNCH_CLUTCH_SPEED);
        entityData.set(SPEED, (float)(newSpeed * 72.0));
        entityData.set(RPM, rpm);
        entityData.set(TYRE_SLIP, (float) Math.max(0.0, Math.min(1.0, tyreSlip)));
        previousHorizontalSpeed = horizontalSpeed;
    }

    private void tickImpactDamage() {
        if (horizontalCollision && previousHorizontalSpeed > 0.28) {
            Vec3 barrierNormal = nearbyBarrierNormal();
            boolean barrierImpact = barrierNormal.lengthSqr() > 0.0;
            double approachFactor = barrierImpact ? barrierApproachFactor(barrierNormal) : 1.0;
            float severity = (float) ((previousHorizontalSpeed - 0.28) * (barrierImpact ? 14.0 * approachFactor : 40.0));
            addDamage(severity);
            playImpactFeedback(severity);
            if (barrierImpact) {
                setDeltaMovement(bounceFromBarrier(getDeltaMovement(), barrierNormal, approachFactor));
            } else {
                setDeltaMovement(getDeltaMovement().scale(0.15));
            }

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
            visitedCheckpoints.clear();
            visitedCheckpointSet.clear();
            entityData.set(CURRENT_LAP_TICKS, 0);
            entityData.set(CHECKPOINT_ARMED, false);
            showInvalidLap(reason);
        }
    }

    private boolean updatePlayerBestLap(int lapTicks) {
        Entity passenger = getControllingPassenger();
        if (!(passenger instanceof Player player) || !(level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        OWRLapRecords records = OWRLapRecords.get(serverLevel);
        boolean personalBest = records.setBestLapIfBetter(player.getUUID(), lapTicks);
        entityData.set(BEST_LAP_TICKS, records.getBestLap(player.getUUID()));
        return personalBest;
    }

    public void syncPlayerBestLap(Player player) {
        if (level() instanceof ServerLevel serverLevel) {
            entityData.set(BEST_LAP_TICKS, OWRLapRecords.get(serverLevel).getBestLap(player.getUUID()));
        }
    }

    public void prepareForDriver(Player player) {
        syncPlayerBestLap(player);
        double speed = Math.sqrt(getDeltaMovement().x * getDeltaMovement().x + getDeltaMovement().z * getDeltaMovement().z);
        if (speed < 0.04) {
            entityData.set(GEAR, 1);
        }
        if (!level().isClientSide()) {
            LOGGER.info("OWR car mounted id={} player={} pos=({}, {}, {}) delta={} gear={} speed={} bbox={}",
                getId(),
                player.getScoreboardName(),
                String.format("%.3f", getX()),
                String.format("%.3f", getY()),
                String.format("%.3f", getZ()),
                formatVec(getDeltaMovement()),
                getGear(),
                String.format("%.4f", speed),
                getBoundingBox());
        }
    }

    private void logShift(String direction) {
        if (!level().isClientSide()) {
            LOGGER.info("OWR car shift id={} direction={} gear={} speedKmh={} rpm={} passenger={}",
                getId(),
                direction,
                getGear(),
                String.format("%.1f", getSpeedKmh()),
                getRpm(),
                getControllingPassenger() == null ? "none" : getControllingPassenger().getScoreboardName());
        }
    }

    private boolean isForwardPass(Direction markerFacing) {
        Vec3 carForward = Vec3.directionFromRotation(0.0f, getYRot());
        Vec3 markerForward = new Vec3(markerFacing.getStepX(), 0.0, markerFacing.getStepZ());
        return carForward.dot(markerForward) > 0.35;
    }

    private Vec3 nearbyBarrierNormal() {
        Vec3 normal = Vec3.ZERO;
        Vec3 carCenter = position();
        for (BlockPos pos : BlockPos.betweenClosed(blockPosition().offset(-1, 0, -1), blockPosition().offset(1, 2, 1))) {
            if (level().getBlockState(pos).is(OWRBlocks.BARRIER.get())) {
                Vec3 away = carCenter.subtract(Vec3.atCenterOf(pos));
                away = new Vec3(away.x, 0.0, away.z);
                if (away.lengthSqr() > 1.0E-4) {
                    normal = normal.add(away.normalize());
                }
            }
        }
        return normal.lengthSqr() > 1.0E-4 ? normal.normalize() : Vec3.ZERO;
    }

    private double barrierApproachFactor(Vec3 barrierNormal) {
        Vec3 velocity = getDeltaMovement();
        Vec3 horizontalVelocity = new Vec3(velocity.x, 0.0, velocity.z);
        if (horizontalVelocity.lengthSqr() < 1.0E-6) {
            return 0.25;
        }
        double headOn = Math.max(0.0, -horizontalVelocity.normalize().dot(barrierNormal));
        return 0.18 + headOn * headOn * 0.82;
    }

    private Vec3 bounceFromBarrier(Vec3 velocity, Vec3 barrierNormal, double approachFactor) {
        Vec3 horizontalVelocity = new Vec3(velocity.x, 0.0, velocity.z);
        double intoBarrier = horizontalVelocity.dot(barrierNormal);
        Vec3 reflected = horizontalVelocity;
        if (intoBarrier < 0.0) {
            reflected = horizontalVelocity.subtract(barrierNormal.scale(1.55 * intoBarrier));
        }
        double retainedSpeed = 0.35 + (1.0 - approachFactor) * 0.45;
        reflected = reflected.scale(retainedSpeed);
        return new Vec3(reflected.x, velocity.y, reflected.z);
    }

    private void destroyIntoMaterials(ServerLevel serverLevel) {
        invalidateLap("car destroyed");
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

    private void showInvalidLap(String reason) {
        Component reasonMessage = Component.literal(reason);
        Entity passenger = getControllingPassenger();
        if (passenger instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetTitlesAnimationPacket(5, 35, 10));
            serverPlayer.connection.send(new ClientboundSetTitleTextPacket(Component.literal("INVALID LAP")));
            serverPlayer.connection.send(new ClientboundSetSubtitleTextPacket(reasonMessage));
        }
        messageDriver(Component.literal("INVALID LAP: " + reason));
    }

    private static int calculateRpm(double speed, int gear, double gearTopSpeed, boolean autoClutch) {
        double wheelRpm = gearTopSpeed <= 0.0 ? IDLE_RPM : speed / gearTopSpeed * REDLINE_RPM;
        double rpm = Math.max(IDLE_RPM, wheelRpm);
        if (autoClutch) {
            rpm = Math.max(rpm, LAUNCH_RPM);
        }
        return (int) Math.max(IDLE_RPM, Math.min(REDLINE_RPM, rpm));
    }

    private static double powerAcceleration(double powerWatts, double speedBlocksPerTick) {
        double speedMetersPerSecond = Math.max(MIN_POWER_SPEED, speedBlocksPerTick * 20.0);
        double accelerationMetersPerSecondSquared = powerWatts / (CAR_MASS_KG * speedMetersPerSecond);
        return accelerationMetersPerSecondSquared / 400.0;
    }

    private static double enginePowerWatts(int rpm) {
        double[] rpmPoints = {900.0, 2500.0, 4000.0, 4700.0, 6500.0, 8200.0, 10500.0, 11800.0, 12600.0, 13000.0};
        double[] powerPoints = {0.03, 0.10, 0.22, 0.34, 0.56, 0.75, 0.95, 1.00, 0.78, 0.42};
        if (rpm <= rpmPoints[0]) {
            return PEAK_POWER_WATTS * powerPoints[0];
        }
        for (int i = 1; i < rpmPoints.length; i++) {
            if (rpm <= rpmPoints[i]) {
                double t = (rpm - rpmPoints[i - 1]) / (rpmPoints[i] - rpmPoints[i - 1]);
                double power = powerPoints[i - 1] + (powerPoints[i] - powerPoints[i - 1]) * t;
                return PEAK_POWER_WATTS * power;
            }
        }
        return PEAK_POWER_WATTS * powerPoints[powerPoints.length - 1];
    }

    private void resetTyreRelaxation() {
        relaxedFrontLatForce = 0.0;
        relaxedRearLatForce = 0.0;
    }

    private static double loadSensitiveMu(double baseMu, double normalLoad, double referenceLoad) {
        return Math.max(0.35, baseMu * (1.0 - LOAD_SENSITIVITY * (normalLoad / referenceLoad - 1.0)));
    }

    private static double lateralTyreForceRequest(double slipAngle, double corneringStiffness, double peakLateralForce) {
        if (Math.abs(slipAngle) < SLIP_ANGLE_DEADBAND) {
            return 0.0;
        }
        double linearForce = -corneringStiffness * slipAngle;
        if (peakLateralForce <= 1.0) {
            return 0.0;
        }
        return peakLateralForce * Math.tanh(linearForce / peakLateralForce);
    }

    private static double tyreRelaxationGain(double speedMetersPerSecond, double relaxationLength) {
        double timeConstant = relaxationLength / Math.max(1.0, speedMetersPerSecond);
        return 1.0 - Math.exp(-PHYSICS_DT / timeConstant);
    }

    private static double absLimitedBrakeForce(double brakeForce, double lateralForce, double longitudinalLimit, double lateralLimit) {
        double lateralUse = Math.abs(lateralForce) / Math.max(1.0, lateralLimit);
        double longitudinalAvailable = longitudinalLimit * Math.sqrt(Math.max(0.0, 0.96 * 0.96 - lateralUse * lateralUse));
        return clamp(brakeForce, -longitudinalAvailable, longitudinalAvailable);
    }

    private static double combinedSlipDemand(double longitudinalForce, double lateralForce, double longitudinalLimit, double lateralLimit) {
        double x = longitudinalForce / Math.max(1.0, longitudinalLimit);
        double y = lateralForce / Math.max(1.0, lateralLimit);
        return Math.sqrt(x * x + y * y);
    }

    private static double tyreForceLongitudinal(double demand, double requestedForce, double longitudinalSlipVelocity, double lateralSlipVelocity, double forceLimit) {
        if (demand <= 1.0) {
            return requestedForce;
        }
        return Math.signum(requestedForce) * Math.min(Math.abs(requestedForce) / demand, KINETIC_MU_RATIO * forceLimit);
    }

    private static double tyreForceLateral(double demand, double requestedForce, double longitudinalSlipVelocity, double lateralSlipVelocity, double forceLimit) {
        if (demand <= 1.0) {
            return requestedForce;
        }
        double slipMagnitude = Math.sqrt(longitudinalSlipVelocity * longitudinalSlipVelocity + lateralSlipVelocity * lateralSlipVelocity);
        if (slipMagnitude < 0.05) {
            return requestedForce / demand;
        }
        return -KINETIC_MU_RATIO * forceLimit * lateralSlipVelocity / slipMagnitude;
    }

    private static double square(double value) {
        return value * value;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String formatLapTime(int ticks) {
        int totalCentiseconds = ticks * 5;
        int minutes = totalCentiseconds / 6000;
        int seconds = totalCentiseconds / 100 % 60;
        int centiseconds = totalCentiseconds % 100;
        return String.format("%d:%02d.%02d", minutes, seconds, centiseconds);
    }

    private static String formatVec(Vec3 vec) {
        return String.format("(%.4f, %.4f, %.4f)", vec.x, vec.y, vec.z);
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
