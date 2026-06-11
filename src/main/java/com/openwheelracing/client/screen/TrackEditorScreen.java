package com.openwheelracing.client.screen;

import com.openwheelracing.content.track.TrackEditorMaterial;
import com.openwheelracing.content.track.TrackEditorMode;
import com.openwheelracing.content.track.TrackEditorOperation;
import com.openwheelracing.network.OWRNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

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

    private final List<BlockPos> points = new ArrayList<>();
    private int modeIndex;
    private int pavementIndex;
    private int edgeIndex;
    private int width = 3;

    public TrackEditorScreen() {
        super(Component.translatable("screen.openwheelracing.track_editor"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            addPointFromCrosshair();
            return true;
        }
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            removeLastPoint();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if ((event.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0 && keyCode == GLFW.GLFW_KEY_Z) {
            OWRNetwork.CHANNEL.send(new OWRNetwork.TrackEditorUndoMessage(), PacketDistributor.SERVER.noArg());
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
            default -> {
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int x = 8;
        int y = 8;
        int width = 226;
        int height = 104;
        graphics.fill(x, y, x + width, y + height, 0xAA000000);
        graphics.renderOutline(x, y, width, height, 0xFFDA1A20);
        graphics.drawString(font, title, x + 8, y + 8, 0xFFFFFFFF, false);
        graphics.drawString(font, Component.translatable("screen.openwheelracing.track_editor.mode", mode().name()), x + 8, y + 22, 0xFFE6E6E6, false);
        graphics.drawString(font, Component.translatable("screen.openwheelracing.track_editor.material", material().name()), x + 8, y + 34, 0xFFE6E6E6, false);
        graphics.drawString(font, Component.translatable("screen.openwheelracing.track_editor.width", this.width), x + 8, y + 46, 0xFFE6E6E6, false);
        graphics.drawString(font, Component.translatable("screen.openwheelracing.track_editor.points", points.size(), requiredPointsText()), x + 8, y + 58, 0xFFE6E6E6, false);
        graphics.drawString(font, Component.translatable("screen.openwheelracing.track_editor.help1"), x + 8, y + 74, 0xFFB7FFB7, false);
        graphics.drawString(font, Component.translatable("screen.openwheelracing.track_editor.help2"), x + 8, y + 86, 0xFFB7FFB7, false);
    }

    private void addPointFromCrosshair() {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() == HitResult.Type.MISS) {
            return;
        }
        points.add(hit.getBlockPos().immutable());
        if (mode() != TrackEditorMode.POLYGON) {
            commitIfReady();
        }
    }

    private void commitIfReady() {
        int required = requiredPoints();
        if (points.size() < required) {
            return;
        }
        TrackEditorMode mode = mode();
        if (mode == TrackEditorMode.POLYGON && points.size() < 3) {
            return;
        }
        List<BlockPos> operationPoints = new ArrayList<>(points);
        OWRNetwork.CHANNEL.send(new OWRNetwork.TrackEditorPlaceMessage(new TrackEditorOperation(mode, material(), width, operationPoints, facing())), PacketDistributor.SERVER.noArg());
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

    private void cycleMaterial() {
        if (mode() == TrackEditorMode.EDGE) {
            edgeIndex = (edgeIndex + 1) % EDGE_MATERIALS.length;
        } else {
            pavementIndex = (pavementIndex + 1) % PAVEMENT_MATERIALS.length;
        }
    }

    private TrackEditorMode mode() {
        return MODES[modeIndex];
    }

    private TrackEditorMaterial material() {
        return mode() == TrackEditorMode.EDGE ? EDGE_MATERIALS[edgeIndex] : PAVEMENT_MATERIALS[pavementIndex];
    }

    private Direction facing() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return Direction.NORTH;
        }
        return minecraft.player.getDirection();
    }
}
