package com.openwheelracing.registry;

import com.openwheelracing.OpenwheelRacing;
import com.openwheelracing.content.block.CarAssemblyWorkstationBlock;
import com.openwheelracing.content.block.CrudeOilBlock;
import com.openwheelracing.content.block.DirectionalTrackBlock;
import com.openwheelracing.content.block.LapMarkerBlock;
import com.openwheelracing.content.block.RaceDirectorBlock;
import com.openwheelracing.content.block.RefineryBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
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

    public static final RegistryObject<Block> REFINERY = BLOCKS.register("refinery",
        () -> new RefineryBlock(BlockBehaviour.Properties.of()
            .setId(BLOCKS.key("refinery"))
            .mapColor(MapColor.METAL)
            .strength(3.5f, 6.0f)
            .requiresCorrectToolForDrops())
    );

    public static final RegistryObject<Block> RACE_DIRECTOR = BLOCKS.register("race_director",
        () -> new RaceDirectorBlock(BlockBehaviour.Properties.of()
            .setId(BLOCKS.key("race_director"))
            .mapColor(MapColor.METAL)
            .strength(3.5f, 6.0f)
            .requiresCorrectToolForDrops())
    );

    public static final RegistryObject<Block> CRUDE_OIL_DEPOSIT = BLOCKS.register("crude_oil_deposit",
        () -> new CrudeOilBlock(BlockBehaviour.Properties.of()
            .setId(BLOCKS.key("crude_oil_deposit"))
            .mapColor(MapColor.COLOR_BLACK)
            .strength(1.8f, 2.0f)
            .requiresCorrectToolForDrops())
    );

    public static final RegistryObject<Block> ASPHALT_TRACK = registerSimpleBlock("asphalt_track", MapColor.COLOR_BLACK, 2.4f, 6.0f);
    public static final RegistryObject<Block> ASPHALT_TRACK_SLAB = registerSlabBlock("asphalt_track_slab", MapColor.COLOR_BLACK, 2.4f, 6.0f);
    public static final RegistryObject<Block> KERB = registerDirectionalBlock("kerb", MapColor.COLOR_RED, 2.0f, 6.0f);
    public static final RegistryObject<Block> BARRIER = registerSimpleBlock("barrier", MapColor.METAL, 4.0f, 8.0f);
    public static final RegistryObject<Block> PIT_LANE = registerSimpleBlock("pit_lane", MapColor.COLOR_GRAY, 2.4f, 6.0f);
    public static final RegistryObject<Block> PIT_LANE_SLAB = registerSlabBlock("pit_lane_slab", MapColor.COLOR_GRAY, 2.4f, 6.0f);
    public static final RegistryObject<Block> START_FINISH = registerLapMarker("start_finish", true, MapColor.SNOW);
    public static final RegistryObject<Block> CHECKPOINT = registerLapMarker("checkpoint", false, MapColor.COLOR_LIGHT_BLUE);

    public static final RegistryObject<Item> CAR_ASSEMBLY_WORKSTATION_ITEM = registerBlockItem("car_assembly_workstation", CAR_ASSEMBLY_WORKSTATION);
    public static final RegistryObject<Item> REFINERY_ITEM = registerBlockItem("refinery", REFINERY);
    public static final RegistryObject<Item> RACE_DIRECTOR_ITEM = registerBlockItem("race_director", RACE_DIRECTOR);
    public static final RegistryObject<Item> CRUDE_OIL_DEPOSIT_ITEM = registerBlockItem("crude_oil_deposit", CRUDE_OIL_DEPOSIT);
    public static final RegistryObject<Item> ASPHALT_TRACK_ITEM = registerBlockItem("asphalt_track", ASPHALT_TRACK);
    public static final RegistryObject<Item> ASPHALT_TRACK_SLAB_ITEM = registerBlockItem("asphalt_track_slab", ASPHALT_TRACK_SLAB);
    public static final RegistryObject<Item> KERB_ITEM = registerBlockItem("kerb", KERB);
    public static final RegistryObject<Item> BARRIER_ITEM = registerBlockItem("barrier", BARRIER);
    public static final RegistryObject<Item> PIT_LANE_ITEM = registerBlockItem("pit_lane", PIT_LANE);
    public static final RegistryObject<Item> PIT_LANE_SLAB_ITEM = registerBlockItem("pit_lane_slab", PIT_LANE_SLAB);
    public static final RegistryObject<Item> START_FINISH_ITEM = registerBlockItem("start_finish", START_FINISH);
    public static final RegistryObject<Item> CHECKPOINT_ITEM = registerBlockItem("checkpoint", CHECKPOINT);

    private OWRBlocks() {
    }

    public static void register(BusGroup modBusGroup) {
        BLOCKS.register(modBusGroup);
    }

    private static RegistryObject<Block> registerSimpleBlock(String name, MapColor mapColor, float destroyTime, float explosionResistance) {
        return BLOCKS.register(name, () -> new Block(BlockBehaviour.Properties.of()
            .setId(BLOCKS.key(name))
            .mapColor(mapColor)
            .strength(destroyTime, explosionResistance)
            .requiresCorrectToolForDrops())
        );
    }

    private static RegistryObject<Block> registerSlabBlock(String name, MapColor mapColor, float destroyTime, float explosionResistance) {
        return BLOCKS.register(name, () -> new SlabBlock(BlockBehaviour.Properties.of()
            .setId(BLOCKS.key(name))
            .mapColor(mapColor)
            .strength(destroyTime, explosionResistance)
            .requiresCorrectToolForDrops())
        );
    }

    private static RegistryObject<Block> registerDirectionalBlock(String name, MapColor mapColor, float destroyTime, float explosionResistance) {
        return BLOCKS.register(name, () -> new DirectionalTrackBlock(BlockBehaviour.Properties.of()
            .setId(BLOCKS.key(name))
            .mapColor(mapColor)
            .strength(destroyTime, explosionResistance)
            .requiresCorrectToolForDrops())
        );
    }

    private static RegistryObject<Block> registerLapMarker(String name, boolean startFinish, MapColor mapColor) {
        return BLOCKS.register(name, () -> new LapMarkerBlock(startFinish, BlockBehaviour.Properties.of()
            .setId(BLOCKS.key(name))
            .mapColor(mapColor)
            .strength(2.0f, 6.0f)
            .requiresCorrectToolForDrops())
        );
    }

    private static RegistryObject<Item> registerBlockItem(String name, RegistryObject<Block> block) {
        return OWRItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().setId(OWRItems.ITEMS.key(name))));
    }
}
