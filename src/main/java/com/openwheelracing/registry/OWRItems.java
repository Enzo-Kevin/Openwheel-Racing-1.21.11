package com.openwheelracing.registry;

import com.openwheelracing.OpenwheelRacing;
import com.openwheelracing.content.item.PrototypeCarItem;
import com.openwheelracing.content.item.RaceControlItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class OWRItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, OpenwheelRacing.MODID);

    public static final RegistryObject<Item> CARBON_FIBER = registerSimple("carbon_fiber");
    public static final RegistryObject<Item> RUBBER = registerSimple("rubber");
    public static final RegistryObject<Item> CRUDE_RUBBER = registerSimple("crude_rubber");
    public static final RegistryObject<Item> CRUDE_OIL_CHUNK = registerSimple("crude_oil_chunk");
    public static final RegistryObject<Item> CRUDE_OIL_BUCKET = ITEMS.register("crude_oil_bucket",
        () -> new Item(new Item.Properties().setId(ITEMS.key("crude_oil_bucket")).craftRemainder(Items.BUCKET).stacksTo(1))
    );
    public static final RegistryObject<Item> GAS = registerSimple("gas");
    public static final RegistryObject<Item> PETROL_CAN = registerSimple("petrol_can");
    public static final RegistryObject<Item> DIESEL_CAN = registerSimple("diesel_can");
    public static final RegistryObject<Item> ASPHALT_BINDER = registerSimple("asphalt_binder");
    public static final RegistryObject<Item> PLASTIC = registerSimple("plastic");
    public static final RegistryObject<Item> RACING_ELECTRONICS = registerSimple("racing_electronics");
    public static final RegistryObject<Item> CHASSIS = registerSimple("chassis");
    public static final RegistryObject<Item> ENGINE = registerSimple("engine");
    public static final RegistryObject<Item> TIRES = registerSimple("tires");
    public static final RegistryObject<Item> AERO_KIT = registerSimple("aero_kit");
    public static final RegistryObject<Item> GEARBOX = registerSimple("gearbox");
    public static final RegistryObject<Item> STEERING_CONTROLS = registerSimple("steering_controls");
    public static final RegistryObject<Item> RACE_CONTROL = ITEMS.register("race_control",
        () -> new RaceControlItem(new Item.Properties().setId(ITEMS.key("race_control")))
    );
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
