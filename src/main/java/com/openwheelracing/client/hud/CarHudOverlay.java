package com.openwheelracing.client.hud;

import com.openwheelracing.content.entity.OpenwheelCarEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class CarHudOverlay {
    private static final int PANEL_WIDTH = 132;
    private static final int PANEL_HEIGHT = 89;

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
        graphics.drawString(font, "ABS  " + (car.isAbsEnabled() ? "ON" : "OFF"), x + 68, y + 51, car.isAbsEnabled() ? 0xFFB7FFB7 : 0xFFFFDD66, false);
        graphics.drawString(font, "LAP  " + formatLapTime(car.getCurrentLapTicks()), x + 8, y + 51, 0xFFFFFFFF, false);
        graphics.drawString(font, "CP   " + (car.hasCheckpoint() ? "OK" : "--"), x + 8, y + 62, car.hasCheckpoint() ? 0xFFB7FFB7 : 0xFFFFDD66, false);
        graphics.drawString(font, "BEST " + formatLapTime(car.getBestLapTicks()), x + 8, y + 73, 0xFFFFFF99, false);

        int setupX = 8;
        int setupY = graphics.guiHeight() - 89;
        graphics.fill(setupX, setupY, setupX + 172, setupY + 81, 0x99000000);
        graphics.renderOutline(setupX, setupY, 172, 81, 0xFF555555);
        graphics.drawString(font, "PWR " + car.getSetup().power(), setupX + 7, setupY + 7, 0xFFFF9999, false);
        graphics.drawString(font, "TYRE C" + (car.getTyreCompound() + 1), setupX + 7, setupY + 18, 0xFFB7FFB7, false);
        graphics.drawString(font, "AERO " + car.getSetup().aero(), setupX + 7, setupY + 29, 0xFF99DDFF, false);
        graphics.drawString(font, "GEAR " + car.getSetup().gearing(), setupX + 52, setupY + 29, 0xFFFFDD88, false);
        graphics.drawString(font, Component.translatable("hud.openwheelracing.controls.drive"), setupX + 7, setupY + 43, 0xFFDDDDDD, false);
        graphics.drawString(font, Component.translatable("hud.openwheelracing.controls.shift"), setupX + 7, setupY + 54, 0xFFDDDDDD, false);
        graphics.drawString(font, Component.translatable("hud.openwheelracing.controls.exit"), setupX + 7, setupY + 65, 0xFFDDDDDD, false);

        renderPhysicsDebug(graphics, font, car);

        if (car.isInPitStop()) {
            int remaining = car.getPitStopTicks();
            int pct = 100 - (remaining * 100 / 60);
            int barWidth = 116;
            int barX = x - 54;
            int barY = y - 20;
            graphics.fill(barX, barY, barX + barWidth, barY + 12, 0x99000000);
            graphics.fill(barX + 1, barY + 1, barX + 1 + barWidth * pct / 100, barY + 11, 0xFFDA1A20);
            graphics.drawString(font, "PIT STOP  " + (remaining / 20 + 1) + "s", barX + 4, barY + 2, 0xFFFFFFFF, false);
        }
    }

    private static void renderPhysicsDebug(GuiGraphics graphics, Font font, OpenwheelCarEntity car) {
        int x = 4;
        int y = 4;
        int lineHeight = 8;
        int row = y;
        debugLine(graphics, font, x, row, 0xFF99DDFF, "OWR Phys"); row += lineHeight;
        debugLine(graphics, font, x, row, 0xFFFFFFFF, String.format("spd %.1f rpm %d g%d", car.getSpeedKmh(), car.getRpm(), car.getGear())); row += lineHeight;
        debugLine(graphics, font, x, row, 0xFFFFFFFF, String.format("vL %.2f vY %.2f yaw %.3f", car.getDebugVelocityLong(), car.getDebugVelocityLat(), car.getDebugYawRate())); row += lineHeight;
        debugLine(graphics, font, x, row, 0xFFFFFFFF, String.format("steer %.1f slip %.2f", car.getFrontWheelSteerDegrees(), car.getTyreSlipIntensity())); row += lineHeight;
        debugLine(graphics, font, x, row, 0xFFFFDD88, String.format("drv %.0f drag %.0f df %.0f", car.getDebugDriveForce(), car.getDebugDragForce(), car.getDebugDownforce())); row += lineHeight;
        debugLine(graphics, font, x, row, 0xFFB7FFB7, String.format("Fx F %.0f R %.0f", car.getDebugFrontLongForce(), car.getDebugRearLongForce())); row += lineHeight;
        debugLine(graphics, font, x, row, 0xFFB7FFB7, String.format("Fy F %.0f R %.0f", car.getDebugFrontLatForce(), car.getDebugRearLatForce())); row += lineHeight;
        debugLine(graphics, font, x, row, 0xFFDDDDDD, String.format("Fz F %.0f R %.0f", car.getDebugFrontLoad(), car.getDebugRearLoad())); row += lineHeight;
        debugLine(graphics, font, x, row, demandColor(car.getDebugFrontDemand()), String.format("dem F %.2f R %.2f", car.getDebugFrontDemand(), car.getDebugRearDemand())); row += lineHeight;
        debugLine(graphics, font, x, row, 0xFFFFAAAA, String.format("aSlip F %.1f R %.1f", car.getDebugFrontSlipAngleDegrees(), car.getDebugRearSlipAngleDegrees()));
    }

    private static void debugLine(GuiGraphics graphics, Font font, int x, int y, int color, String text) {
        graphics.drawString(font, text, x, y, color, true);
    }

    private static int demandColor(double demand) {
        if (demand > 1.25) {
            return 0xFFFF7777;
        }
        if (demand > 1.0) {
            return 0xFFFFDD66;
        }
        return 0xFFB7FFB7;
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
