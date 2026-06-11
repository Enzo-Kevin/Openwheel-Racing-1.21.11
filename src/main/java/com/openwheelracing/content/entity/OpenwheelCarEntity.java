package com.openwheelracing.content.entity;

import com.mojang.logging.LogUtils;
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
    private static final EntityDataAccessor<Integer> CURRENT_LAP_TICKS = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BEST_LAP_TICKS = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CHECKPOINT_ARMED = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> PIT_STOP_TICKS = SynchedEntityData.defineId(OpenwheelCarEntity.class, EntityDataSerializers.INT);

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

    private static final double[] GEAR_RATIOS = {0.0, 0.28, 0.43, 0.58, 0.74, 0.91, 1.10};
    private static final double STEERING_DEADZONE = 0.08;
    private static final double MIN_STEERING_SPEED = 0.04;
    private static final double BASE_MAX_SPEED = 1.32;
    private static final double BASE_ACCELERATION = 0.038;
    private static final double BASE_BRAKE_FORCE = 0.10;
    private static final double ENGINE_BRAKE = 0.018; // deceleration per tick when off throttle
    private static final double AIR_DRAG = 0.978;
    private static final double GROUND_DRAG = 0.985;  // lateral-only side-slip cancel, not forward drag
    private PrototypeCarSetup setup = PrototypeCarSetup.DEFAULT;
    private double previousHorizontalSpeed;
    private long lapStartedAt = -1L;
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
        builder.define(CURRENT_LAP_TICKS, 0);
        builder.define(BEST_LAP_TICKS, 0);
        builder.define(CHECKPOINT_ARMED, false);
        builder.define(PIT_STOP_TICKS, 0);
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

    public void crossStartFinishLine(Direction markerFacing) {
        if (!isForwardPass(markerFacing)) {
            invalidateLap("reverse pass");
            return;
        }

        long gameTime = level().getGameTime();
        if (lapStartedAt >= 0L) {
            // Require at least one checkpoint to have been visited
            if (visitedCheckpoints.isEmpty()) {
                invalidateLap("no checkpoints crossed");
                lapStartedAt = gameTime;
                visitedCheckpoints.clear();
                visitedCheckpointSet.clear();
                entityData.set(CHECKPOINT_ARMED, false);
                entityData.set(CURRENT_LAP_TICKS, 0);
                messageDriver(Component.literal("Lap started — cross all checkpoints"));
                return;
            }
            int lapTicks = Math.max(1, (int)(gameTime - lapStartedAt));
            entityData.set(CURRENT_LAP_TICKS, lapTicks);
            boolean personalBest = updatePlayerBestLap(lapTicks);
            messageDriver(Component.literal("Lap: " + formatLapTime(lapTicks)
                + " | CPs: " + visitedCheckpoints.size()
                + (personalBest ? " | Personal best" : "")));
        } else {
            messageDriver(Component.literal("Lap started"));
        }

        lapStartedAt = gameTime;
        visitedCheckpoints.clear();
        visitedCheckpointSet.clear();
        entityData.set(CHECKPOINT_ARMED, false);
        entityData.set(CURRENT_LAP_TICKS, 0);
    }

    public void crossCheckpoint(BlockPos pos, Direction markerFacing) {
        if (!isForwardPass(markerFacing)) {
            invalidateLap("reverse checkpoint pass");
            return;
        }
        if (lapStartedAt < 0L) {
            return; // no lap in progress
        }
        long packed = pos.asLong();
        if (visitedCheckpointSet.contains(packed)) {
            invalidateLap("checkpoint crossed twice");
            return;
        }
        visitedCheckpoints.add(packed);
        visitedCheckpointSet.add(packed);
        entityData.set(CHECKPOINT_ARMED, true);
        messageDriver(Component.literal("CP " + visitedCheckpoints.size()));
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
            ItemStack item = PrototypeCarItem.create(setup, getDamagePercent(), getTyreWearPercent());
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
        //                          grip   drag   wearMult  lapValid
        ASPHALT(                    1.00,  0.997,  1.0,     true),
        KERB(                       0.78,  0.991,  1.8,     true),
        PIT_LANE(                   0.95,  0.996,  0.6,     true),  // low wear in pit
        DIRT(                       0.58,  0.952,  1.4,     false),
        GRAVEL(                     0.45,  0.940,  2.0,     false);

        final double grip;
        final double drag;
        final double wearMult;
        final boolean countsAsTrack;

        SurfaceProfile(double grip, double drag, double wearMult, boolean countsAsTrack) {
            this.grip           = grip;
            this.drag           = drag;
            this.wearMult       = wearMult;
            this.countsAsTrack  = countsAsTrack;
        }
    }

    private SurfaceProfile getSurface(BlockPos pos) {
        Block block = level().getBlockState(pos.below()).getBlock();
        if (block == OWRBlocks.ASPHALT_TRACK.get()
                || block == OWRBlocks.START_FINISH.get()
                || block == OWRBlocks.CHECKPOINT.get()) return SurfaceProfile.ASPHALT;
        if (block == OWRBlocks.PIT_LANE.get())  return SurfaceProfile.PIT_LANE;
        if (block == OWRBlocks.KERB.get())       return SurfaceProfile.KERB;
        return SurfaceProfile.DIRT;
    }

    private SurfaceProfile getCurrentSurface() {
        return getSurface(blockPosition());
    }

    private boolean isPitLane() {
        return getCurrentSurface() == SurfaceProfile.PIT_LANE;
    }

    private boolean isOnTrackSurface() {
        double yaw = Math.toRadians(getYRot());
        Vec3 forward = new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
        Vec3 right   = new Vec3(forward.z, 0.0, -forward.x);
        for (double side : new double[]{-0.95, 0.95}) {
            for (double length : new double[]{-1.25, 1.25}) {
                BlockPos pos = BlockPos.containing(position().add(right.scale(side)).add(forward.scale(length)));
                if (getSurface(pos).countsAsTrack) return true;
            }
        }
        return false;
    }


    public void tickLocalClientMovement(float throttle, float brake, float steering) {
        if (level().isClientSide() && getControllingPassenger() != null) {
            tickMovement(throttle, brake, steering, false);
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
        // Gear ratio: low gear = high torque + low top speed, high gear = low torque + high top speed
        int gear = getGear();
        double gearRatio       = GEAR_RATIOS[gear];                         // 0.28 (1st) → 1.10 (6th)
        double torqueMultiplier = (GEAR_RATIOS[1] + GEAR_RATIOS[6]) - gearRatio; // inverted: more in low gears
        double maxSpeed = BASE_MAX_SPEED * setup.gearingMultiplier() * setup.aeroMultiplier() * damageFactor;
        if (isPitLane()) maxSpeed = Math.min(maxSpeed, 0.52);

        double acceleration = BASE_ACCELERATION * setup.powerMultiplier() * torqueMultiplier * damageFactor;

        // --- Throttle ---
        if (throttle > 0.0 && horizontalSpeed < maxSpeed) {
            Vec3 forward = Vec3.directionFromRotation(0.0f, getYRot()).scale(acceleration * throttle);
            delta = delta.add(forward.x, 0.0, forward.z);
        }

        // --- Engine brake (off throttle) ---
        if (throttle == 0.0 && brake == 0.0 && horizontalSpeed > 0.01) {
            double engineBrake = ENGINE_BRAKE * gearRatio * damageFactor;
            double newSpeed = Math.max(0.0, horizontalSpeed - engineBrake);
            double scale = horizontalSpeed > 0 ? newSpeed / horizontalSpeed : 0;
            delta = new Vec3(delta.x * scale, delta.y, delta.z * scale);
        }

        // --- Foot brake ---
        if (brake > 0.0) {
            double brakeForce = BASE_BRAKE_FORCE * tyreFactor * getCurrentSurface().grip;
            double brakeScale = Math.max(0.0, 1.0 - brakeForce * brake);
            delta = new Vec3(delta.x * brakeScale, delta.y, delta.z * brakeScale);
        }

        // --- Steering + lateral grip ---
        SurfaceProfile surface = getCurrentSurface();
        if (Math.abs(steering) > STEERING_DEADZONE && horizontalSpeed > MIN_STEERING_SPEED) {
            double speedNorm      = Math.min(1.0, horizontalSpeed / maxSpeed);
            double maxTurnPerTick = 4.5 - speedNorm * 3.2;
            float turn = (float) (-steering * maxTurnPerTick * Math.min(1.0, horizontalSpeed / 0.3));
            setYRot(getYRot() + turn);
            addTyreWear((float)(Math.abs(steering) * horizontalSpeed * 0.012 * setup.tyreWearMultiplier() * surface.wearMult));
        }

        // --- Lateral grip / understeer ---
        if (horizontalSpeed > 0.01) {
            Vec3 forward        = Vec3.directionFromRotation(0.0f, getYRot());
            double forwardSpeed = delta.x * forward.x + delta.z * forward.z;
            double sideX        = delta.x - forward.x * forwardSpeed;
            double sideZ        = delta.z - forward.z * forwardSpeed;
            double sideSpeed    = Math.sqrt(sideX * sideX + sideZ * sideZ);
            double corneringLoad   = horizontalSpeed / maxSpeed;
            double gripBase        = (0.55 + 0.45 * tyreFactor * setup.gripMultiplier()) * surface.grip;
            double gripEffective   = Math.max(0.1, Math.min(1.0, gripBase * (1.0 - 0.35 * corneringLoad)));
            double sideKill  = Math.min(sideSpeed, Math.max(0.0, sideSpeed - gripEffective * 0.04));
            double killScale = sideSpeed > 0 ? (sideSpeed - sideKill) / sideSpeed : 1.0;
            delta = new Vec3(
                forward.x * forwardSpeed + sideX * killScale,
                delta.y,
                forward.z * forwardSpeed + sideZ * killScale
            );
        }

        // --- Drag & gravity ---
        double drag = AIR_DRAG * setup.dragMultiplier() * surface.drag;
        double newY = onGround() ? 0.0 : delta.y - 0.04;
        delta = new Vec3(delta.x * drag, newY, delta.z * drag);

        setDeltaMovement(delta);
        Vec3 beforeMove = position();
        move(MoverType.SELF, delta);
        Vec3 actualMovement = position().subtract(beforeMove);
        setDeltaMovement(actualMovement);

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
        entityData.set(SPEED, (float)(newSpeed * 72.0));
        entityData.set(RPM, Math.max(900, Math.min(12500, (int)(900 + newSpeed * 7200 * gearRatio / Math.max(0.15, GEAR_RATIOS[6])))));
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
