package com.openwheelracing.client.sound;

import net.minecraft.world.phys.Vec3;

final class CarSoundPhysics {
    static final double MAX_AUDIBLE_DISTANCE = 112.0;
    static final double MAX_AUDIBLE_DISTANCE_SQR = MAX_AUDIBLE_DISTANCE * MAX_AUDIBLE_DISTANCE;
    static final double SPEED_OF_SOUND = 343.0;

    private CarSoundPhysics() {
    }

    static float attenuation(double distance) {
        if (distance >= MAX_AUDIBLE_DISTANCE) {
            return 0.0f;
        }
        double normalized = distance / MAX_AUDIBLE_DISTANCE;
        return (float) (1.0 / (1.0 + 3.5 * normalized * normalized));
    }

    static float doppler(float basePitch, Vec3 sourcePosition, Vec3 previousSourcePosition, Vec3 listenerPosition) {
        double sourceVelocity = sourcePosition.subtract(previousSourcePosition).length();
        if (sourceVelocity <= 0.0001) {
            return basePitch;
        }

        Vec3 sourceToListener = listenerPosition.subtract(sourcePosition);
        double distance = sourceToListener.length();
        if (distance <= 0.0001) {
            return basePitch;
        }

        Vec3 direction = sourceToListener.scale(1.0 / distance);
        Vec3 sourceVelocityVec = sourcePosition.subtract(previousSourcePosition);
        double radialSpeed = sourceVelocityVec.dot(direction);
        double ratio = SPEED_OF_SOUND / Math.max(1.0, SPEED_OF_SOUND - radialSpeed);
        return (float) (basePitch * ratio);
    }
}
