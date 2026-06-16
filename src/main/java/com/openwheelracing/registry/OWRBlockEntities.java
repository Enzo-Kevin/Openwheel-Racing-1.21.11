package com.openwheelracing.registry;

import com.openwheelracing.OpenwheelRacing;
import com.openwheelracing.content.block.entity.CarAssemblyWorkstationBlockEntity;
import com.openwheelracing.content.block.entity.RaceDirectorBlockEntity;
import com.openwheelracing.content.block.entity.RefineryBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public final class OWRBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, OpenwheelRacing.MODID);

    public static final RegistryObject<BlockEntityType<CarAssemblyWorkstationBlockEntity>> CAR_ASSEMBLY_WORKSTATION = BLOCK_ENTITIES.register("car_assembly_workstation",
        () -> new BlockEntityType<>(CarAssemblyWorkstationBlockEntity::new, Set.of(OWRBlocks.CAR_ASSEMBLY_WORKSTATION.get()))
    );

    public static final RegistryObject<BlockEntityType<RefineryBlockEntity>> REFINERY = BLOCK_ENTITIES.register("refinery",
        () -> new BlockEntityType<>(RefineryBlockEntity::new, Set.of(OWRBlocks.REFINERY.get()))
    );

    public static final RegistryObject<BlockEntityType<RaceDirectorBlockEntity>> RACE_DIRECTOR = BLOCK_ENTITIES.register("race_director",
        () -> new BlockEntityType<>(RaceDirectorBlockEntity::new, Set.of(OWRBlocks.RACE_DIRECTOR.get()))
    );

    private OWRBlockEntities() {
    }

    public static void register(BusGroup modBusGroup) {
        BLOCK_ENTITIES.register(modBusGroup);
    }
}
