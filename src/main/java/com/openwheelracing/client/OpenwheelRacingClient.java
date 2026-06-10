package com.openwheelracing.client;

import com.openwheelracing.OpenwheelRacing;
import com.openwheelracing.client.hud.CarHudOverlay;
import com.openwheelracing.client.input.OWRClientInputHandler;
import com.openwheelracing.client.render.OpenwheelCarRenderer;
import com.openwheelracing.client.screen.CarAssemblyScreen;
import com.openwheelracing.client.screen.RefineryScreen;
import com.openwheelracing.registry.OWREntities;
import com.openwheelracing.registry.OWRMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.gui.overlay.ForgeLayeredDraw;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = OpenwheelRacing.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class OpenwheelRacingClient {
    private static final Identifier CAR_HUD = Identifier.fromNamespaceAndPath(OpenwheelRacing.MODID, "car_hud");

    private OpenwheelRacingClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(OWRMenus.CAR_ASSEMBLY.get(), CarAssemblyScreen::new);
            MenuScreens.register(OWRMenus.REFINERY.get(), RefineryScreen::new);
            EntityRenderers.register(OWREntities.PROTOTYPE_CAR.get(), OpenwheelCarRenderer::new);
        });

        TickEvent.ClientTickEvent.Post.BUS.addListener(OWRClientInputHandler::onClientTick);
        AddGuiOverlayLayersEvent.BUS.addListener(overlayEvent -> overlayEvent.getLayeredDraw().addAbove(ForgeLayeredDraw.HOTBAR_AND_DECOS, CAR_HUD, CarHudOverlay::render));
    }
}
