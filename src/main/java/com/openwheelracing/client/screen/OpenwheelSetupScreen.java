package com.openwheelracing.client.screen;

import com.openwheelracing.client.input.WheelInputSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class OpenwheelSetupScreen extends Screen {
    private static final int PANEL_WIDTH = 360;
    private final Screen parent;
    private WheelInputSettings settings;

    public OpenwheelSetupScreen(Screen parent) {
        super(Component.translatable("screen.openwheelracing.setup.title"));
        this.parent = parent;
        this.settings = WheelInputSettings.copyOfCurrent();
    }

    @Override
    protected void init() {
        clearWidgets();
        int x = (width - PANEL_WIDTH) / 2;
        int y = 42;
        int buttonWidth = 160;
        int left = x + 16;
        int right = x + 184;

        addRenderableWidget(Button.builder(hudToggleLabel("physics_debug", settings.showPhysicsDebugHud), button -> {
            settings.showPhysicsDebugHud = !settings.showPhysicsDebugHud;
            button.setMessage(hudToggleLabel("physics_debug", settings.showPhysicsDebugHud));
        }).bounds(left, y + 34, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(hudToggleLabel("ranking", settings.showRankingHud), button -> {
            settings.showRankingHud = !settings.showRankingHud;
            button.setMessage(hudToggleLabel("ranking", settings.showRankingHud));
        }).bounds(right, y + 34, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(hudToggleLabel("setup", settings.showSetupHud), button -> {
            settings.showSetupHud = !settings.showSetupHud;
            button.setMessage(hudToggleLabel("setup", settings.showSetupHud));
        }).bounds(left, y + 58, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(hudToggleLabel("driving", settings.showDrivingHud), button -> {
            settings.showDrivingHud = !settings.showDrivingHud;
            button.setMessage(hudToggleLabel("driving", settings.showDrivingHud));
        }).bounds(right, y + 58, buttonWidth, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("screen.openwheelracing.setup.wheel_setup"), button -> Minecraft.getInstance().setScreen(new WheelSetupScreen(this)))
            .bounds(left, y + 114, PANEL_WIDTH - 32, 20)
            .build());
        addRenderableWidget(Button.builder(Component.translatable("screen.openwheelracing.setup.keybind_setup"), button -> Minecraft.getInstance().setScreen(new KeyBindsScreen(this, Minecraft.getInstance().options)))
            .bounds(left, y + 138, PANEL_WIDTH - 32, 20)
            .build());

        addRenderableWidget(Button.builder(Component.translatable("screen.openwheelracing.setup.done"), button -> saveAndClose())
            .bounds(x + 96, y + 188, 76, 20)
            .build());
        addRenderableWidget(Button.builder(Component.translatable("screen.openwheelracing.setup.cancel"), button -> closeToParent())
            .bounds(x + 188, y + 188, 76, 20)
            .build());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0x88000000);
        int x = (width - PANEL_WIDTH) / 2;
        int y = 42;
        graphics.fill(x, y, x + PANEL_WIDTH, y + 220, 0xDD1F2328);
        graphics.fill(x + 6, y + 22, x + PANEL_WIDTH - 6, y + 86, 0xFF2A3038);
        graphics.fill(x + 6, y + 102, x + PANEL_WIDTH - 6, y + 166, 0xFF2F3640);
        graphics.drawString(font, title, x + 10, y + 8, 0xFFE8EDF2, false);
        graphics.drawString(font, Component.translatable("screen.openwheelracing.setup.visual"), x + 12, y + 24, 0xFFC9D1D9, false);
        graphics.drawString(font, Component.translatable("screen.openwheelracing.setup.controls"), x + 12, y + 104, 0xFFC9D1D9, false);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            closeToParent();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        closeToParent();
    }

    private Component hudToggleLabel(String hud, boolean shown) {
        return Component.translatable(
            "screen.openwheelracing.setup.hud_toggle",
            Component.translatable("screen.openwheelracing.setup.hud." + hud),
            Component.translatable(shown ? "screen.openwheelracing.setup.shown" : "screen.openwheelracing.setup.hidden")
        );
    }

    private void saveAndClose() {
        WheelInputSettings.set(settings);
        WheelInputSettings.save(Minecraft.getInstance());
        closeToParent();
    }

    private void closeToParent() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }
}
