package com.openwheelracing.client.sound;

import com.openwheelracing.content.entity.OpenwheelCarEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class CarSoundManager {
    private static final int MAX_ACTIVE_CAR_SOUNDS = 3;
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

        // Collect all cars in hearing range sorted nearest-first.
        // The player's own ridden car is always treated as distance 0.
        List<OpenwheelCarEntity> nearby = new ArrayList<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof OpenwheelCarEntity car
                    && car.distanceToSqr(player) <= CarSoundPhysics.MAX_AUDIBLE_DISTANCE_SQR) {
                nearby.add(car);
            }
        }
        nearby.sort((a, b) -> {
            double da = player.isPassenger() && player.getVehicle() == a ? -1.0 : a.distanceToSqr(player);
            double db = player.isPassenger() && player.getVehicle() == b ? -1.0 : b.distanceToSqr(player);
            return Double.compare(da, db);
        });

        // Only the closest MAX_ACTIVE_CAR_SOUNDS cars get live audio.
        Set<Integer> allowed = new HashSet<>();
        for (int i = 0; i < Math.min(MAX_ACTIVE_CAR_SOUNDS, nearby.size()); i++) {
            allowed.add(nearby.get(i).getId());
        }

        // Activate/update allowed cars; stop anything outside the allowed set.
        Set<Integer> active = new HashSet<>();
        for (OpenwheelCarEntity car : nearby) {
            if (!allowed.contains(car.getId())) continue;
            active.add(car.getId());
            SOUNDS.computeIfAbsent(car.getId(), id -> CarSoundSet.start(soundManager, car, listenerPosition))
                  .replaceCar(car);
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
