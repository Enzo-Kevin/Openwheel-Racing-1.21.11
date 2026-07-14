package com.openwheelracing.client.sound;

import com.openwheelracing.content.entity.OpenwheelCarEntity;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.phys.Vec3;

final class CarSoundSet {
    private CarEngineSoundInstance engineLow;
    private CarEngineSoundInstance engineHigh;
    private final CarTyreSoundInstance frontLeft;
    private final CarTyreSoundInstance frontRight;
    private final CarTyreSoundInstance rearLeft;
    private final CarTyreSoundInstance rearRight;

    private OpenwheelCarEntity car;
    private Vec3 listenerPosition;

    private CarSoundSet(OpenwheelCarEntity car, Vec3 listenerPosition) {
        this.car = car;
        this.listenerPosition = listenerPosition;
        engineLow = CarEngineSoundInstance.lowTone(car, listenerPosition);
        engineHigh = CarEngineSoundInstance.highTone(car, listenerPosition);
        frontLeft = CarTyreSoundInstance.frontLeft(car, listenerPosition);
        frontRight = CarTyreSoundInstance.frontRight(car, listenerPosition);
        rearLeft = CarTyreSoundInstance.rearLeft(car, listenerPosition);
        rearRight = CarTyreSoundInstance.rearRight(car, listenerPosition);
    }

    static CarSoundSet start(SoundManager soundManager, OpenwheelCarEntity car, Vec3 listenerPosition) {
        CarSoundSet soundSet = new CarSoundSet(car, listenerPosition);
        soundManager.play(soundSet.engineLow);
        soundManager.play(soundSet.engineHigh);
        soundManager.play(soundSet.frontLeft);
        soundManager.play(soundSet.frontRight);
        soundManager.play(soundSet.rearLeft);
        soundManager.play(soundSet.rearRight);
        return soundSet;
    }

    void replaceCar(OpenwheelCarEntity car) {
        this.car = car;
        engineLow.replaceCar(car);
        engineHigh.replaceCar(car);
        frontLeft.replaceCar(car);
        frontRight.replaceCar(car);
        rearLeft.replaceCar(car);
        rearRight.replaceCar(car);
    }

    void updateListener(Vec3 listenerPosition) {
        this.listenerPosition = listenerPosition;
        engineLow.updateListener(listenerPosition);
        engineHigh.updateListener(listenerPosition);
        frontLeft.updateListener(listenerPosition);
        frontRight.updateListener(listenerPosition);
        rearLeft.updateListener(listenerPosition);
        rearRight.updateListener(listenerPosition);
    }

    // Restart any engine track the sound pool silently evicted.
    // Tyre instances are non-critical so we leave them alone.
    void repairEngines(SoundManager soundManager) {
        if (engineLow.isStopped()) {
            engineLow = CarEngineSoundInstance.lowTone(car, listenerPosition);
            soundManager.play(engineLow);
        }
        if (engineHigh.isStopped()) {
            engineHigh = CarEngineSoundInstance.highTone(car, listenerPosition);
            soundManager.play(engineHigh);
        }
    }

    boolean isEntityGone() {
        return car == null || !car.isAlive() || car.isRemoved();
    }

    void stop(SoundManager soundManager) {
        soundManager.stop(engineLow);
        soundManager.stop(engineHigh);
        soundManager.stop(frontLeft);
        soundManager.stop(frontRight);
        soundManager.stop(rearLeft);
        soundManager.stop(rearRight);
    }
}
