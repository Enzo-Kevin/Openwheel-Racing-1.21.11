package com.openwheelracing.client.input;

import com.openwheelracing.content.entity.OpenwheelCarEntity;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;

public final class OWRClientInputHandler {
    private OWRClientInputHandler() {
    }

    public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !(minecraft.player.getVehicle() instanceof OpenwheelCarEntity car)) {
            return;
        }

        while (OWRKeyMappings.SHIFT_UP.consumeClick()) {
            car.shiftUp();
        }

        while (OWRKeyMappings.SHIFT_DOWN.consumeClick()) {
            car.shiftDown();
        }

        while (OWRKeyMappings.EXIT_CAR.consumeClick()) {
            minecraft.player.stopRiding();
        }
    }
}
