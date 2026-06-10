package com.openwheelracing;

import com.mojang.logging.LogUtils;
import com.openwheelracing.registry.OWRBlockEntities;
import com.openwheelracing.registry.OWRBlocks;
import com.openwheelracing.registry.OWRCreativeTabs;
import com.openwheelracing.registry.OWRDataComponents;
import com.openwheelracing.registry.OWREntities;
import com.openwheelracing.registry.OWRItems;
import com.openwheelracing.registry.OWRMenus;
import com.openwheelracing.registry.OWRRecipes;
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
        OWRItems.register(modBusGroup);
        OWRBlocks.register(modBusGroup);
        OWRBlockEntities.register(modBusGroup);
        OWRMenus.register(modBusGroup);
        OWRRecipes.register(modBusGroup);
        OWRCreativeTabs.register(modBusGroup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Openwheel Racing initialized");
    }
}
