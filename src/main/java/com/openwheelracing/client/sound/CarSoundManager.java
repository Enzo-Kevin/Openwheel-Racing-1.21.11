package com.openwheelracing.client.sound;

import com.openwheelracing.content.entity.OpenwheelCarEntity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class CarSoundManager {
    private static final Map<Integer, CarSoundSet> SOUNDS = new HashMap<>();

    private CarSoundManager() {
    }

    public static void onClientTick() {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        if (level == null || player == null) {
            stopAll(mc.getSoundManager());
            return;
        }

        SoundManager soundManager = mc.getSoundManager();
        Vec3 listenerPosition = player.position();
        Set<Integer> active = new HashSet<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof OpenwheelCarEntity car) {
                double distanceSqr = car.distanceToSqr(player);
                if (distanceSqr > CarSoundPhysics.MAX_AUDIBLE_DISTANCE_SQR) {
                    continue;
                }
                active.add(car.getId());
                SOUNDS.computeIfAbsent(car.getId(), id -> CarSoundSet.start(soundManager, car, listenerPosition)).replaceCar(car);
            }
        }

        SOUNDS.entrySet().removeIf(entry -> {
            CarSoundSet soundSet = entry.getValue();
            if (!active.contains(entry.getKey()) || soundSet.isStopped()) {
                soundSet.stop(soundManager);
                return true;
            }
            soundSet.updateListener(listenerPosition);
            return false;
        });
    }

    private static void stopAll(SoundManager soundManager) {
        SOUNDS.values().forEach(soundSet -> soundSet.stop(soundManager));
        SOUNDS.clear();
    }
}
