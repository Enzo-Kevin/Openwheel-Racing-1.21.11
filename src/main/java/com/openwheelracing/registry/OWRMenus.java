package com.openwheelracing.registry;

import com.openwheelracing.OpenwheelRacing;
import com.openwheelracing.content.menu.CarAssemblyMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class OWRMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, OpenwheelRacing.MODID);

    public static final RegistryObject<MenuType<CarAssemblyMenu>> CAR_ASSEMBLY = MENUS.register("car_assembly",
        () -> IForgeMenuType.create(CarAssemblyMenu::new)
    );

    private OWRMenus() {
    }

    public static void register(BusGroup modBusGroup) {
        MENUS.register(modBusGroup);
    }
}
