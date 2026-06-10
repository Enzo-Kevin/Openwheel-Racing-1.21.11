package com.openwheelracing.client.hud;

import com.openwheelracing.content.entity.OpenwheelCarEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;

public final class CarHudOverlay {
    private CarHudOverlay() {
    }

    public static void onCustomizeGuiOverlay(CustomizeGuiOverlayEvent.DebugText event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !(minecraft.player.getVehicle() instanceof OpenwheelCarEntity car) || event.getSide() != CustomizeGuiOverlayEvent.DebugText.Side.Left) {
            return;
        }

        event.getText().add(Component.literal(String.format("OWR SPD %.0f km/h", car.getSpeedKmh())).getString());
        event.getText().add(Component.literal("OWR GEAR " + car.getGear()).getString());
        event.getText().add(Component.literal("OWR RPM " + car.getRpm()).getString());
        event.getText().add(Component.literal(String.format("OWR TYRE %.0f%%", Math.max(0.0f, 100.0f - car.getTyreWearPercent()))).getString());
        event.getText().add(Component.literal(String.format("OWR DMG %.0f%%", car.getDamagePercent())).getString());
    }
}
