package com.openwheelracing.client.sound;

import com.openwheelracing.content.entity.OpenwheelCarEntity;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.phys.Vec3;

final class CarSoundSet {
    private final CarEngineSoundInstance[] engines = new CarEngineSoundInstance[CarEngineSoundInstance.sampleCount()];
    private final CarTyreSoundInstance frontLeft;
    private final CarTyreSoundInstance frontRight;
    private final CarTyreSoundInstance rearLeft;
    private final CarTyreSoundInstance rearRight;

    private OpenwheelCarEntity car;
    private Vec3 listenerPosition;

    private CarSoundSet(OpenwheelCarEntity car, Vec3 listenerPosition) {
        this.car = car;
        this.listenerPosition = listenerPosition;
        frontLeft = CarTyreSoundInstance.frontLeft(car, listenerPosition);
        frontRight = CarTyreSoundInstance.frontRight(car, listenerPosition);
        rearLeft = CarTyreSoundInstance.rearLeft(car, listenerPosition);
        rearRight = CarTyreSoundInstance.rearRight(car, listenerPosition);
    }

    static CarSoundSet start(SoundManager soundManager, OpenwheelCarEntity car, Vec3 listenerPosition) {
        CarSoundSet soundSet = new CarSoundSet(car, listenerPosition);
        soundSet.updateEngines(soundManager);
        soundManager.play(soundSet.frontLeft);
        soundManager.play(soundSet.frontRight);
        soundManager.play(soundSet.rearLeft);
        soundManager.play(soundSet.rearRight);
        return soundSet;
    }

    void replaceCar(OpenwheelCarEntity car) {
        this.car = car;
        for (CarEngineSoundInstance engine : engines) {
            if (engine != null) {
                engine.replaceCar(car);
            }
        }
        frontLeft.replaceCar(car);
        frontRight.replaceCar(car);
        rearLeft.replaceCar(car);
        rearRight.replaceCar(car);
    }

    void updateListener(Vec3 listenerPosition) {
        this.listenerPosition = listenerPosition;
        for (CarEngineSoundInstance engine : engines) {
            if (engine != null) {
                engine.updateListener(listenerPosition);
            }
        }
        frontLeft.updateListener(listenerPosition);
        frontRight.updateListener(listenerPosition);
        rearLeft.updateListener(listenerPosition);
        rearRight.updateListener(listenerPosition);
    }

    void updateEngines(SoundManager soundManager) {
        float rpm = CarEngineSoundInstance.displayedRpm(car);
        int first = CarEngineSoundInstance.firstAudibleSample(rpm);
        int second = CarEngineSoundInstance.secondAudibleSample(rpm);

        for (int i = 0; i < engines.length; i++) {
            if (i != first && i != second && engines[i] != null) {
                soundManager.stop(engines[i]);
                engines[i] = null;
            }
        }
        ensureEngine(soundManager, first);
        if (second >= 0) {
            ensureEngine(soundManager, second);
        }
    }

    private void ensureEngine(SoundManager soundManager, int sampleIndex) {
        if (sampleIndex < 0) {
            return;
        }
        CarEngineSoundInstance engine = engines[sampleIndex];
        if (engine == null || engine.isStopped()) {
            engines[sampleIndex] = CarEngineSoundInstance.rpmSample(car, listenerPosition, sampleIndex);
            soundManager.play(engines[sampleIndex]);
        }
    }

    boolean isEntityGone() {
        return car == null || !car.isAlive() || car.isRemoved();
    }

    void stop(SoundManager soundManager) {
        for (CarEngineSoundInstance engine : engines) {
            if (engine != null) {
                soundManager.stop(engine);
            }
        }
        soundManager.stop(frontLeft);
        soundManager.stop(frontRight);
        soundManager.stop(rearLeft);
        soundManager.stop(rearRight);
    }
}
