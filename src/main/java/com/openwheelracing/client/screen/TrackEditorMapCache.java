package com.openwheelracing.client.screen;

import com.openwheelracing.registry.OWRBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.HashMap;
import java.util.Map;

final class TrackEditorMapCache {
    private static final int SAMPLE_SIZE = 2;
    private final Map<Long, TerrainSample> samples = new HashMap<>();

    TerrainSample sample(Level level, int x, int z, int fallbackY) {
        int sampleX = Math.floorDiv(x, SAMPLE_SIZE) * SAMPLE_SIZE;
        int sampleZ = Math.floorDiv(z, SAMPLE_SIZE) * SAMPLE_SIZE;
        long key = pack(sampleX, sampleZ);
        TerrainSample cached = samples.get(key);
        if (cached != null) {
            return cached;
        }
        TerrainSample sampled = sampleSurface(level, sampleX, sampleZ, fallbackY);
        samples.put(key, sampled);
        return sampled;
    }

    int surfaceY(Level level, int x, int z, int fallbackY) {
        return sample(level, x, z, fallbackY).surfaceY();
    }

    int color(Level level, int x, int z, int fallbackY) {
        return sample(level, x, z, fallbackY).colorArgb();
    }

    private TerrainSample sampleSurface(Level level, int x, int z, int fallbackY) {
        BlockPos chunkCheck = new BlockPos(x, fallbackY, z);
        if (!level.hasChunkAt(chunkCheck)) {
            return new TerrainSample(fallbackY, 0xFF181A1C);
        }
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
        if (y < level.getMinY()) {
            return new TerrainSample(fallbackY, 0xFF101214);
        }
        BlockState state = level.getBlockState(new BlockPos(x, y, z));
        int color = colorFor(state);
        return new TerrainSample(y, color == 0 ? 0xFF2B352B : color);
    }

    private int colorFor(BlockState state) {
        Block block = state.getBlock();
        if (block == OWRBlocks.ASPHALT_TRACK.get()) {
            return 0xFF202020;
        }
        if (block == OWRBlocks.PIT_LANE.get()) {
            return 0xFF4A4A4A;
        }
        if (block == OWRBlocks.KERB.get()) {
            return 0xFFCC3333;
        }
        if (block == OWRBlocks.BARRIER.get()) {
            return 0xFF888888;
        }
        if (block == Blocks.GRASS_BLOCK) {
            return 0xFF4B7D3A;
        }
        if (block == Blocks.OAK_LEAVES || block == Blocks.BIRCH_LEAVES || block == Blocks.SPRUCE_LEAVES || block == Blocks.JUNGLE_LEAVES || block == Blocks.ACACIA_LEAVES || block == Blocks.DARK_OAK_LEAVES || block == Blocks.MANGROVE_LEAVES || block == Blocks.CHERRY_LEAVES) {
            return 0xFF2F6B31;
        }
        if (block == Blocks.OAK_LOG || block == Blocks.BIRCH_LOG || block == Blocks.SPRUCE_LOG || block == Blocks.JUNGLE_LOG || block == Blocks.ACACIA_LOG || block == Blocks.DARK_OAK_LOG || block == Blocks.MANGROVE_LOG || block == Blocks.CHERRY_LOG) {
            return 0xFF6F5136;
        }
        if (block == Blocks.DIRT || block == Blocks.COARSE_DIRT || block == Blocks.ROOTED_DIRT || block == Blocks.PODZOL) {
            return 0xFF74543A;
        }
        if (block == Blocks.STONE || block == Blocks.COBBLESTONE || block == Blocks.ANDESITE || block == Blocks.DIORITE || block == Blocks.GRANITE) {
            return 0xFF777777;
        }
        if (block == Blocks.SMOOTH_STONE || block == Blocks.STONE_BRICKS || block == Blocks.BRICKS || block == Blocks.POLISHED_ANDESITE || block == Blocks.POLISHED_DIORITE || block == Blocks.POLISHED_GRANITE) {
            return 0xFF8A8A8A;
        }
        if (block == Blocks.SAND || block == Blocks.SANDSTONE || block == Blocks.SMOOTH_SANDSTONE) {
            return 0xFFC8B77B;
        }
        if (block == Blocks.RED_SAND || block == Blocks.RED_SANDSTONE || block == Blocks.SMOOTH_RED_SANDSTONE) {
            return 0xFFC47745;
        }
        if (block == Blocks.WATER) {
            return 0xFF315EAF;
        }
        if (block == Blocks.ICE || block == Blocks.PACKED_ICE || block == Blocks.BLUE_ICE) {
            return 0xFF8FC6DD;
        }
        if (block == Blocks.SNOW || block == Blocks.SNOW_BLOCK) {
            return 0xFFE8F0F0;
        }
        if (block == Blocks.GLASS || block == Blocks.GLASS_PANE) {
            return 0xFF9FB8C8;
        }
        if (block == Blocks.OAK_PLANKS || block == Blocks.BIRCH_PLANKS || block == Blocks.SPRUCE_PLANKS || block == Blocks.JUNGLE_PLANKS || block == Blocks.ACACIA_PLANKS || block == Blocks.DARK_OAK_PLANKS || block == Blocks.MANGROVE_PLANKS || block == Blocks.CHERRY_PLANKS) {
            return 0xFFA8794B;
        }
        if (block == Blocks.GRAVEL) {
            return 0xFF737373;
        }
        if (block == Blocks.WHITE_CONCRETE || block == Blocks.LIGHT_GRAY_CONCRETE) {
            return 0xFFBEBEBE;
        }
        if (block == Blocks.GRAY_CONCRETE || block == Blocks.BLACK_CONCRETE) {
            return 0xFF505050;
        }
        if (block == Blocks.RED_CONCRETE) {
            return 0xFF8F2B25;
        }
        return 0xFF667066;
    }

    private long pack(int x, int z) {
        return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
    }

    record TerrainSample(int surfaceY, int colorArgb) {
    }
}
