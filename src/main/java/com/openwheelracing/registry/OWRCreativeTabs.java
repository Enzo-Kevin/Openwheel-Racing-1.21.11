package com.openwheelracing.registry;

import com.openwheelracing.OpenwheelRacing;
import com.openwheelracing.content.item.PrototypeCarItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class OWRCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OpenwheelRacing.MODID);

    public static final RegistryObject<CreativeModeTab> OPENWHEEL_RACING = CREATIVE_MODE_TABS.register("openwheel_racing", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.openwheelracing.openwheel_racing"))
        .icon(PrototypeCarItem::createWithDefaultSetup)
        .displayItems((parameters, output) -> {
            output.accept(OWRBlocks.CAR_ASSEMBLY_WORKSTATION_ITEM.get());
            output.accept(OWRBlocks.REFINERY_ITEM.get());
            output.accept(OWRBlocks.CRUDE_OIL_DEPOSIT_ITEM.get());
            output.accept(OWRBlocks.ASPHALT_TRACK_ITEM.get());
            output.accept(OWRBlocks.KERB_ITEM.get());
            output.accept(OWRBlocks.BARRIER_ITEM.get());
            output.accept(OWRBlocks.PIT_LANE_ITEM.get());
            output.accept(OWRBlocks.START_FINISH_ITEM.get());
            output.accept(OWRBlocks.CHECKPOINT_ITEM.get());
            output.accept(OWRItems.CARBON_FIBER.get());
            output.accept(OWRItems.CRUDE_OIL_CHUNK.get());
            output.accept(OWRItems.CRUDE_OIL_BUCKET.get());
            output.accept(OWRItems.GAS.get());
            output.accept(OWRItems.PETROL_CAN.get());
            output.accept(OWRItems.DIESEL_CAN.get());
            output.accept(OWRItems.CRUDE_RUBBER.get());
            output.accept(OWRItems.RUBBER.get());
            output.accept(OWRItems.ASPHALT_BINDER.get());
            output.accept(OWRItems.PLASTIC.get());
            output.accept(OWRItems.RACING_ELECTRONICS.get());
            output.accept(OWRItems.CHASSIS.get());
            output.accept(OWRItems.ENGINE.get());
            output.accept(OWRItems.TIRES.get());
            output.accept(OWRItems.AERO_KIT.get());
            output.accept(OWRItems.GEARBOX.get());
            output.accept(OWRItems.STEERING_CONTROLS.get());
            output.accept(PrototypeCarItem.createWithDefaultSetup());
        })
        .build()
    );

    private OWRCreativeTabs() {
    }

    public static void register(BusGroup modBusGroup) {
        CREATIVE_MODE_TABS.register(modBusGroup);
    }
}
