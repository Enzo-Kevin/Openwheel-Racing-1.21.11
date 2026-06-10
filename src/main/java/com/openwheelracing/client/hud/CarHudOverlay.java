package com.openwheelracing.client.hud;

import com.openwheelracing.content.entity.OpenwheelCarEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class CarHudOverlay {
    private static final int PANEL_WIDTH = 132;
    private static final int PANEL_HEIGHT = 78;

    private CarHudOverlay() {
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !(minecraft.player.getVehicle() instanceof OpenwheelCarEntity car)) {
            return;
        }

        Font font = minecraft.font;
        int x = graphics.guiWidth() - PANEL_WIDTH - 8;
        int y = graphics.guiHeight() - PANEL_HEIGHT - 8;

        graphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0x99000000);
        graphics.renderOutline(x, y, PANEL_WIDTH, PANEL_HEIGHT, 0xFFDA1A20);
        graphics.drawString(font, String.format("SPD %3.0f km/h", car.getSpeedKmh()), x + 8, y + 7, 0xFFFFFFFF, false);
        graphics.drawString(font, "GEAR " + car.getGear(), x + 8, y + 18, 0xFFFFFFFF, false);
        graphics.drawString(font, "RPM  " + car.getRpm(), x + 8, y + 29, 0xFFFFFFFF, false);
        graphics.drawString(font, String.format("TYRE %3.0f%%", Math.max(0.0f, 100.0f - car.getTyreWearPercent())), x + 8, y + 40, car.getTyreWearPercent() > 70.0f ? 0xFFFFDD66 : 0xFFB7FFB7, false);
        graphics.drawString(font, String.format("DMG %3.0f%%", car.getDamagePercent()), x + 68, y + 40, car.getDamagePercent() > 70.0f ? 0xFFFF7777 : 0xFFFFFFFF, false);
        graphics.drawString(font, "LAP  " + formatLapTime(car.getCurrentLapTicks()), x + 8, y + 51, car.hasCheckpoint() ? 0xFFB7FFB7 : 0xFFFFFFFF, false);
        graphics.drawString(font, "BEST " + formatLapTime(car.getBestLapTicks()), x + 8, y + 62, 0xFFFFFF99, false);

        int setupX = 8;
        int setupY = graphics.guiHeight() - 56;
        graphics.fill(setupX, setupY, setupX + 98, setupY + 48, 0x99000000);
        graphics.renderOutline(setupX, setupY, 98, 48, 0xFF555555);
        graphics.drawString(font, "PWR " + car.getSetup().power(), setupX + 7, setupY + 7, 0xFFFF9999, false);
        graphics.drawString(font, "TYRE C" + (car.getSetup().grip() + 1), setupX + 7, setupY + 18, 0xFFB7FFB7, false);
        graphics.drawString(font, "AERO " + car.getSetup().aero(), setupX + 7, setupY + 29, 0xFF99DDFF, false);
        graphics.drawString(font, "GEAR " + car.getSetup().gearing(), setupX + 52, setupY + 29, 0xFFFFDD88, false);
    }

    private static String formatLapTime(int ticks) {
        if (ticks <= 0) {
            return "--:--.--";
        }
        int totalCentiseconds = ticks * 5;
        int minutes = totalCentiseconds / 6000;
        int seconds = totalCentiseconds / 100 % 60;
        int centiseconds = totalCentiseconds % 100;
        return String.format("%d:%02d.%02d", minutes, seconds, centiseconds);
    }
}
