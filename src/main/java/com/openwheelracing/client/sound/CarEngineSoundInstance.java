package com.openwheelracing.client.sound;

import com.openwheelracing.content.car.CarLivery;
import com.openwheelracing.content.entity.OpenwheelCarEntity;
import com.openwheelracing.registry.OWRSoundEvents;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;

final class CarEngineSoundInstance extends AbstractTickableSoundInstance {
    private static final float IDLE_RPM    = 900.0f;
    private static final float REDLINE_RPM = 13000.0f;
    // Gear top speeds in km/h (index 0 = neutral placeholder, 1-8 = gears)
    private static final float[] GEAR_TOP_KMH = { 0f, 80f, 120f, 150f, 190f, 235f, 275f, 310f, 350f };
    private static final float REVERSE_TOP_KMH = 35f;
    // Mirror server-side neutral blip rates (RPM/s)
    private static final float NEUTRAL_RISE = 18_000f;
    private static final float NEUTRAL_DECAY = 3_800f;
    private static final float ENGINE_BRAKE_DECAY = 7_000f;
    private static final float CLUTCH_RPM_DROP = 12_000f;
    private static final float LAUNCH_RPM = 4_000f;
    private static final int CLUTCH_RELEASE_TICKS = 12;
    private static final float DT = 1.0f / 20.0f; // one game tick

    private final Tone tone;
    private OpenwheelCarEntity car;
    private Vec3 listenerPosition;
    private Vec3 previousSourcePosition;
    private float predictedRpm;
    private int lastGear = Integer.MIN_VALUE;
    private int shiftCutTicks;
    private int clutchReleaseTicks;

    private CarEngineSoundInstance(OpenwheelCarEntity car, Vec3 listenerPosition, SoundEvent soundEvent, Tone tone) {
        super(soundEvent, SoundSource.PLAYERS, RandomSource.create());
        this.car = car;
        this.listenerPosition = listenerPosition;
        this.tone = tone;
        looping = true;
        delay = 0;
        attenuation = Attenuation.LINEAR;
        volume = 0.0f;
        pitch = 1.0f;
        x = car.getX();
        y = car.getY() + 0.35;
        z = car.getZ();
        previousSourcePosition = new Vec3(x, y, z);
        predictedRpm = car.getRpm();
        lastGear = car.getGear();
    }

    private static SoundEvent resolve(RegistryObject<SoundEvent> ferrari,
                                      RegistryObject<SoundEvent> renault,
                                      RegistryObject<SoundEvent> mercedes,
                                      RegistryObject<SoundEvent> rbpt,
                                      OpenwheelCarEntity car) {
        CarLivery.PowerUnit pu = CarLivery.fromIndex(car.getLivery()).powerUnit();
        return switch (pu) {
            case FERRARI  -> ferrari.get();
            case RENAULT  -> renault.get();
            case MERCEDES -> mercedes.get();
            case RBPT     -> rbpt.get();
        };
    }

    static CarEngineSoundInstance lowTone(OpenwheelCarEntity car, Vec3 listenerPosition) {
        SoundEvent sound = resolve(
            OWRSoundEvents.CAR_ENGINE_FERRARI_LOW,
            OWRSoundEvents.CAR_ENGINE_RENAULT_LOW,
            OWRSoundEvents.CAR_ENGINE_MERCEDES_LOW,
            OWRSoundEvents.CAR_ENGINE_RBPT_LOW,
            car);
        return new CarEngineSoundInstance(car, listenerPosition, sound, Tone.LOW);
    }

    static CarEngineSoundInstance highTone(OpenwheelCarEntity car, Vec3 listenerPosition) {
        SoundEvent sound = resolve(
            OWRSoundEvents.CAR_ENGINE_FERRARI_HIGH,
            OWRSoundEvents.CAR_ENGINE_RENAULT_HIGH,
            OWRSoundEvents.CAR_ENGINE_MERCEDES_HIGH,
            OWRSoundEvents.CAR_ENGINE_RBPT_HIGH,
            car);
        return new CarEngineSoundInstance(car, listenerPosition, sound, Tone.HIGH);
    }

    void replaceCar(OpenwheelCarEntity car) {
        this.car = car;
    }

    void updateListener(Vec3 listenerPosition) {
        this.listenerPosition = listenerPosition;
    }

    @Override
    public boolean canStartSilent() { return true; }

    @Override
    public boolean canPlaySound() {
        return car != null && car.isAlive() && !car.isRemoved();
    }

