package com.openwheelracing.client.screen;

import com.openwheelracing.content.menu.RefineryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class RefineryScreen extends AbstractContainerScreen<RefineryMenu> {
    public RefineryScreen(RefineryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageHeight = 166;
        imageWidth = 176;
        inventoryLabelY = 72;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF202020);
        graphics.fill(x + 4, y + 4, x + imageWidth - 4, y + 72, 0xFF2C2C2C);
        graphics.fill(x + 4, y + 78, x + imageWidth - 4, y + imageHeight - 4, 0xFF303030);
        drawSlot(graphics, x + 35, y + 17);
        drawSlot(graphics, x + 35, y + 53);
        drawSlot(graphics, x + 116, y + 8);
        drawSlot(graphics, x + 98, y + 29);
        drawSlot(graphics, x + 116, y + 29);
        drawSlot(graphics, x + 134, y + 29);
        drawSlot(graphics, x + 116, y + 50);

        graphics.fill(x + 59, y + 31, x + 87, y + 37, 0xFF111111);
        graphics.fill(x + 61, y + 33, x + 61 + menu.getScaledProgress(), y + 35, 0xFFFFB347);
        graphics.fill(x + 38, y + 37, x + 52, y + 51, 0xFF111111);
        graphics.fill(x + 38, y + 51 - menu.getScaledBurn(), x + 52, y + 51, 0xFFFF6A00);
        graphics.drawString(font, "Crude", x + 28, y + 8, 0xFFFFFFFF, false);
        graphics.drawString(font, "Fuel", x + 31, y + 44, 0xFFFFFFFF, false);
        graphics.drawString(font, "Outputs", x + 107, y + 68, 0xFFFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF111111);
        graphics.fill(x, y, x + 16, y + 16, 0xFF777777);
    }
}
