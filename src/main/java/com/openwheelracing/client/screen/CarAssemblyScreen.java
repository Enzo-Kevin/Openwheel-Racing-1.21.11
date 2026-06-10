package com.openwheelracing.client.screen;

import com.openwheelracing.content.menu.CarAssemblyMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CarAssemblyScreen extends AbstractContainerScreen<CarAssemblyMenu> {
    public CarAssemblyScreen(CarAssemblyMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageHeight = 166;
        imageWidth = 176;
        inventoryLabelY = 72;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF222222);
        graphics.fill(x + 4, y + 4, x + imageWidth - 4, y + 68, 0xFF303030);
        graphics.fill(x + 4, y + 78, x + imageWidth - 4, y + imageHeight - 4, 0xFF303030);
        graphics.hLine(x, x + imageWidth, y, 0xFF555555);
        graphics.hLine(x, x + imageWidth, y + imageHeight, 0xFF111111);

        drawSlot(graphics, x + 35, y + 17);
        drawSlot(graphics, x + 53, y + 17);
        drawSlot(graphics, x + 71, y + 17);
        drawSlot(graphics, x + 35, y + 35);
        drawSlot(graphics, x + 53, y + 35);
        drawSlot(graphics, x + 71, y + 35);
        drawSlot(graphics, x + 125, y + 26);

        graphics.fill(x + 95, y + 30, x + 119, y + 35, 0xFF111111);
        graphics.fill(x + 95, y + 30, x + 95 + menu.getScaledProgress(), y + 35, 0xFF55AAFF);
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
