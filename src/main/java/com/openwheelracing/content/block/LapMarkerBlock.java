package com.openwheelracing.content.block;

import com.openwheelracing.content.entity.OpenwheelCarEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

public class LapMarkerBlock extends DirectionalTrackBlock {
    private final boolean startFinish;

    public LapMarkerBlock(boolean startFinish, Properties properties) {
        super(properties);
        this.startFinish = startFinish;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide() && entity instanceof OpenwheelCarEntity car) {
            if (startFinish) {
                car.crossStartFinishLine(state.getValue(HorizontalDirectionalBlock.FACING));
            } else {
                car.crossCheckpoint(state.getValue(HorizontalDirectionalBlock.FACING));
            }
        }
        super.stepOn(level, pos, state, entity);
    }
}
