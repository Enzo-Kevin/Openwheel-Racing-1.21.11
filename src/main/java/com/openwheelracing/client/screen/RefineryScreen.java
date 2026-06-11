package com.openwheelracing.client.screen;

import com.openwheelracing.content.menu.RefineryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class RefineryScreen extends AbstractContainerScreen<RefineryMenu> {
    private static final Identifier BG = Identifier.fromNamespaceAndPath("openwheelracing", "textures/gui/refinery.png");

    public RefineryScreen(RefineryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageHeight = 192;
        imageWidth = 176;
        inventoryLabelY = 98;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        graphics.blit(BG, x, y, x + imageWidth, y + imageHeight, 0.0f, 1.0f, 0.0f, 1.0f);

        int progress = menu.getScaledProgress();
        if (progress > 0) {
            graphics.fill(x + 74, y + 45, x + 74 + progress, y + 49, 0xFFFFB347);
        }

        int burn = menu.getScaledBurn();
        if (burn > 0) {
            graphics.fill(x + 39, y + 58 - burn, x + 51, y + 58, 0xFFFF6A00);
        }

        graphics.drawString(font, "Crude", x + 29, y + 12, 0xFF404040, false);
        graphics.drawString(font, "Fuel", x + 33, y + 52, 0xFF404040, false);
        graphics.drawString(font, "Tower", x + 80, y + 22, 0xFF404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
