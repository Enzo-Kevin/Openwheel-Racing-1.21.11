package com.openwheelracing.client.screen;

import com.openwheelracing.registry.OWRBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
        Block block = level.getBlockState(new BlockPos(x, y, z)).getBlock();
        int color = colorFor(block);
        return new TerrainSample(y, color == 0 ? 0xFF2B352B : color);
    }

    private int colorFor(Block block) {
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
        if (block == Blocks.DIRT) {
            return 0xFF74543A;
        }
        if (block == Blocks.SAND) {
            return 0xFFC8B77B;
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
        return 0;
    }

    private long pack(int x, int z) {
        return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
    }

    record TerrainSample(int surfaceY, int colorArgb) {
    }
}
