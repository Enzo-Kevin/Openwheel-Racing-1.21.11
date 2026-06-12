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

public final class CarSoundManager {
    private static final double AUDIBLE_DISTANCE_SQR = 96.0 * 96.0;
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

        Set<Integer> active = new HashSet<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof OpenwheelCarEntity car && car.distanceToSqr(player) <= AUDIBLE_DISTANCE_SQR) {
                active.add(car.getId());
                SOUNDS.computeIfAbsent(car.getId(), id -> CarSoundSet.start(mc.getSoundManager(), car)).replaceCar(car);
            }
        }

        SoundManager soundManager = mc.getSoundManager();
        SOUNDS.entrySet().removeIf(entry -> {
            CarSoundSet soundSet = entry.getValue();
            if (!active.contains(entry.getKey()) || soundSet.isStopped()) {
                soundSet.stop(soundManager);
                return true;
            }
            return false;
        });
    }

    private static void stopAll(SoundManager soundManager) {
        SOUNDS.values().forEach(soundSet -> soundSet.stop(soundManager));
        SOUNDS.clear();
    }
}
