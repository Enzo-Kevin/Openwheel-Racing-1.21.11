package com.openwheelracing.content.track;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;

public record TrackEditorOperation(TrackEditorMode mode, TrackEditorMaterial material, int width, List<BlockPos> points, Direction facing, TrackEditorPreset preset, TrackEditorMaterial runoffMaterial, boolean fullSurface, int clearHeight) {
    public static final int MAX_POINTS = 128;
    public static final int MAX_WIDTH = 16;
    public static final int MAX_CLEAR_HEIGHT = 12;

    public TrackEditorOperation(TrackEditorMode mode, TrackEditorMaterial material, int width, List<BlockPos> points, Direction facing) {
        this(mode, material, width, points, facing, TrackEditorPreset.BLANK, TrackEditorMaterial.GRAVEL, false, 0);
    }

    public TrackEditorOperation(TrackEditorMode mode, TrackEditorMaterial material, int width, List<BlockPos> points, Direction facing, TrackEditorPreset preset, TrackEditorMaterial runoffMaterial) {
        this(mode, material, width, points, facing, preset, runoffMaterial, false, 0);
    }

    public TrackEditorOperation(TrackEditorMode mode, TrackEditorMaterial material, int width, List<BlockPos> points, Direction facing, TrackEditorPreset preset, TrackEditorMaterial runoffMaterial, boolean fullSurface) {
        this(mode, material, width, points, facing, preset, runoffMaterial, fullSurface, 0);
    }

    public TrackEditorOperation {
        points = List.copyOf(points);
        width = Math.max(1, Math.min(MAX_WIDTH, width));
        clearHeight = Math.max(0, Math.min(MAX_CLEAR_HEIGHT, clearHeight));
        facing = facing == null ? Direction.NORTH : facing;
        preset = preset == null ? TrackEditorPreset.BLANK : preset;
        runoffMaterial = runoffMaterial == null || runoffMaterial.isEdge() ? TrackEditorMaterial.GRAVEL : runoffMaterial;
    }
}
