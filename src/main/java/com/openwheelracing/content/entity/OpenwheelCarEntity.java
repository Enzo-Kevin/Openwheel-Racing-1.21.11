package com.openwheelracing.content.entity;

import com.openwheelracing.content.car.PrototypeCarSetup;
import com.openwheelracing.content.item.PrototypeCarItem;
import com.openwheelracing.registry.OWRItems;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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

    private static final double[] GEAR_RATIOS = {0.0, 0.34, 0.48, 0.62, 0.76, 0.90, 1.05};
    private PrototypeCarSetup setup = PrototypeCarSetup.DEFAULT;
    private double previousHorizontalSpeed;

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
    }

    public void setSetup(PrototypeCarSetup setup) {
        this.setup = setup;
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

    public void shiftUp() {
        if (getGear() < 6) {
            entityData.set(GEAR, getGear() + 1);
        }
    }

    public void shiftDown() {
        if (getGear() > 1) {
            entityData.set(GEAR, getGear() - 1);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            tickMovement();
            tickImpactDamage();
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide() && getPassengers().isEmpty()) {
            player.startRiding(this);
        }
        return InteractionResult.SUCCESS;
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
            Vec3 seat = new Vec3(0.0, 0.45, 0.0).yRot((float) -Math.toRadians(getYRot()));
            callback.accept(passenger, getX() + seat.x, getY() + seat.y, getZ() + seat.z);
        }
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel level, net.minecraft.world.damagesource.DamageSource damageSource, float amount) {
        addDamage(amount * 4.0f);
        if (getDamagePercent() >= 100.0f) {
            spawnAtLocation(level, PrototypeCarItem.createWithDefaultSetup());
            discard();
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
            setYRot(player.getYRot());
        }

        Vec3 delta = getDeltaMovement();
        double horizontalSpeed = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        double damageFactor = 1.0 - getDamagePercent() / 140.0;
        double tyreFactor = 1.0 - getTyreWearPercent() / 180.0;
        double gearRatio = GEAR_RATIOS[getGear()];
        double maxSpeed = 1.15 * setup.gearingMultiplier() * setup.aeroMultiplier() * damageFactor;
        double acceleration = 0.035 * setup.powerMultiplier() * gearRatio * damageFactor;
        double brakeForce = 0.055 * tyreFactor;

        if (throttle > 0.0 && horizontalSpeed < maxSpeed) {
            Vec3 forward = Vec3.directionFromRotation(0.0f, getYRot()).scale(acceleration * throttle);
            delta = delta.add(forward.x, 0.0, forward.z);
        }

        if (brake > 0.0) {
            delta = new Vec3(delta.x * (1.0 - brakeForce * brake), delta.y, delta.z * (1.0 - brakeForce * brake));
        }

        if (Math.abs(steering) > 0.01 && horizontalSpeed > 0.02) {
            float turn = (float) (-steering * (2.0 + 3.0 * tyreFactor * setup.gripMultiplier()) * Math.min(1.0, horizontalSpeed / 0.5));
            setYRot(getYRot() + turn);
            addTyreWear((float) (Math.abs(steering) * horizontalSpeed * 0.015));
        }

        delta = new Vec3(delta.x * 0.96, delta.y - 0.04, delta.z * 0.96);
        if (onGround()) {
            delta = new Vec3(delta.x * 0.98, 0.0, delta.z * 0.98);
        }

        setDeltaMovement(delta);
        move(MoverType.SELF, getDeltaMovement());

        double newSpeed = Math.sqrt(getDeltaMovement().x * getDeltaMovement().x + getDeltaMovement().z * getDeltaMovement().z);
        entityData.set(SPEED, (float) (newSpeed * 72.0));
        entityData.set(RPM, Math.max(900, Math.min(12500, (int) (900 + newSpeed * 9000 / Math.max(0.2, gearRatio)))));
        previousHorizontalSpeed = horizontalSpeed;
    }

    private void tickImpactDamage() {
        if (horizontalCollision && previousHorizontalSpeed > 0.28) {
            float severity = (float) ((previousHorizontalSpeed - 0.28) * 40.0);
            addDamage(severity);
            setDeltaMovement(getDeltaMovement().scale(0.15));

            Entity passenger = getControllingPassenger();
            if (passenger instanceof Player player) {
                player.hurt(damageSources().flyIntoWall(), Math.max(1.0f, severity * 0.35f));
            }
        }
    }

    private void addDamage(float amount) {
        entityData.set(DAMAGE, Math.min(100.0f, getDamagePercent() + amount));
    }

    private void addTyreWear(float amount) {
        entityData.set(TYRE_WEAR, Math.min(100.0f, getTyreWearPercent() + amount));
    }
}
