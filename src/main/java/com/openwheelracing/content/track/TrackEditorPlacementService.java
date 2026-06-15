package com.openwheelracing.content.track;

import com.openwheelracing.registry.OWRBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TrackEditorPlacementService {
    private static final int MAX_BLOCKS_PER_OPERATION = 8192;
    private static final int MAX_DISTANCE_FROM_PLAYER = 512;
    private static final int MAX_POLYGON_AREA = 16384;

    private TrackEditorPlacementService() {
    }

    public static void place(ServerPlayer player, TrackEditorOperation operation) {
        if (!isValidOperation(operation)) {
            return;
        }

        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel level)) {
            return;
        }
        LinkedHashMap<BlockPos, BlockState> placements = generatePlacements(operation);
        if (placements.isEmpty() || placements.size() > MAX_BLOCKS_PER_OPERATION) {
            return;
        }

        List<TrackEditorUndoStore.Entry> undo = new ArrayList<>();
        for (Map.Entry<BlockPos, BlockState> entry : placements.entrySet()) {
            BlockPos pos = entry.getKey();
            if (!level.isInWorldBounds(pos) || !isNearPlayer(player, pos)) {
                return;
            }
            BlockState previous = level.getBlockState(pos);
            if (!canReplace(previous)) {
                return;
            }
            if (!previous.equals(entry.getValue())) {
                undo.add(new TrackEditorUndoStore.Entry(pos.immutable(), previous));
            }
        }

        for (Map.Entry<BlockPos, BlockState> entry : placements.entrySet()) {
            level.setBlock(entry.getKey(), entry.getValue(), 3);
        }
        TrackEditorUndoStore.push(player, undo);
    }

    private static boolean isValidOperation(TrackEditorOperation operation) {
        int points = operation.points().size();
        if (points == 0 || points > TrackEditorOperation.MAX_POINTS) {
            return false;
        }
        return switch (operation.mode()) {
            case STRAIGHT -> points == 2 && !operation.material().isEdge();
            case FREEHAND -> points >= 2 && !operation.material().isEdge();
            case ARC -> points == 3 && !operation.material().isEdge();
            case POLYGON -> points >= 3 && !operation.material().isEdge();
            case EDGE -> points >= 2 && operation.material().isEdge();
        };
    }

    private static LinkedHashMap<BlockPos, BlockState> generatePlacements(TrackEditorOperation operation) {
        LinkedHashMap<BlockPos, BlockState> placements = new LinkedHashMap<>();
        switch (operation.mode()) {
            case STRAIGHT -> addLine(placements, operation.points().get(0), operation.points().get(1), operation, true);
            case FREEHAND -> addPath(placements, operation.points(), operation);
            case EDGE -> addManualEdgePath(placements, operation.points(), operation.width(), operation.material(), operation.facing());
            case ARC -> addArc(placements, operation.points().get(0), operation.points().get(1), operation.points().get(2), operation);
            case POLYGON -> addPolygon(placements, operation.points(), operation.material(), operation.facing());
        }
        return placements;
    }

    private static void addPath(LinkedHashMap<BlockPos, BlockState> placements, List<BlockPos> points, TrackEditorOperation operation) {
        int[] segmentLengths = new int[points.size() - 1];
        int totalSteps = 0;
        for (int i = 1; i < points.size(); i++) {
            int steps = lineSteps(points.get(i - 1), points.get(i));
            segmentLengths[i - 1] = steps;
            totalSteps += steps;
        }
        int walkedSteps = 0;
        for (int i = 1; i < points.size(); i++) {
            addLine(placements, points.get(i - 1), points.get(i), operation, walkedSteps, totalSteps, true);
            walkedSteps += segmentLengths[i - 1];
        }
    }

    private static void addManualEdgePath(LinkedHashMap<BlockPos, BlockState> placements, List<BlockPos> points, int width, TrackEditorMaterial material, Direction fallbackFacing) {
        for (int i = 1; i < points.size(); i++) {
            Direction facing = horizontalFacing(points.get(i - 1), points.get(i), fallbackFacing);
            addManualLine(placements, points.get(i - 1), points.get(i), width, material, facing);
        }
    }

    private static void addLine(LinkedHashMap<BlockPos, BlockState> placements, BlockPos start, BlockPos end, TrackEditorOperation operation, boolean trimPresetEnds) {
        addLine(placements, start, end, operation, 0, lineSteps(start, end), trimPresetEnds);
    }

    private static void addLine(LinkedHashMap<BlockPos, BlockState> placements, BlockPos start, BlockPos end, TrackEditorOperation operation, int walkedSteps, int totalSteps, boolean trimPresetEnds) {
        int dx = end.getX() - start.getX();
        int dz = end.getZ() - start.getZ();
        int steps = lineSteps(start, end);
        Direction pathFacing = horizontalFacing(start, end, operation.facing());
        Direction outward = perpendicularFacing(pathFacing);
        if (steps == 0) {
            addPresetCrossSection(placements, start, outward, operation, !trimPresetEnds || isInsideTrimmedPresetRange(walkedSteps, totalSteps, operation.width()));
            return;
        }
        for (int i = 0; i <= steps; i++) {
            int x = Math.round(start.getX() + dx * (i / (float) steps));
            int z = Math.round(start.getZ() + dz * (i / (float) steps));
            boolean includePreset = !trimPresetEnds || isInsideTrimmedPresetRange(walkedSteps + i, totalSteps, operation.width());
            addPresetCrossSection(placements, new BlockPos(x, start.getY(), z), outward, operation, includePreset);
            if (i > 0) {
                int previousX = Math.round(start.getX() + dx * ((i - 1) / (float) steps));
                int previousZ = Math.round(start.getZ() + dz * ((i - 1) / (float) steps));
                if (previousX != x && previousZ != z) {
                    addPresetCrossSection(placements, new BlockPos(previousX, start.getY(), z), outward, operation, includePreset);
                    addPresetCrossSection(placements, new BlockPos(x, start.getY(), previousZ), outward, operation, includePreset);
                }
            }
        }
    }

    private static int lineSteps(BlockPos start, BlockPos end) {
        int dx = end.getX() - start.getX();
        int dz = end.getZ() - start.getZ();
        return Math.max(Math.abs(dx), Math.abs(dz));
    }

    private static boolean isInsideTrimmedPresetRange(int step, int totalSteps, int width) {
        return step >= width && step <= totalSteps - width;
    }

    private static void addManualLine(LinkedHashMap<BlockPos, BlockState> placements, BlockPos start, BlockPos end, int width, TrackEditorMaterial material, Direction facing) {
        int dx = end.getX() - start.getX();
        int dz = end.getZ() - start.getZ();
        int steps = Math.max(Math.abs(dx), Math.abs(dz));
        if (steps == 0) {
            addThickPoint(placements, start, width, material, facing);
            return;
        }
        for (int i = 0; i <= steps; i++) {
            int x = Math.round(start.getX() + dx * (i / (float) steps));
            int z = Math.round(start.getZ() + dz * (i / (float) steps));
            addThickPoint(placements, new BlockPos(x, start.getY(), z), width, material, facing);
        }
    }

    private static void addArc(LinkedHashMap<BlockPos, BlockState> placements, BlockPos start, BlockPos control, BlockPos end, TrackEditorOperation operation) {
        double length = start.distSqr(control) + control.distSqr(end);
        int samples = Math.min(128, Math.max(12, (int) Math.sqrt(length) * 2));
        BlockPos previous = start;
        for (int i = 1; i <= samples; i++) {
            double t = i / (double) samples;
            double u = 1.0 - t;
            int x = (int) Math.round(u * u * start.getX() + 2.0 * u * t * control.getX() + t * t * end.getX());
            int z = (int) Math.round(u * u * start.getZ() + 2.0 * u * t * control.getZ() + t * t * end.getZ());
            BlockPos next = new BlockPos(x, start.getY(), z);
            addLine(placements, previous, next, operation, true);
            previous = next;
        }
    }

    private static void addPresetCrossSection(LinkedHashMap<BlockPos, BlockState> placements, BlockPos center, Direction outward, TrackEditorOperation operation, boolean includePreset) {
        int radius = Math.max(0, (operation.width() - 1) / 2);
        for (int offset = -radius; offset <= radius; offset++) {
            putSurface(placements, offset(center, outward, offset), operation.material().state(operation.facing()));
        }
        if (!includePreset || operation.preset() == TrackEditorPreset.BLANK) {
            return;
        }
        addPresetSide(placements, center, outward, radius + 1, operation, outward);
        addPresetSide(placements, center, outward.getOpposite(), radius + 1, operation, outward.getOpposite());
    }

    private static void addPresetSide(LinkedHashMap<BlockPos, BlockState> placements, BlockPos center, Direction side, int startOffset, TrackEditorOperation operation, Direction facing) {
        switch (operation.preset()) {
            case STREET -> {
                putSurface(placements, offset(center, side, startOffset), operation.runoffMaterial().state(facing));
                putBarrierStack(placements, offset(center, side, startOffset + 1), facing, 2);
            }
            case HALF_STREET -> {
                putSurface(placements, offset(center, side, startOffset), kerbState(facing));
                putSurface(placements, offset(center, side, startOffset + 1), Blocks.GRASS_BLOCK.defaultBlockState());
                putBarrierStack(placements, offset(center, side, startOffset + 2), facing, 1);
            }
            case FULL_CIRCUIT -> {
                putSurface(placements, offset(center, side, startOffset), operation.runoffMaterial().state(facing));
                putSurface(placements, offset(center, side, startOffset + 1), Blocks.GRASS_BLOCK.defaultBlockState());
                putSurface(placements, offset(center, side, startOffset + 2), Blocks.GRASS_BLOCK.defaultBlockState());
                putBarrierStack(placements, offset(center, side, startOffset + 3), facing, 1);
            }
            case BLANK -> {
            }
        }
    }

    private static void putBarrierStack(LinkedHashMap<BlockPos, BlockState> placements, BlockPos pos, Direction facing, int fenceHeight) {
        BlockPos barrierPos = pos.above();
        placements.put(barrierPos, OWRBlocks.BARRIER.get().defaultBlockState());
        for (int i = 1; i <= fenceHeight; i++) {
            placements.put(barrierPos.above(i), Blocks.IRON_BARS.defaultBlockState());
        }
    }

    private static BlockState kerbState(Direction facing) {
        BlockState state = OWRBlocks.KERB.get().defaultBlockState();
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            return state.setValue(HorizontalDirectionalBlock.FACING, facing);
        }
        return state;
    }

    private static void putSurface(LinkedHashMap<BlockPos, BlockState> placements, BlockPos pos, BlockState state) {
        placements.put(pos, state);
    }

    private static BlockPos offset(BlockPos pos, Direction direction, int distance) {
        return pos.relative(direction, distance);
    }

    private static Direction perpendicularFacing(Direction facing) {
        return switch (facing) {
            case NORTH -> Direction.EAST;
            case SOUTH -> Direction.WEST;
            case EAST -> Direction.SOUTH;
            case WEST -> Direction.NORTH;
            default -> Direction.EAST;
        };
    }

    private static void addPolygon(LinkedHashMap<BlockPos, BlockState> placements, List<BlockPos> points, TrackEditorMaterial material, Direction facing) {
        int minX = points.stream().mapToInt(BlockPos::getX).min().orElse(0);
        int maxX = points.stream().mapToInt(BlockPos::getX).max().orElse(0);
        int minZ = points.stream().mapToInt(BlockPos::getZ).min().orElse(0);
        int maxZ = points.stream().mapToInt(BlockPos::getZ).max().orElse(0);
        if ((maxX - minX + 1) * (maxZ - minZ + 1) > MAX_POLYGON_AREA) {
            return;
        }
        int y = points.get(0).getY();
        BlockState state = material.state(facing);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (isInsidePolygon(x + 0.5, z + 0.5, points)) {
                    placements.put(new BlockPos(x, y, z), state);
                }
            }
        }
    }

    private static void addThickPoint(LinkedHashMap<BlockPos, BlockState> placements, BlockPos center, int width, TrackEditorMaterial material, Direction facing) {
        int radius = Math.max(0, (width - 1) / 2);
        BlockState state = material.state(facing);
        for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
            for (int z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
                placements.put(new BlockPos(x, center.getY(), z), state);
            }
        }
    }

    private static boolean isInsidePolygon(double x, double z, List<BlockPos> points) {
        boolean inside = false;
        for (int i = 0, j = points.size() - 1; i < points.size(); j = i++) {
            BlockPos a = points.get(i);
            BlockPos b = points.get(j);
            if ((a.getZ() > z) != (b.getZ() > z) && x < (b.getX() - a.getX()) * (z - a.getZ()) / (double) (b.getZ() - a.getZ()) + a.getX()) {
                inside = !inside;
            }
        }
        return inside;
    }

    private static Direction horizontalFacing(BlockPos start, BlockPos end, Direction fallback) {
        int dx = end.getX() - start.getX();
        int dz = end.getZ() - start.getZ();
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        }
        if (dz != 0) {
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }
        return fallback.getAxis().isHorizontal() ? fallback : Direction.NORTH;
    }

    private static boolean isNearPlayer(ServerPlayer player, BlockPos pos) {
        return player.blockPosition().distManhattan(pos) <= MAX_DISTANCE_FROM_PLAYER;
    }

    private static boolean canReplace(BlockState state) {
        return state.isAir() || state.canBeReplaced() || isEditorMaterial(state.getBlock());
    }

    private static boolean isEditorMaterial(Block block) {
        return block == OWRBlocks.ASPHALT_TRACK.get()
            || block == OWRBlocks.PIT_LANE.get()
            || block == OWRBlocks.KERB.get()
            || block == OWRBlocks.BARRIER.get()
            || block == Blocks.WHITE_CONCRETE
            || block == Blocks.LIGHT_GRAY_CONCRETE
            || block == Blocks.GRAY_CONCRETE
            || block == Blocks.BLACK_CONCRETE
            || block == Blocks.RED_CONCRETE
            || block == Blocks.SAND
            || block == Blocks.GRASS_BLOCK
            || block == Blocks.DIRT
            || block == Blocks.GRAVEL
            || block == Blocks.IRON_BARS;
    }
}