    @Override
    public void tick() {
        if (!canPlaySound()) {
            stop();
            return;
        }

        previousSourcePosition = new Vec3(x, y, z);
        x = car.getX();
        y = car.getY() + 0.35;
        z = car.getZ();

        int gear = car.getGear();
        float speedKmh = car.getSpeedKmh();

        if (gear != lastGear) {
            int previousGear = lastGear;
            if (previousGear == 0 && gear != 0 && predictedRpm > LAUNCH_RPM) {
                clutchReleaseTicks = CLUTCH_RELEASE_TICKS;
                predictedRpm = applyClutchRelease(gear, speedKmh);
            } else {
                clutchReleaseTicks = 0;
                predictedRpm = rpmFromSpeed(gear, speedKmh);
            }
            shiftCutTicks = previousGear > 0 && gear > previousGear ? 1 : 0;
            lastGear = gear;
        } else {
            predictedRpm = predictRpm(gear, speedKmh);
        }

        float rpmNorm = Mth.clamp((predictedRpm - 900.0f) / 11600.0f, 0.0f, 1.0f);
        float basePitch = tone.pitch(predictedRpm);
        float distance = (float) listenerPosition.distanceTo(new Vec3(x, y, z));
        float shiftGain = shiftCutTicks > 0 ? 0.28f : 1.0f;
        volume = tone.volume(speedKmh, rpmNorm) * CarSoundPhysics.attenuation(distance) * shiftGain;
        pitch = CarSoundPhysics.doppler(basePitch, new Vec3(x, y, z), previousSourcePosition, listenerPosition);
        if (shiftCutTicks > 0) {
            shiftCutTicks--;
        }
    }

    private float predictRpm(int gear, float speedKmh) {
        if (gear == 0) {
            // Neutral: mirror server blip logic using float precision (no int truncation)
            int serverRpm = car.getRpm();
            // Rising if server RPM is climbing, decaying otherwise
            if (serverRpm > predictedRpm) {
                predictedRpm += NEUTRAL_RISE * DT;
            } else {
                predictedRpm -= NEUTRAL_DECAY * DT;
            }
            return Mth.clamp(predictedRpm, IDLE_RPM, REDLINE_RPM);
        }

        if (clutchReleaseTicks > 0) {
            return applyClutchRelease(gear, speedKmh);
        }

        float floorRpm = rpmFromSpeed(gear, speedKmh);
        if (predictedRpm > floorRpm) {
            predictedRpm = Math.max(floorRpm, predictedRpm - ENGINE_BRAKE_DECAY * DT);
        } else {
            predictedRpm = floorRpm;
        }
        return Mth.clamp(predictedRpm, IDLE_RPM, REDLINE_RPM);
    }

    private float applyClutchRelease(int gear, float speedKmh) {
        float wheelRpm = rpmFromSpeed(gear, speedKmh);
        predictedRpm = Math.max(wheelRpm, predictedRpm - CLUTCH_RPM_DROP * DT);
        clutchReleaseTicks--;
        return Mth.clamp(predictedRpm, IDLE_RPM, REDLINE_RPM);
    }

    private static float rpmFromSpeed(int gear, float speedKmh) {
        if (gear == 0) {
            return IDLE_RPM;
        }
        float topKmh = gear == -1
            ? REVERSE_TOP_KMH
            : GEAR_TOP_KMH[Math.min(gear, GEAR_TOP_KMH.length - 1)];
        return Mth.clamp(Math.max(IDLE_RPM, speedKmh / topKmh * REDLINE_RPM), IDLE_RPM, REDLINE_RPM);
    }

    private enum Tone {
        LOW(40.0f, 162.5f, 0.42f),
        HIGH(60.0f, 108.333336f, 0.92f);

        private final float divisor;
        private final float referenceFrequency;
        private final float baseVolume;

        Tone(float divisor, float referenceFrequency, float baseVolume) {
            this.divisor = divisor;
            this.referenceFrequency = referenceFrequency;
            this.baseVolume = baseVolume;
        }

        float volume(float speedKmh, float rpmNorm) {
            float speedBoost = Mth.clamp(speedKmh / 80.0f, 0.0f, 1.0f) * 0.35f;
            if (this == LOW) {
                float rpmCurved = (float) Math.pow(rpmNorm, 2.5);
                return baseVolume * (0.08f + rpmCurved * 0.92f + speedBoost);
            }
            return baseVolume * (0.35f + rpmNorm * 0.65f + speedBoost);
        }

        float pitch(float rpm) {
            return Mth.clamp((rpm / divisor) / referenceFrequency, 0.5f, 2.0f);
        }
    }
}
