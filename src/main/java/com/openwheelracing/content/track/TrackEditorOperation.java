package com.openwheelracing.content.track;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;

public record TrackEditorOperation(TrackEditorMode mode, TrackEditorMaterial material, int width, List<BlockPos> points, Direction facing) {
    public static final int MAX_POINTS = 128;
    public static final int MAX_WIDTH = 16;

    public TrackEditorOperation {
        points = List.copyOf(points);
        width = Math.max(1, Math.min(MAX_WIDTH, width));
        facing = facing == null ? Direction.NORTH : facing;
    }
}
