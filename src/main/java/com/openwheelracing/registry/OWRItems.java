package com.openwheelracing.registry;

import com.openwheelracing.OpenwheelRacing;
import com.openwheelracing.content.item.PrototypeCarItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class OWRItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, OpenwheelRacing.MODID);

    public static final RegistryObject<Item> CARBON_FIBER = registerSimple("carbon_fiber");
    public static final RegistryObject<Item> CHASSIS = registerSimple("chassis");
    public static final RegistryObject<Item> ENGINE = registerSimple("engine");
    public static final RegistryObject<Item> TIRES = registerSimple("tires");
    public static final RegistryObject<Item> AERO_KIT = registerSimple("aero_kit");
    public static final RegistryObject<Item> GEARBOX = registerSimple("gearbox");
    public static final RegistryObject<Item> STEERING_CONTROLS = registerSimple("steering_controls");
    public static final RegistryObject<Item> PROTOTYPE_CAR_SPAWN = ITEMS.register("prototype_car_spawn",
        () -> new PrototypeCarItem(new Item.Properties().setId(ITEMS.key("prototype_car_spawn")))
    );

    private OWRItems() {
    }

    public static void register(BusGroup modBusGroup) {
        ITEMS.register(modBusGroup);
    }

    private static RegistryObject<Item> registerSimple(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties().setId(ITEMS.key(name))));
    }
}
