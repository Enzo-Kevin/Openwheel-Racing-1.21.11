package com.openwheelracing.client;

import com.openwheelracing.OpenwheelRacing;
import com.openwheelracing.client.hud.CarHudOverlay;
import com.openwheelracing.client.input.OWRClientInputHandler;
import com.openwheelracing.client.render.OpenwheelCarRenderer;
import com.openwheelracing.client.screen.CarAssemblyScreen;
import com.openwheelracing.registry.OWREntities;
import com.openwheelracing.registry.OWRMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = OpenwheelRacing.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class OpenwheelRacingClient {
    private OpenwheelRacingClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(OWRMenus.CAR_ASSEMBLY.get(), CarAssemblyScreen::new);
            EntityRenderers.register(OWREntities.PROTOTYPE_CAR.get(), OpenwheelCarRenderer::new);
        });

        TickEvent.ClientTickEvent.Post.BUS.addListener(OWRClientInputHandler::onClientTick);
        CustomizeGuiOverlayEvent.DebugText.BUS.addListener(CarHudOverlay::onCustomizeGuiOverlay);
    }
}
