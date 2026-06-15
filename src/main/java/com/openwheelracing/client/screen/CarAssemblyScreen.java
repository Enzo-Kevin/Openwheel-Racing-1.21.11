package com.openwheelracing.client.screen;

import com.openwheelracing.content.car.CarLivery;
import com.openwheelracing.content.item.PrototypeCarItem;
import com.openwheelracing.content.menu.CarAssemblyMenu;
import com.openwheelracing.network.OWRNetwork;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class CarAssemblyScreen extends AbstractContainerScreen<CarAssemblyMenu> {
    private static final Identifier BG = Identifier.fromNamespaceAndPath("openwheelracing", "textures/gui/car_assembly_workstation.png");

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
        addLiveryButtons(66);
        addRenderableWidget(Button.builder(Component.literal("Repair"), button -> OWRNetwork.CHANNEL.send(new OWRNetwork.RepairCarMessage(), PacketDistributor.SERVER.noArg()))
            .bounds(leftPos + 126, topPos + 78, 42, 14)
            .build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        graphics.blit(BG, x, y, x + imageWidth, y + imageHeight, 0.0f, 1.0f, 0.0f, 1.0f);

        int progress = menu.getScaledProgress();
        if (progress > 0) {
            graphics.fill(x + 74, y + 44, x + 74 + progress, y + 49, 0xFF55AAFF);
        }

        graphics.drawString(font, "Assembly", x + 78, y + 30, 0xFF404040, false);
        graphics.drawString(font, "Setup", x + 126, y + 5, 0xFF404040, false);
        graphics.drawString(font, "P", x + 127, y + 16, 0xFF404040, false);
        graphics.drawString(font, "T", x + 127, y + 29, 0xFF404040, false);
        graphics.drawString(font, "A", x + 127, y + 42, 0xFF404040, false);
        graphics.drawString(font, "G", x + 127, y + 55, 0xFF404040, false);
        graphics.drawString(font, "L", x + 127, y + 68, 0xFF404040, false);
        if (!menu.getOutputStack().isEmpty()) {
            String name = CarLivery.fromIndex(PrototypeCarItem.getLivery(menu.getOutputStack())).displayName();
            graphics.drawString(font, name, x + 78, y + 72, 0xFF404040, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void addTuneButtons(int setupSlot, int yOffset) {
        addRenderableWidget(Button.builder(Component.literal("-"), button -> OWRNetwork.CHANNEL.send(new OWRNetwork.TuneCarMessage(setupSlot, -1), PacketDistributor.SERVER.noArg()))
            .bounds(leftPos + 139, topPos + yOffset, 12, 11)
            .build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> OWRNetwork.CHANNEL.send(new OWRNetwork.TuneCarMessage(setupSlot, 1), PacketDistributor.SERVER.noArg()))
            .bounds(leftPos + 158, topPos + yOffset, 12, 11)
            .build());
    }

    private void addLiveryButtons(int yOffset) {
        addRenderableWidget(Button.builder(Component.literal("-"), button -> OWRNetwork.CHANNEL.send(new OWRNetwork.CycleLiveryMessage(-1), PacketDistributor.SERVER.noArg()))
            .bounds(leftPos + 139, topPos + yOffset, 12, 11)
            .build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> OWRNetwork.CHANNEL.send(new OWRNetwork.CycleLiveryMessage(1), PacketDistributor.SERVER.noArg()))
            .bounds(leftPos + 158, topPos + yOffset, 12, 11)
            .build());
    }
}
