package com.openwheelracing.client;

import com.openwheelracing.OpenwheelRacing;
import com.openwheelracing.client.hud.CarHudOverlay;
import com.openwheelracing.client.input.OWRClientInputHandler;
import com.openwheelracing.client.input.OWRKeyMappings;
import com.openwheelracing.client.screen.WheelSetupScreen;
import com.openwheelracing.client.sound.CarSoundManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OpenwheelRacing.MODID, value = Dist.CLIENT)
public final class OpenwheelRacingClientEvents {
    private static final Identifier CAR_HUD = Identifier.fromNamespaceAndPath(OpenwheelRacing.MODID, "car_hud");

    private OpenwheelRacingClientEvents() {
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        OWRKeyMappings.register(event);
    }

    @SubscribeEvent
    public static void onAddGuiOverlayLayers(AddGuiOverlayLayersEvent event) {
        event.getLayeredDraw().add(CAR_HUD, CarHudOverlay::render);
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof PauseScreen)) {
            return;
        }
        event.addListener(Button.builder(Component.translatable("screen.openwheelracing.wheel_setup.open"), button -> Minecraft.getInstance().setScreen(new WheelSetupScreen(screen)))
            .bounds(screen.width - 142, screen.height - 28, 134, 20)
            .build());
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        OWRClientInputHandler.onClientTick(event);
        CarSoundManager.onClientTick();
    }
}
