package com.openwheelracing;

import com.mojang.logging.LogUtils;
import com.openwheelracing.content.command.OWRCommands;
import com.openwheelracing.network.OWRNetwork;
import com.openwheelracing.registry.OWRBlockEntities;
import com.openwheelracing.registry.OWRBlocks;
import com.openwheelracing.registry.OWRCreativeTabs;
import com.openwheelracing.registry.OWRDataComponents;
import com.openwheelracing.registry.OWREntities;
import com.openwheelracing.registry.OWRFluids;
import com.openwheelracing.registry.OWRFuelHandler;
import com.openwheelracing.registry.OWRItems;
import com.openwheelracing.registry.OWRMenus;
import com.openwheelracing.registry.OWRRecipes;
import com.openwheelracing.registry.OWRSoundEvents;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(OpenwheelRacing.MODID)
public final class OpenwheelRacing {
    public static final String MODID = "openwheelracing";

    private static final Logger LOGGER = LogUtils.getLogger();

    public OpenwheelRacing(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();

        FMLCommonSetupEvent.getBus(modBusGroup).addListener(this::commonSetup);
        OWRDataComponents.register(modBusGroup);
        OWREntities.register(modBusGroup);
        OWRFluids.register(modBusGroup);
        OWRItems.register(modBusGroup);
        OWRBlocks.register(modBusGroup);
        OWRBlockEntities.register(modBusGroup);
        OWRMenus.register(modBusGroup);
        OWRRecipes.register(modBusGroup);
        OWRSoundEvents.register(modBusGroup);
        OWRCreativeTabs.register(modBusGroup);
        FurnaceFuelBurnTimeEvent.BUS.addListener(OWRFuelHandler::onFuelBurnTime);
        RegisterCommandsEvent.BUS.addListener(OWRCommands::register);
        PlayerEvent.PlayerLoggedInEvent.BUS.addListener(this::onPlayerLoggedIn);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        OWRNetwork.register();
        LOGGER.info("Openwheel Racing initialized");
    }

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return;
        }
        net.minecraft.server.MinecraftServer server = serverPlayer.level().getServer();
        if (server == null) {
            return;
        }
        net.minecraft.server.level.ServerLevel overworld = server.getLevel(net.minecraft.world.level.Level.OVERWORLD);
        if (overworld != null) {
            OWRNetwork.broadcastRankingBoard(server, overworld);
        }
    }
}
