package com.openwheelracing.client.sound;

import com.openwheelracing.content.entity.OpenwheelCarEntity;
import com.openwheelracing.registry.OWRSoundEvents;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

final class CarEngineSoundInstance extends AbstractTickableSoundInstance {
    private final Tone tone;
    private OpenwheelCarEntity car;
    private Vec3 listenerPosition;
    private Vec3 previousSourcePosition;

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
    }

    static CarEngineSoundInstance lowTone(OpenwheelCarEntity car, Vec3 listenerPosition) {
        return new CarEngineSoundInstance(car, listenerPosition, OWRSoundEvents.CAR_ENGINE_LOW.get(), Tone.LOW);
    }

    static CarEngineSoundInstance highTone(OpenwheelCarEntity car, Vec3 listenerPosition) {
        return new CarEngineSoundInstance(car, listenerPosition, OWRSoundEvents.CAR_ENGINE_HIGH.get(), Tone.HIGH);
    }

    void replaceCar(OpenwheelCarEntity car) {
        this.car = car;
    }

    void updateListener(Vec3 listenerPosition) {
        this.listenerPosition = listenerPosition;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

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

        float rpm = car.getRpm();
        float speed = car.getSpeedKmh();
        float rpmNorm = Mth.clamp((rpm - 900.0f) / 11600.0f, 0.0f, 1.0f);
        float basePitch = tone.pitch(rpm);
        float distance = (float) listenerPosition.distanceTo(new Vec3(x, y, z));
        volume = tone.volume(speed, rpmNorm) * CarSoundPhysics.attenuation(distance);
        pitch = CarSoundPhysics.doppler(basePitch, new Vec3(x, y, z), previousSourcePosition, listenerPosition);
    }

    private enum Tone {
        LOW(40.0f, 162.5f, 0.42f),
        HIGH(60.0f, 108.333336f, 0.26f);

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
            return baseVolume * (0.35f + rpmNorm * 0.65f + speedBoost);
        }

        float pitch(float rpm) {
            return Mth.clamp((rpm / divisor) / referenceFrequency, 0.5f, 2.0f);
        }
    }
}
