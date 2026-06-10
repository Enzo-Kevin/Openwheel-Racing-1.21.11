package com.openwheelracing.registry;

import com.openwheelracing.OpenwheelRacing;
import com.openwheelracing.content.block.CarAssemblyWorkstationBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class OWRBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, OpenwheelRacing.MODID);

    public static final RegistryObject<Block> CAR_ASSEMBLY_WORKSTATION = BLOCKS.register("car_assembly_workstation",
        () -> new CarAssemblyWorkstationBlock(BlockBehaviour.Properties.of()
            .setId(BLOCKS.key("car_assembly_workstation"))
            .mapColor(MapColor.METAL)
            .strength(3.5f, 6.0f)
            .requiresCorrectToolForDrops())
    );

    public static final RegistryObject<Item> CAR_ASSEMBLY_WORKSTATION_ITEM = OWRItems.ITEMS.register("car_assembly_workstation",
        () -> new BlockItem(CAR_ASSEMBLY_WORKSTATION.get(), new Item.Properties().setId(OWRItems.ITEMS.key("car_assembly_workstation")))
    );

    private OWRBlocks() {
    }

    public static void register(BusGroup modBusGroup) {
        BLOCKS.register(modBusGroup);
    }
}
