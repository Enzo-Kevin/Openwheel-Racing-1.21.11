package com.openwheelracing.registry;

import com.openwheelracing.OpenwheelRacing;
import com.openwheelracing.content.car.PrototypeCarSetup;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponentType;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class OWRDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, OpenwheelRacing.MODID);

    public static final RegistryObject<DataComponentType<PrototypeCarSetup>> CAR_SETUP = DATA_COMPONENTS.register("car_setup",
        () -> DataComponentType.<PrototypeCarSetup>builder()
            .persistent(PrototypeCarSetup.CODEC)
            .networkSynchronized(PrototypeCarSetup.STREAM_CODEC)
            .build()
    );

    private OWRDataComponents() {
    }

    public static void register(BusGroup modBusGroup) {
        DATA_COMPONENTS.register(modBusGroup);
    }
}
