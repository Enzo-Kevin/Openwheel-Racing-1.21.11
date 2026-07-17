package com.openwheelracing.client.screen;

import com.openwheelracing.content.car.CarLivery;
import com.openwheelracing.content.car.CarLiveryColors;
import com.openwheelracing.content.item.PrototypeCarItem;
import com.openwheelracing.content.menu.CarAssemblyMenu;
import com.openwheelracing.network.OWRNetwork;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CarAssemblyScreen extends AbstractContainerScreen<CarAssemblyMenu> {
    private static final int[] WORKSTATION_SLOT_X = {52, 52, 18, 52, 86, 86, 130};
    private static final int[] WORKSTATION_SLOT_Y = {36, 70, 53, 16, 70, 36, 45};

    public CarAssemblyScreen(CarAssemblyMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageHeight = 218;
        imageWidth = 256;
        inventoryLabelY = 124;
    }

    @Override
    protected void init() {
        super.init();
        addTuneButtons(0, 28);
        addTuneButtons(1, 43);
        addTuneButtons(2, 58);
        addTuneButtons(3, 73);
        addLiveryButtons(88);
        addLiveryColorButtons(116);
        addRenderableWidget(Button.builder(Component.literal("Repair"), button -> OWRNetwork.CHANNEL.send(new OWRNetwork.RepairCarMessage(), PacketDistributor.SERVER.noArg()))
            .bounds(leftPos + 190, topPos + 98, 52, 14)
            .build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);
        graphics.fill(x + 6, y + 6, x + 172, y + 114, 0xFFDADADA);
        graphics.fill(x + 178, y + 6, x + 250, y + 114, 0xFFE0E0E0);
        graphics.fill(x + 6, y + 120, x + 250, y + 210, 0xFFD0D0D0);
        graphics.fill(x + 70, y + 44, x + 95, y + 49, 0xFF55555A);
        for (int slot = 0; slot < WORKSTATION_SLOT_X.length; slot++) {
            drawSlot(graphics, x + WORKSTATION_SLOT_X[slot], y + WORKSTATION_SLOT_Y[slot]);
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlot(graphics, x + 46 + column * 18, y + 136 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawSlot(graphics, x + 46 + column * 18, y + 194);
        }

        int progress = menu.getScaledProgress();
        if (progress > 0) {
            graphics.fill(x + 96, y + 44, x + 96 + progress, y + 49, 0xFF55AAFF);
        }

        graphics.drawString(font, title, x + 10, y + 10, 0xFF404040, false);
        graphics.drawString(font, "Assembly", x + 98, y + 32, 0xFF404040, false);
        graphics.drawString(font, "Setup", x + 190, y + 10, 0xFF404040, false);
        graphics.drawString(font, "P", x + 190, y + 30, 0xFF404040, false);
        graphics.drawString(font, "T", x + 190, y + 45, 0xFF404040, false);
        graphics.drawString(font, "A", x + 190, y + 60, 0xFF404040, false);
        graphics.drawString(font, "G", x + 190, y + 75, 0xFF404040, false);
        graphics.drawString(font, "Livery", x + 190, y + 90, 0xFF404040, false);
        graphics.drawString(font, playerInventoryTitle, x + 8, y + inventoryLabelY, 0xFF404040, false);
        if (!menu.getOutputStack().isEmpty()) {
            String name = CarLivery.fromIndex(PrototypeCarItem.getLivery(menu.getOutputStack())).displayName();
            graphics.drawString(font, name, x + 190, y + 104, 0xFF404040, false);
            CarLiveryColors colors = PrototypeCarItem.getLiveryColors(menu.getOutputStack());
            graphics.drawString(font, "B " + CarLiveryColors.colorName(colors.body()), x + 190, y + 128, colors.bodySide(), false);
            graphics.drawString(font, "A1 " + CarLiveryColors.colorName(colors.accent1()), x + 190, y + 139, colors.accent1Side(), false);
            graphics.drawString(font, "A2 " + CarLiveryColors.colorName(colors.accent2()), x + 190, y + 150, colors.accent2Side(), false);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF555555);
        graphics.fill(x, y, x + 18, y + 18, 0xFFFFFFFF);
        graphics.fill(x, y, x + 17, y + 17, 0xFF8B8B8B);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFFEFEFEF);
    }

    private void addTuneButtons(int setupSlot, int yOffset) {
        addRenderableWidget(Button.builder(Component.literal("-"), button -> OWRNetwork.CHANNEL.send(new OWRNetwork.TuneCarMessage(setupSlot, -1), PacketDistributor.SERVER.noArg()))
            .bounds(leftPos + 214, topPos + yOffset, 12, 11)
            .build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> OWRNetwork.CHANNEL.send(new OWRNetwork.TuneCarMessage(setupSlot, 1), PacketDistributor.SERVER.noArg()))
            .bounds(leftPos + 232, topPos + yOffset, 12, 11)
            .build());
    }

    private void addLiveryButtons(int yOffset) {
        addRenderableWidget(Button.builder(Component.literal("-"), button -> OWRNetwork.CHANNEL.send(new OWRNetwork.CycleLiveryMessage(-1), PacketDistributor.SERVER.noArg()))
            .bounds(leftPos + 214, topPos + yOffset, 12, 11)
            .build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> OWRNetwork.CHANNEL.send(new OWRNetwork.CycleLiveryMessage(1), PacketDistributor.SERVER.noArg()))
            .bounds(leftPos + 232, topPos + yOffset, 12, 11)
            .build());
    }

    private void addLiveryColorButtons(int yOffset) {
        addRenderableWidget(Button.builder(Component.literal("B"), button -> OWRNetwork.CHANNEL.send(new OWRNetwork.CycleLiveryColorMessage(0, 1), PacketDistributor.SERVER.noArg()))
            .bounds(leftPos + 190, topPos + yOffset, 12, 11)
            .build());
        addRenderableWidget(Button.builder(Component.literal("A1"), button -> OWRNetwork.CHANNEL.send(new OWRNetwork.CycleLiveryColorMessage(1, 1), PacketDistributor.SERVER.noArg()))
            .bounds(leftPos + 207, topPos + yOffset, 14, 11)
            .build());
        addRenderableWidget(Button.builder(Component.literal("A2"), button -> OWRNetwork.CHANNEL.send(new OWRNetwork.CycleLiveryColorMessage(2, 1), PacketDistributor.SERVER.noArg()))
            .bounds(leftPos + 232, topPos + yOffset, 14, 11)
            .build());
    }
}
