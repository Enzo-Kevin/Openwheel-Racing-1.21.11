package com.openwheelracing.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;

public class CrudeOilBlock extends Block {
    public CrudeOilBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @org.jspecify.annotations.Nullable Orientation orientation, boolean isMoving) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel && orientation != null && isIgnitedBy(level.getBlockState(pos.relative(orientation.getFront())))) {
            serverLevel.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0f, Level.ExplosionInteraction.BLOCK);
        }
        super.neighborChanged(state, level, pos, block, orientation, isMoving);
    }

    private static boolean isIgnitedBy(BlockState state) {
        return state.is(net.minecraft.world.level.block.Blocks.FIRE)
            || state.is(net.minecraft.world.level.block.Blocks.SOUL_FIRE)
            || state.is(net.minecraft.world.level.block.Blocks.LAVA)
            || state.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK);
    }
}
