package com.openwheelracing.client.screen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.openwheelracing.content.track.TrackEditorMaterial;
import com.openwheelracing.content.track.TrackEditorMode;
import com.openwheelracing.content.track.TrackEditorOperation;
import com.openwheelracing.content.track.TrackEditorPreset;
import com.openwheelracing.network.OWRNetwork;
import com.openwheelracing.registry.OWRBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TrackEditorScreen extends Screen {
    private static final TrackEditorMode[] MODES = TrackEditorMode.values();
    private static final TrackEditorMaterial[] PAVEMENT_MATERIALS = {
        TrackEditorMaterial.ASPHALT,
        TrackEditorMaterial.PIT_LANE,
        TrackEditorMaterial.WHITE_CONCRETE,
        TrackEditorMaterial.LIGHT_GRAY_CONCRETE,
        TrackEditorMaterial.GRAY_CONCRETE,
        TrackEditorMaterial.BLACK_CONCRETE,
        TrackEditorMaterial.RED_CONCRETE,
        TrackEditorMaterial.SAND,
        TrackEditorMaterial.GRASS,
        TrackEditorMaterial.DIRT,
        TrackEditorMaterial.GRAVEL
    };
    private static final TrackEditorMaterial[] EDGE_MATERIALS = {
        TrackEditorMaterial.KERB,
        TrackEditorMaterial.BARRIER
    };
    private static final TrackEditorMaterial[] RUNOFF_MATERIALS = {
        TrackEditorMaterial.GRAVEL,
        TrackEditorMaterial.SAND,
        TrackEditorMaterial.DIRT,
        TrackEditorMaterial.GRASS,
        TrackEditorMaterial.LIGHT_GRAY_CONCRETE,
        TrackEditorMaterial.GRAY_CONCRETE,
        TrackEditorMaterial.RED_CONCRETE
    };
    private static final TrackEditorPreset[] PRESETS = TrackEditorPreset.values();
    private static final double[] ZOOM_LEVELS = {0.25, 0.5, 1.0, 2.0, 4.0};
    private static final int OUTER_MARGIN = 8;
    private static final int QUEUE_CHUNK_SIZE = 64;
    private static final int QUEUE_SEND_DISTANCE = 480;
    private static final int QUEUE_SENDS_PER_TICK = 2;
    private static final double IMPORT_SAMPLE_SPACING = 1.0;
    private static final String IMPORT_PATH = "openwheelracing/imports/lap-simulator-track.json";
    private static final List<PendingEditorOperation> PENDING_QUEUE = new ArrayList<>();

    private final List<BlockPos> points = new ArrayList<>();
    private int modeIndex;
    private int pavementIndex;
    private int edgeIndex;
    private int runoffMaterialIndex;
    private int presetIndex;
    private int zoomIndex = 2;
    private int width = 3;
    private int editY;
    private double centerX;
    private double centerZ;
    private boolean initialized;
    private boolean dragging;
    private double lastDragX;
    private double lastDragY;
    private final TrackEditorMapCache mapCache = new TrackEditorMapCache();
    private OverlayNotice notice;
    private Button clearQueueButton;
    private Button elevationModeButton;
    private Button surfaceApplicationButton;
    private ClearHeightSlider clearHeightSlider;
    private int lastQueuedCount;
    private int clearHeight = 3;
    private ElevationMode elevationMode = ElevationMode.SURFACE;
    private SurfaceApplication surfaceApplication = SurfaceApplication.KEYPOINT;
    private boolean showHelp;

    public TrackEditorScreen() {
        super(Component.translatable("screen.openwheelracing.track_editor"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        if (!initialized && minecraft != null && minecraft.player != null) {
            centerX = minecraft.player.getX();
            centerZ = minecraft.player.getZ();
            editY = minecraft.player.blockPosition().getY();
            initialized = true;
        }
        MapBounds map = mapBounds();
        clearQueueButton = addRenderableWidget(Button.builder(Component.translatable("screen.openwheelracing.track_editor.clear_queue"), button -> clearPendingQueue())
            .bounds(map.right - 112, map.top + 8, 104, 20)
            .build());
        elevationModeButton = addRenderableWidget(Button.builder(elevationModeLabel(), button -> toggleElevationMode())
            .bounds(map.right - 360, map.top + 8, 116, 20)
            .build());
        surfaceApplicationButton = addRenderableWidget(Button.builder(surfaceApplicationLabel(), button -> toggleSurfaceApplication())
            .bounds(map.right - 240, map.top + 8, 124, 20)
            .build());
        clearHeightSlider = addRenderableWidget(new ClearHeightSlider(map.right - 360, map.top + 32, 352, 20));
        updateClearQueueButton();
        updateToggleButtons();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && isInsideMap(event.x(), event.y())) {
            addPoint(screenToBlock(event.x(), event.y()));
            return true;
        }
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            removeLastPoint();
            return true;
        }
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && isInsideMap(event.x(), event.y())) {
            dragging = true;
            lastDragX = event.x();
            lastDragY = event.y();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            dragging = false;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging) {
            centerX -= (event.x() - lastDragX) * blocksPerPixel();
            centerZ -= (event.y() - lastDragY) * blocksPerPixel();
            lastDragX = event.x();
            lastDragY = event.y();
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isInsideMap(mouseX, mouseY)) {
            zoomIndex = Math.max(0, Math.min(ZOOM_LEVELS.length - 1, zoomIndex + (scrollY > 0 ? -1 : 1)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if ((event.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0 && keyCode == GLFW.GLFW_KEY_Z) {
            OWRNetwork.CHANNEL.send(new OWRNetwork.TrackEditorUndoMessage(), PacketDistributor.SERVER.noArg());
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_C) {
            clearPendingQueue();
            return true;
        }
        switch (keyCode) {
            case GLFW.GLFW_KEY_M -> {
                modeIndex = (modeIndex + 1) % MODES.length;
                points.clear();
                return true;
            }
            case GLFW.GLFW_KEY_N -> {
                cycleMaterial();
                return true;
            }
            case GLFW.GLFW_KEY_P -> {
                presetIndex = (presetIndex + 1) % PRESETS.length;
                return true;
            }
            case GLFW.GLFW_KEY_O -> {
                runoffMaterialIndex = (runoffMaterialIndex + 1) % RUNOFF_MATERIALS.length;
                return true;
            }
            case GLFW.GLFW_KEY_I -> {
                importLapSimulatorTrack();
                return true;
            }
            case GLFW.GLFW_KEY_EQUAL, GLFW.GLFW_KEY_KP_ADD -> {
                width = Math.min(TrackEditorOperation.MAX_WIDTH, width + 1);
                return true;
            }
            case GLFW.GLFW_KEY_MINUS, GLFW.GLFW_KEY_KP_SUBTRACT -> {
                width = Math.max(1, width - 1);
                return true;
            }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                if (mode() == TrackEditorMode.POLYGON) {
                    commitIfReady();
                    return true;
                }
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                removeLastPoint();
                return true;
            }
            case GLFW.GLFW_KEY_R -> {
                recenterOnPlayer();
                return true;
            }
            case GLFW.GLFW_KEY_H -> {
                showHelp = !showHelp;
                return true;
            }
            case GLFW.GLFW_KEY_PAGE_UP, GLFW.GLFW_KEY_RIGHT_BRACKET -> {
                editY++;
                return true;
            }
            case GLFW.GLFW_KEY_PAGE_DOWN, GLFW.GLFW_KEY_LEFT_BRACKET -> {
                editY--;
                return true;
            }
            case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_A -> {
                centerX -= panStep();
                return true;
            }
            case GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_D -> {
                centerX += panStep();
                return true;
            }
            case GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_W -> {
                centerZ -= panStep();
                return true;
            }
            case GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_S -> {
                centerZ += panStep();
                return true;
            }
            default -> {
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public void tick() {
        flushPendingQueue();
        if (notice != null && notice.ticksRemaining() > 0) {
            notice = new OverlayNotice(notice.text(), notice.color(), notice.ticksRemaining() - 1);
            if (notice.ticksRemaining() <= 0) {
                notice = null;
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderMap(graphics);
        renderQueuedOperations(graphics);
        renderPendingGeometry(graphics);
        renderPlayerMarker(graphics);
        renderOverlayHud(graphics);
        renderNotice(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderMap(GuiGraphics graphics) {
        MapBounds map = mapBounds();
        graphics.fill(map.left, map.top, map.right, map.bottom, 0xEE101214);
        renderTerrainSamples(graphics, map);
        renderGrid(graphics, map);
        renderScaleBar(graphics, map);
        graphics.renderOutline(map.left, map.top, map.width(), map.height(), 0xFFDA1A20);
    }

    private void renderTerrainSamples(GuiGraphics graphics, MapBounds map) {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null) {
            return;
        }
        double bpp = blocksPerPixel();
        int sampleBlocks = Math.max(1, (int) Math.ceil(bpp));
        int pixelStep = Math.max(2, (int) Math.round(sampleBlocks / bpp));
        for (int sx = map.left; sx < map.right; sx += pixelStep) {
            for (int sy = map.top; sy < map.bottom; sy += pixelStep) {
                BlockPos pos = screenToBlock(sx + pixelStep * 0.5, sy + pixelStep * 0.5);
                int color = mapCache.color(level, pos.getX(), pos.getZ(), editY);
                graphics.fill(sx, sy, Math.min(map.right, sx + pixelStep), Math.min(map.bottom, sy + pixelStep), color);
            }
        }
    }

    private void renderGrid(GuiGraphics graphics, MapBounds map) {
        double bpp = blocksPerPixel();
        int minorStep = gridStep();
        int startX = floorToStep(screenToBlock(map.left, map.top).getX(), minorStep) - minorStep;
        int endX = screenToBlock(map.right, map.top).getX() + minorStep;
        int startZ = floorToStep(screenToBlock(map.left, map.top).getZ(), minorStep) - minorStep;
        int endZ = screenToBlock(map.left, map.bottom).getZ() + minorStep;
        for (int x = startX; x <= endX; x += minorStep) {
            int sx = worldToScreenX(x);
            int color = x % 16 == 0 ? 0x88666666 : 0x44333333;
            graphics.fill(sx, map.top, sx + 1, map.bottom, color);
        }
        for (int z = startZ; z <= endZ; z += minorStep) {
            int sy = worldToScreenY(z);
            int color = z % 16 == 0 ? 0x88666666 : 0x44333333;
            graphics.fill(map.left, sy, map.right, sy + 1, color);
        }
    }

    private void renderScaleBar(GuiGraphics graphics, MapBounds map) {
        int fullLength = (int) Math.round(100.0 / blocksPerPixel());
        int maxLength = Math.max(48, map.width() - 40);
        int length = Math.min(fullLength, maxLength);
        int x = map.left + 16;
        int y = map.bottom - 24;
        graphics.fill(x - 6, y - 12, x + length + 58, y + 12, 0xAA000000);
        graphics.fill(x, y, x + length, y + 3, 0xFFFFFFFF);
        graphics.fill(x, y - 4, x + 2, y + 7, 0xFFFFFFFF);
        graphics.fill(x + length - 2, y - 4, x + length, y + 7, 0xFFFFFFFF);
        graphics.drawString(font, Component.translatable("screen.openwheelracing.track_editor.scale_100m"), x + length + 6, y - 4, 0xFFFFFFFF, false);
    }

    private void renderQueuedOperations(GuiGraphics graphics) {
        for (PendingEditorOperation queued : PENDING_QUEUE) {
            List<BlockPos> queuedPoints = queued.operation().points();
            for (int i = 1; i < queuedPoints.size(); i++) {
                drawLine(graphics, queuedPoints.get(i - 1), queuedPoints.get(i), 0xFF55FFAA);
            }
        }
    }

    private void renderPendingGeometry(GuiGraphics graphics) {
        for (int i = 0; i < points.size(); i++) {
            BlockPos point = points.get(i);
            int x = worldToScreenX(point.getX());
            int y = worldToScreenY(point.getZ());
            graphics.fill(x - 3, y - 3, x + 4, y + 4, 0xFFFFDD55);
            if (i > 0) {
                drawLine(graphics, points.get(i - 1), point, 0xFFFFDD55);
            }
        }
        if (mode() == TrackEditorMode.POLYGON && points.size() > 2) {
            drawLine(graphics, points.get(points.size() - 1), points.get(0), 0x99FFDD55);
        }
    }

    private void renderPlayerMarker(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        int x = worldToScreenX(minecraft.player.getX());
        int y = worldToScreenY(minecraft.player.getZ());
        graphics.fill(x - 3, y - 3, x + 4, y + 4, 0xFF55AAFF);
        graphics.drawString(font, Component.translatable("screen.openwheelracing.track_editor.player"), x + 6, y - 4, 0xFF55AAFF, false);
    }

    private void renderOverlayHud(GuiGraphics graphics) {
        MapBounds map = mapBounds();
        int x = map.left + 8;
        int y = map.top + 8;
        int panelWidth = showHelp ? 360 : 292;
        int panelHeight = showHelp ? 160 : 76;
        graphics.fill(x, y, x + panelWidth, y + panelHeight, 0xAA000000);
        graphics.renderOutline(x, y, panelWidth, panelHeight, 0xFFDA1A20);
        graphics.drawString(font, title, x + 8, y + 8, 0xFFFFFFFF, false);
        graphics.drawString(font, Component.translatable("screen.openwheelracing.track_editor.hud_line1", mode().name(), material().name(), width, blocksPerPixel()), x + 8, y + 22, 0xFFE6E6E6, false);
        graphics.drawString(font, Component.translatable("screen.openwheelracing.track_editor.hud_line2", preset().displayName(), runoffMaterial().name(), editY, elevationModeText(), surfaceApplicationText()), x + 8, y + 34, 0xFFE6E6E6, false);
        graphics.drawString(font, Component.translatable("screen.openwheelracing.track_editor.hud_line3", points.size(), requiredPointsText(), PENDING_QUEUE.size(), clearHeight), x + 8, y + 46, 0xFFE6E6E6, false);
        graphics.drawString(font, Component.translatable("screen.openwheelracing.track_editor.hud_hint"), x + 8, y + 60, 0xFFB7FFB7, false);
        if (showHelp) {
            graphics.drawString(font, Component.translatable("screen.openwheelracing.track_editor.map_help1"), x + 8, y + 82, 0xFFB7FFB7, false);
            graphics.drawString(font, Component.translatable("screen.openwheelracing.track_editor.map_help2"), x + 8, y + 94, 0xFFB7FFB7, false);
            graphics.drawString(font, Component.translatable("screen.openwheelracing.track_editor.map_help3"), x + 8, y + 106, 0xFFB7FFB7, false);
            graphics.drawString(font, Component.translatable("screen.openwheelracing.track_editor.map_help4"), x + 8, y + 118, 0xFFB7FFB7, false);
            graphics.drawString(font, Component.translatable("screen.openwheelracing.track_editor.map_help5"), x + 8, y + 130, 0xFFB7FFB7, false);
            graphics.drawString(font, Component.translatable("screen.openwheelracing.track_editor.map_help6"), x + 8, y + 142, 0xFFB7FFB7, false);
        }
    }

    private void renderNotice(GuiGraphics graphics) {
        if (notice == null) {
            return;
        }
        int textWidth = font.width(notice.text());
        int x = (this.width - textWidth) / 2;
        int y = mapBounds().top + 12;
        graphics.fill(x - 8, y - 6, x + textWidth + 8, y + 14, 0xBB000000);
        graphics.drawString(font, notice.text(), x, y, notice.color(), false);
    }

    private void importLapSimulatorTrack() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        Path path = minecraft.gameDirectory.toPath().resolve(IMPORT_PATH);
        try {
            List<LapSimulatorSection> sections = parseLapSimulatorSections(Files.readString(path, StandardCharsets.UTF_8));
            List<BlockPos> importedPoints = mapImportedPoints(sampleLapSimulatorCenterline(sections), sections.get(0));
            if (importedPoints.size() < 2) {
                showNotice(Component.translatable("screen.openwheelracing.track_editor.import_failed", "not enough unique points"), 0xFFFF7777);
                return;
            }
            int chunks = queuePath(importedPoints, QueueReason.IMPORT);
            updatePendingQueueStatus(Component.translatable("screen.openwheelracing.track_editor.import_queued", sections.size(), importedPoints.size(), chunks));
        } catch (IOException e) {
            showNotice(Component.translatable("screen.openwheelracing.track_editor.import_failed", IMPORT_PATH), 0xFFFF7777);
        } catch (IllegalArgumentException | JsonSyntaxException e) {
            showNotice(Component.translatable("screen.openwheelracing.track_editor.import_failed", e.getMessage()), 0xFFFF7777);
        }
    }

    private List<LapSimulatorSection> parseLapSimulatorSections(String json) {
        JsonElement root = JsonParser.parseString(json);
        JsonArray rawSections;
        if (root.isJsonArray()) {
            rawSections = root.getAsJsonArray();
        } else if (root.isJsonObject() && root.getAsJsonObject().get("sections") instanceof JsonArray sections) {
            rawSections = sections;
        } else {
            throw new IllegalArgumentException("missing sections array");
        }
        if (rawSections.size() < 2) {
            throw new IllegalArgumentException("track needs at least two sections");
        }
        List<LapSimulatorSection> sections = new ArrayList<>(rawSections.size());
        for (int i = 0; i < rawSections.size(); i++) {
            JsonElement element = rawSections.get(i);
            if (!element.isJsonObject()) {
                throw new IllegalArgumentException("section " + i + " is not an object");
            }
            JsonObject section = element.getAsJsonObject();
            sections.add(new LapSimulatorSection(
                requiredString(section, "id", i),
                requiredFiniteDouble(section, "x", i),
                requiredFiniteDouble(section, "y", i),
                requiredFiniteDouble(section, "direction", i),
                requiredPositiveDouble(section, "width", i)
            ));
        }
        return sections;
    }

    private String requiredString(JsonObject object, String key, int sectionIndex) {
        JsonElement value = object.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString() || value.getAsString().isBlank()) {
            throw new IllegalArgumentException("section " + sectionIndex + " missing " + key);
        }
        return value.getAsString();
    }

    private double requiredPositiveDouble(JsonObject object, String key, int sectionIndex) {
        double value = requiredFiniteDouble(object, key, sectionIndex);
        if (value <= 0.0) {
            throw new IllegalArgumentException("section " + sectionIndex + " " + key + " must be > 0");
        }
        return value;
    }

    private double requiredFiniteDouble(JsonObject object, String key, int sectionIndex) {
        JsonElement value = object.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException("section " + sectionIndex + " missing " + key);
        }
        double number = value.getAsDouble();
        if (!Double.isFinite(number)) {
            throw new IllegalArgumentException("section " + sectionIndex + " invalid " + key);
        }
        return number;
    }

    private List<Vec2> sampleLapSimulatorCenterline(List<LapSimulatorSection> sections) {
        List<Vec2> points = new ArrayList<>();
        for (int i = 0; i < sections.size(); i++) {
            LapSimulatorSection start = sections.get(i);
            LapSimulatorSection end = sections.get((i + 1) % sections.size());
            sampleBezierSegment(points, start, end);
        }
        return points;
    }

    private void sampleBezierSegment(List<Vec2> points, LapSimulatorSection start, LapSimulatorSection end) {
        Vec2 p0 = new Vec2(start.x(), start.y());
        Vec2 p3 = new Vec2(end.x(), end.y());
        double chord = p0.distanceTo(p3);
        if (chord < 0.01) {
            return;
        }
        double alpha = chord / 3.0;
        Vec2 c0 = p0.add(Vec2.fromDirection(start.direction()).scale(alpha));
        Vec2 c1 = p3.subtract(Vec2.fromDirection(end.direction()).scale(alpha));
        int subdivisions = Math.max(12, Math.min(256, (int) Math.ceil(chord / 2.0)));
        Vec2 previous = p0;
        double carried = points.isEmpty() ? 0.0 : IMPORT_SAMPLE_SPACING;
        addSampledPoint(points, p0);
        for (int i = 1; i <= subdivisions; i++) {
            Vec2 current = cubic(p0, c0, c1, p3, i / (double) subdivisions);
            double segmentLength = previous.distanceTo(current);
            while (carried + segmentLength >= IMPORT_SAMPLE_SPACING) {
                double t = (IMPORT_SAMPLE_SPACING - carried) / segmentLength;
                Vec2 sampled = previous.lerp(current, t);
                addSampledPoint(points, sampled);
                previous = sampled;
                segmentLength = previous.distanceTo(current);
                carried = 0.0;
            }
            carried += segmentLength;
            previous = current;
        }
    }

    private Vec2 cubic(Vec2 p0, Vec2 c0, Vec2 c1, Vec2 p3, double t) {
        double u = 1.0 - t;
        return new Vec2(
            u * u * u * p0.x() + 3.0 * u * u * t * c0.x() + 3.0 * u * t * t * c1.x() + t * t * t * p3.x(),
            u * u * u * p0.y() + 3.0 * u * u * t * c0.y() + 3.0 * u * t * t * c1.y() + t * t * t * p3.y()
        );
    }

    private void addSampledPoint(List<Vec2> points, Vec2 point) {
        if (points.isEmpty() || points.get(points.size() - 1).distanceTo(point) >= 0.5) {
            points.add(point);
        }
    }

    private List<BlockPos> mapImportedPoints(List<Vec2> importedPoints, LapSimulatorSection origin) {
        List<BlockPos> mapped = new ArrayList<>(importedPoints.size());
        BlockPos previous = null;
        int anchorX = (int) Math.round(centerX);
        int anchorZ = (int) Math.round(centerZ);
        for (Vec2 point : importedPoints) {
            BlockPos pos = withEditorY(
                (int) Math.round(anchorX + point.x() - origin.x()),
                (int) Math.round(anchorZ + point.y() - origin.y())
            );
            if (!pos.equals(previous)) {
                mapped.add(pos);
                previous = pos;
            }
        }
        if (mapped.size() > 1 && mapped.get(0).equals(mapped.get(mapped.size() - 1))) {
            mapped.remove(mapped.size() - 1);
        }
        return mapped;
    }

    private int queuePath(List<BlockPos> pathPoints, QueueReason reason) {
        int chunks = 0;
        for (int start = 0; start < pathPoints.size() - 1; start += QUEUE_CHUNK_SIZE - 1) {
            int end = Math.min(pathPoints.size(), start + QUEUE_CHUNK_SIZE);
            List<BlockPos> chunk = new ArrayList<>(pathPoints.subList(start, end));
            if (chunk.size() < 2) {
                continue;
            }
            PENDING_QUEUE.add(new PendingEditorOperation(new TrackEditorOperation(TrackEditorMode.FREEHAND, importMaterial(), width, chunk, facing(), preset(), runoffMaterial(), surfaceApplication == SurfaceApplication.FULL, clearHeight), reason));
            chunks++;
        }
        if (reason == QueueReason.IMPORT && pathPoints.size() > 2) {
            List<BlockPos> closingChunk = List.of(pathPoints.get(pathPoints.size() - 1), pathPoints.get(0));
            PENDING_QUEUE.add(new PendingEditorOperation(new TrackEditorOperation(TrackEditorMode.FREEHAND, importMaterial(), width, closingChunk, facing(), preset(), runoffMaterial(), surfaceApplication == SurfaceApplication.FULL), reason));
            chunks++;
        }
        return chunks;
    }

    private void enqueueOperation(TrackEditorOperation operation, QueueReason reason) {
        PENDING_QUEUE.add(new PendingEditorOperation(operation, reason));
        updatePendingQueueStatus(Component.translatable("screen.openwheelracing.track_editor.operation_queued", reason.displayName(), PENDING_QUEUE.size()));
    }

    private void flushPendingQueue() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || PENDING_QUEUE.isEmpty()) {
            return;
        }
        BlockPos playerPos = minecraft.player.blockPosition();
        int sent = 0;
        for (int i = 0; i < PENDING_QUEUE.size() && sent < QUEUE_SENDS_PER_TICK; ) {
            PendingEditorOperation queued = PENDING_QUEUE.get(i);
            if (!isOperationNearPlayer(queued.operation(), playerPos)) {
                i++;
                continue;
            }
            OWRNetwork.CHANNEL.send(new OWRNetwork.TrackEditorPlaceMessage(queued.operation()), PacketDistributor.SERVER.noArg());
            PENDING_QUEUE.remove(i);
            sent++;
        }
        if (sent > 0 || lastQueuedCount != PENDING_QUEUE.size()) {
            updatePendingQueueStatus(Component.translatable("screen.openwheelracing.track_editor.queue", PENDING_QUEUE.size()));
        }
    }

    private void clearPendingQueue() {
        int cleared = PENDING_QUEUE.size();
        if (cleared <= 0) {
            updateClearQueueButton();
            return;
        }
        PENDING_QUEUE.clear();
        updatePendingQueueStatus(Component.translatable("screen.openwheelracing.track_editor.queue_cleared", cleared));
    }

    private void updatePendingQueueStatus(Component status) {
        lastQueuedCount = PENDING_QUEUE.size();
        showNotice(status, 0xFFFFDD55);
        updateClearQueueButton();
    }

    private void updateClearQueueButton() {
        if (clearQueueButton != null) {
            clearQueueButton.active = !PENDING_QUEUE.isEmpty();
        }
    }

    private boolean isOperationNearPlayer(TrackEditorOperation operation, BlockPos playerPos) {
        for (BlockPos point : operation.points()) {
            if (playerPos.distManhattan(point) > QUEUE_SEND_DISTANCE) {
                return false;
            }
        }
        return true;
    }

    private void addPoint(BlockPos point) {
        points.add(point.immutable());
        if (mode() != TrackEditorMode.POLYGON) {
            commitIfReady();
        }
    }

    private void commitIfReady() {
        if (points.size() < requiredPoints()) {
            return;
        }
        TrackEditorMode mode = mode();
        if (mode == TrackEditorMode.POLYGON && points.size() < 3) {
            return;
        }
        TrackEditorOperation operation = new TrackEditorOperation(mode, material(), width, new ArrayList<>(points), facing(), preset(), runoffMaterial(), surfaceApplication == SurfaceApplication.FULL, clearHeight);
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && !isOperationNearPlayer(operation, minecraft.player.blockPosition())) {
            enqueueOperation(operation, QueueReason.TOO_FAR);
        } else {
            OWRNetwork.CHANNEL.send(new OWRNetwork.TrackEditorPlaceMessage(operation), PacketDistributor.SERVER.noArg());
        }
        if (mode == TrackEditorMode.FREEHAND || mode == TrackEditorMode.EDGE) {
            BlockPos last = points.get(points.size() - 1);
            points.clear();
            points.add(last);
        } else {
            points.clear();
        }
    }

    private void removeLastPoint() {
        if (!points.isEmpty()) {
            points.remove(points.size() - 1);
        }
    }

    private void cycleMaterial() {
        if (mode() == TrackEditorMode.EDGE) {
            edgeIndex = (edgeIndex + 1) % EDGE_MATERIALS.length;
        } else {
            pavementIndex = (pavementIndex + 1) % PAVEMENT_MATERIALS.length;
        }
    }

    private void recenterOnPlayer() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            centerX = minecraft.player.getX();
            centerZ = minecraft.player.getZ();
        }
    }

    private int requiredPoints() {
        return switch (mode()) {
            case STRAIGHT, FREEHAND, EDGE -> 2;
            case ARC -> 3;
            case POLYGON -> 3;
        };
    }

    private String requiredPointsText() {
        return mode() == TrackEditorMode.POLYGON ? "3+" : Integer.toString(requiredPoints());
    }

    private TrackEditorMode mode() {
        return MODES[modeIndex];
    }

    private TrackEditorMaterial material() {
        return mode() == TrackEditorMode.EDGE ? EDGE_MATERIALS[edgeIndex] : importMaterial();
    }

    private TrackEditorMaterial importMaterial() {
        return PAVEMENT_MATERIALS[pavementIndex];
    }

    private TrackEditorPreset preset() {
        return PRESETS[presetIndex];
    }

    private TrackEditorMaterial runoffMaterial() {
        return RUNOFF_MATERIALS[runoffMaterialIndex];
    }

    private Direction facing() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player == null ? Direction.NORTH : minecraft.player.getDirection();
    }

    private double blocksPerPixel() {
        return ZOOM_LEVELS[zoomIndex];
    }

    private int panStep() {
        return (int) Math.round(blocksPerPixel() * 24.0);
    }

    private int gridStep() {
        double bpp = blocksPerPixel();
        if (bpp <= 0.5) {
            return 4;
        }
        if (bpp <= 2.0) {
            return 8;
        }
        return 16;
    }

    private boolean isInsideMap(double x, double y) {
        MapBounds map = mapBounds();
        return x >= map.left && x < map.right && y >= map.top && y < map.bottom;
    }

    private BlockPos screenToBlock(double x, double y) {
        MapBounds map = mapBounds();
        int worldX = (int) Math.floor(centerX + (x - (map.left + map.width() / 2.0)) * blocksPerPixel());
        int worldZ = (int) Math.floor(centerZ + (y - (map.top + map.height() / 2.0)) * blocksPerPixel());
        return withEditorY(worldX, worldZ);
    }

    private BlockPos withEditorY(int worldX, int worldZ) {
        Minecraft minecraft = Minecraft.getInstance();
        int y = editY;
        if (elevationMode == ElevationMode.SURFACE && minecraft.level != null) {
            y = mapCache.surfaceY(minecraft.level, worldX, worldZ, editY);
        }
        return new BlockPos(worldX, y, worldZ);
    }

    private int worldToScreenX(double worldX) {
        MapBounds map = mapBounds();
        return (int) Math.round(map.left + map.width() / 2.0 + (worldX - centerX) / blocksPerPixel());
    }

    private int worldToScreenY(double worldZ) {
        MapBounds map = mapBounds();
        return (int) Math.round(map.top + map.height() / 2.0 + (worldZ - centerZ) / blocksPerPixel());
    }

    private void drawLine(GuiGraphics graphics, BlockPos a, BlockPos b, int color) {
        int ax = worldToScreenX(a.getX());
        int ay = worldToScreenY(a.getZ());
        int bx = worldToScreenX(b.getX());
        int by = worldToScreenY(b.getZ());
        int steps = Math.max(Math.abs(bx - ax), Math.abs(by - ay));
        if (steps == 0) {
            graphics.fill(ax, ay, ax + 1, ay + 1, color);
            return;
        }
        for (int i = 0; i <= steps; i++) {
            int x = Math.round(ax + (bx - ax) * (i / (float) steps));
            int y = Math.round(ay + (by - ay) * (i / (float) steps));
            graphics.fill(x, y, x + 1, y + 1, color);
        }
    }

    private int floorToStep(int value, int step) {
        return Math.floorDiv(value, step) * step;
    }

    private Component elevationModeText() {
        return Component.translatable(elevationMode.translationKey());
    }

    private Component surfaceApplicationText() {
        return Component.translatable(surfaceApplication.translationKey());
    }

    private Component elevationModeLabel() {
        return Component.translatable("screen.openwheelracing.track_editor.elevation_button", elevationModeText());
    }

    private Component surfaceApplicationLabel() {
        return Component.translatable("screen.openwheelracing.track_editor.surface_button", surfaceApplicationText());
    }

    private Component clearHeightLabel() {
        return Component.translatable("screen.openwheelracing.track_editor.clear_height", clearHeight);
    }

    private void toggleElevationMode() {
        elevationMode = elevationMode == ElevationMode.SURFACE ? ElevationMode.HEIGHT : ElevationMode.SURFACE;
        updateToggleButtons();
        showNotice(Component.translatable("screen.openwheelracing.track_editor.elevation_mode_changed", elevationModeText()), 0xFFB7FFB7);
    }

    private void toggleSurfaceApplication() {
        surfaceApplication = surfaceApplication == SurfaceApplication.KEYPOINT ? SurfaceApplication.FULL : SurfaceApplication.KEYPOINT;
        updateToggleButtons();
        showNotice(Component.translatable("screen.openwheelracing.track_editor.surface_application_changed", surfaceApplicationText()), 0xFFB7FFB7);
    }

    private void updateToggleButtons() {
        if (elevationModeButton != null) {
            elevationModeButton.setMessage(elevationModeLabel());
        }
        if (surfaceApplicationButton != null) {
            surfaceApplicationButton.setMessage(surfaceApplicationLabel());
        }
    }

    private void showNotice(Component text, int color) {
        notice = new OverlayNotice(text, color, 80);
    }

    private MapBounds mapBounds() {
        int left = OUTER_MARGIN;
        int top = OUTER_MARGIN;
        return new MapBounds(left, top, Math.max(left + 160, this.width - OUTER_MARGIN), Math.max(top + 120, this.height - OUTER_MARGIN));
    }

    private record PendingEditorOperation(TrackEditorOperation operation, QueueReason reason) {
    }

    private enum QueueReason {
        IMPORT("screen.openwheelracing.track_editor.queue_reason.import"),
        TOO_FAR("screen.openwheelracing.track_editor.queue_reason.too_far"),
        TOO_MANY_POINTS("screen.openwheelracing.track_editor.queue_reason.too_many_points"),
        TOO_LARGE("screen.openwheelracing.track_editor.queue_reason.too_large");

        private final String key;

        QueueReason(String key) {
            this.key = key;
        }

        private Component displayName() {
            return Component.translatable(key);
        }
    }

    private record OverlayNotice(Component text, int color, int ticksRemaining) {
    }

    private class ClearHeightSlider extends AbstractSliderButton {
        private ClearHeightSlider(int x, int y, int width, int height) {
            super(x, y, width, height, clearHeightLabel(), clearHeight / (double) TrackEditorOperation.MAX_CLEAR_HEIGHT);
        }

        @Override
        protected void updateMessage() {
            setMessage(clearHeightLabel());
        }

        @Override
        protected void applyValue() {
            clearHeight = (int) Math.round(value * TrackEditorOperation.MAX_CLEAR_HEIGHT);
        }
    }

    private enum ElevationMode {
        SURFACE("screen.openwheelracing.track_editor.elevation.surface"),
        HEIGHT("screen.openwheelracing.track_editor.elevation.height");

        private final String translationKey;

        ElevationMode(String translationKey) {
            this.translationKey = translationKey;
        }

        private String translationKey() {
            return translationKey;
        }
    }

    private enum SurfaceApplication {
        KEYPOINT("screen.openwheelracing.track_editor.surface.keypoint"),
        FULL("screen.openwheelracing.track_editor.surface.full");

        private final String translationKey;

        SurfaceApplication(String translationKey) {
            this.translationKey = translationKey;
        }

        private String translationKey() {
            return translationKey;
        }
    }

    private record LapSimulatorSection(String id, double x, double y, double direction, double width) {
    }

    private record Vec2(double x, double y) {
        private static Vec2 fromDirection(double degrees) {
            double radians = Math.toRadians(degrees);
            return new Vec2(Math.cos(radians), Math.sin(radians));
        }

        private Vec2 add(Vec2 other) {
            return new Vec2(x + other.x, y + other.y);
        }

        private Vec2 subtract(Vec2 other) {
            return new Vec2(x - other.x, y - other.y);
        }

        private Vec2 scale(double scalar) {
            return new Vec2(x * scalar, y * scalar);
        }

        private Vec2 lerp(Vec2 other, double t) {
            return new Vec2(x + (other.x - x) * t, y + (other.y - y) * t);
        }

        private double distanceTo(Vec2 other) {
            double dx = other.x - x;
            double dy = other.y - y;
            return Math.sqrt(dx * dx + dy * dy);
        }
    }

    private record MapBounds(int left, int top, int right, int bottom) {
        int width() {
            return right - left;
        }

        int height() {
            return bottom - top;
        }
    }
}
