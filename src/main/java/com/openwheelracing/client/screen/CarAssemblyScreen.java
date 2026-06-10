package com.openwheelracing.client.screen;

import com.openwheelracing.content.menu.CarAssemblyMenu;
import com.openwheelracing.network.OWRNetwork;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CarAssemblyScreen extends AbstractContainerScreen<CarAssemblyMenu> {
    public CarAssemblyScreen(CarAssemblyMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageHeight = 178;
        imageWidth = 176;
        inventoryLabelY = 84;
    }

    @Override
    protected void init() {
        super.init();
        addTuneButtons(0, 14);
        addTuneButtons(1, 27);
        addTuneButtons(2, 40);
        addTuneButtons(3, 53);
        addRenderableWidget(Button.builder(Component.literal("Repair"), button -> OWRNetwork.CHANNEL.send(new OWRNetwork.RepairCarMessage(), PacketDistributor.SERVER.noArg()))
            .bounds(leftPos + 123, topPos + 70, 45, 14)
            .build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF222222);
        graphics.fill(x + 4, y + 4, x + imageWidth - 4, y + 86, 0xFF303030);
        graphics.fill(x + 4, y + 90, x + imageWidth - 4, y + imageHeight - 4, 0xFF303030);
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
        graphics.drawString(font, "Assembly", x + 89, y + 20, 0xFFFFFFFF, false);
        graphics.drawString(font, "Setup", x + 123, y + 5, 0xFFFFFFFF, false);
        graphics.drawString(font, "P", x + 124, y + 16, 0xFFFFFFFF, false);
        graphics.drawString(font, "T", x + 124, y + 29, 0xFFFFFFFF, false);
        graphics.drawString(font, "A", x + 124, y + 42, 0xFFFFFFFF, false);
        graphics.drawString(font, "G", x + 124, y + 55, 0xFFFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void addTuneButtons(int setupSlot, int yOffset) {
        addRenderableWidget(Button.builder(Component.literal("-"), button -> OWRNetwork.CHANNEL.send(new OWRNetwork.TuneCarMessage(setupSlot, -1), PacketDistributor.SERVER.noArg()))
            .bounds(leftPos + 136, topPos + yOffset, 12, 11)
            .build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> OWRNetwork.CHANNEL.send(new OWRNetwork.TuneCarMessage(setupSlot, 1), PacketDistributor.SERVER.noArg()))
            .bounds(leftPos + 156, topPos + yOffset, 12, 11)
            .build());
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF111111);
        graphics.fill(x, y, x + 16, y + 16, 0xFF777777);
    }
}
