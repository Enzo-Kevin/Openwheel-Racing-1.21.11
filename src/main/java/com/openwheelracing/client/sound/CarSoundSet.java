package com.openwheelracing.client.sound;

import com.openwheelracing.content.entity.OpenwheelCarEntity;
import net.minecraft.client.sounds.SoundManager;

final class CarSoundSet {
    private final CarEngineSoundInstance engineLow;
    private final CarEngineSoundInstance engineHigh;
    private final CarTyreSoundInstance frontLeft;
    private final CarTyreSoundInstance frontRight;
    private final CarTyreSoundInstance rearLeft;
    private final CarTyreSoundInstance rearRight;

    private CarSoundSet(OpenwheelCarEntity car) {
        engineLow = CarEngineSoundInstance.lowTone(car);
        engineHigh = CarEngineSoundInstance.highTone(car);
        frontLeft = CarTyreSoundInstance.frontLeft(car);
        frontRight = CarTyreSoundInstance.frontRight(car);
        rearLeft = CarTyreSoundInstance.rearLeft(car);
        rearRight = CarTyreSoundInstance.rearRight(car);
    }

    static CarSoundSet start(SoundManager soundManager, OpenwheelCarEntity car) {
        CarSoundSet soundSet = new CarSoundSet(car);
        soundManager.play(soundSet.engineLow);
        soundManager.play(soundSet.engineHigh);
        soundManager.play(soundSet.frontLeft);
        soundManager.play(soundSet.frontRight);
        soundManager.play(soundSet.rearLeft);
        soundManager.play(soundSet.rearRight);
        return soundSet;
    }

    void replaceCar(OpenwheelCarEntity car) {
        engineLow.replaceCar(car);
        engineHigh.replaceCar(car);
        frontLeft.replaceCar(car);
        frontRight.replaceCar(car);
        rearLeft.replaceCar(car);
        rearRight.replaceCar(car);
    }

    boolean isStopped() {
        return engineLow.isStopped() || engineHigh.isStopped();
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
