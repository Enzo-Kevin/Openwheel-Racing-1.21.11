package com.openwheelracing.registry;

import com.openwheelracing.OpenwheelRacing;
import com.openwheelracing.content.entity.OpenwheelCarEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class OWREntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, OpenwheelRacing.MODID);

    public static final RegistryObject<EntityType<OpenwheelCarEntity>> PROTOTYPE_CAR = ENTITY_TYPES.register("prototype_car",
        () -> EntityType.Builder.of(OpenwheelCarEntity::new, MobCategory.MISC)
            .sized(1.9f, 1.05f)
            .clientTrackingRange(10)
            .updateInterval(2)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(OpenwheelRacing.MODID, "prototype_car")))
    );

    private OWREntities() {
    }

    public static void register(BusGroup modBusGroup) {
        ENTITY_TYPES.register(modBusGroup);
    }
}
