package com.openwheelracing.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.openwheelracing.content.entity.OpenwheelCarEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class OpenwheelCarRenderer extends EntityRenderer<OpenwheelCarEntity, OpenwheelCarRenderer.CarRenderState> {
    private static final Identifier BODY_TEX = Identifier.fromNamespaceAndPath("openwheelracing", "textures/entity/car_body.png");
    private static final Identifier WHITE_TEX = Identifier.fromNamespaceAndPath("openwheelracing", "textures/entity/car_white.png");
    private static final Identifier DARK_TEX = Identifier.fromNamespaceAndPath("openwheelracing", "textures/entity/car_dark.png");
    private static final Identifier WHEEL_TEX = Identifier.fromNamespaceAndPath("openwheelracing", "textures/entity/car_wheel.png");

    private static final RenderType RT_BODY = RenderTypes.entitySolid(BODY_TEX);
    private static final RenderType RT_WHITE = RenderTypes.entitySolid(WHITE_TEX);
    private static final RenderType RT_DARK = RenderTypes.entitySolid(DARK_TEX);
    private static final RenderType RT_WHEEL = RenderTypes.entitySolid(WHEEL_TEX);

    private static final int NAV_TOP = rgb(18, 38, 98);
    private static final int NAV_SIDE = rgb(12, 26, 72);
    private static final int NAV_BOTTOM = rgb(8, 16, 48);
    private static final int GOLD_TOP = rgb(240, 185, 20);
    private static final int GOLD_SIDE = rgb(200, 148, 10);
    private static final int GOLD_BOTTOM = rgb(150, 108, 5);
    private static final int SKY_TOP = rgb(80, 160, 220);
    private static final int SKY_SIDE = rgb(55, 120, 180);
    private static final int SKY_BOTTOM = rgb(35, 85, 140);
    private static final int HALO_TOP = rgb(40, 40, 44);
    private static final int HALO_SIDE = rgb(25, 25, 28);
    private static final int HALO_BOTTOM = rgb(12, 12, 14);
    private static final int CARBON_TOP = rgb(48, 48, 52);
    private static final int CARBON_SIDE = rgb(32, 32, 36);
    private static final int CARBON_BOTTOM = rgb(18, 18, 20);
    private static final int TYRE_TOP = rgb(34, 30, 26);
    private static final int TYRE_SIDE = rgb(22, 18, 16);
    private static final int TYRE_DARK = rgb(10, 9, 8);
    private static final int DISC_FACE = rgb(195, 165, 110);
    private static final int DISC_SIDE = rgb(145, 115, 75);

    private static final int[] COMPOUND_RIM_COLORS = {
        rgb(230, 32, 32),
        rgb(240, 210, 30),
        rgb(235, 235, 230),
        rgb(40, 200, 70),
        rgb(45, 115, 240)
    };

    public OpenwheelCarRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 1.2f;
    }

    @Override
    public CarRenderState createRenderState() {
        return new CarRenderState();
    }

    @Override
    public void extractRenderState(OpenwheelCarEntity car, CarRenderState state, float partialTick) {
        super.extractRenderState(car, state, partialTick);
        state.yRot = car.getYRot(partialTick);
        state.lightCoords = 15728880;
        state.tyreCompound = car.getSetup().grip();
    }

    @Override
    public void submit(CarRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraState) {
        super.submit(state, poseStack, nodeCollector, cameraState);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - state.yRot));
        poseStack.scale(1.22f, 1.28f, 1.50f);

        int light = state.lightCoords;
        int rimColor = compoundRimColor(state.tyreCompound);

        nodeCollector.submitCustomGeometry(poseStack, RT_BODY, (pose, consumer) -> {
            box(consumer, pose, -0.38f, 0.12f, -1.35f, 0.38f, 0.58f, 0.98f, NAV_TOP, NAV_SIDE, NAV_BOTTOM, light);
            box(consumer, pose, -0.16f, 0.16f, -2.80f, 0.16f, 0.44f, -1.35f, NAV_TOP, NAV_SIDE, NAV_BOTTOM, light);
            box(consumer, pose, -0.78f, 0.11f, -0.88f, -0.38f, 0.48f, 0.72f, NAV_TOP, NAV_SIDE, NAV_BOTTOM, light);
            box(consumer, pose, 0.38f, 0.11f, -0.88f, 0.78f, 0.48f, 0.72f, NAV_TOP, NAV_SIDE, NAV_BOTTOM, light);
            box(consumer, pose, -0.12f, 0.58f, 0.02f, 0.12f, 1.02f, 0.92f, NAV_TOP, NAV_SIDE, NAV_BOTTOM, light);
            box(consumer, pose, -0.34f, 0.12f, 0.82f, 0.34f, 0.38f, 1.20f, NAV_TOP, NAV_SIDE, NAV_BOTTOM, light);
            box(consumer, pose, -0.24f, 0.42f, -0.90f, 0.24f, 0.66f, -0.12f, NAV_TOP, NAV_SIDE, NAV_BOTTOM, light);
        });

        nodeCollector.submitCustomGeometry(poseStack, RT_WHITE, (pose, consumer) -> {
            box(consumer, pose, -0.065f, 0.46f, -2.76f, 0.065f, 0.53f, -0.34f, GOLD_TOP, GOLD_SIDE, GOLD_BOTTOM, light);
            box(consumer, pose, -0.79f, 0.40f, -0.60f, -0.39f, 0.52f, 0.50f, SKY_TOP, SKY_SIDE, SKY_BOTTOM, light);
            box(consumer, pose, 0.39f, 0.40f, -0.60f, 0.79f, 0.52f, 0.50f, SKY_TOP, SKY_SIDE, SKY_BOTTOM, light);
            box(consumer, pose, -0.07f, 1.03f, 0.12f, 0.07f, 1.14f, 0.82f, GOLD_TOP, GOLD_SIDE, GOLD_BOTTOM, light);
            box(consumer, pose, -0.45f, 0.62f, 0.98f, 0.45f, 0.72f, 1.12f, GOLD_TOP, GOLD_SIDE, GOLD_BOTTOM, light);
        });

        nodeCollector.submitCustomGeometry(poseStack, RT_DARK, (pose, consumer) -> {
            box(consumer, pose, -1.25f, 0.09f, -3.08f, 1.25f, 0.18f, -2.45f, CARBON_TOP, CARBON_SIDE, CARBON_BOTTOM, light);
            box(consumer, pose, -1.25f, 0.09f, -3.08f, -1.08f, 0.40f, -2.45f, GOLD_TOP, GOLD_SIDE, GOLD_BOTTOM, light);
            box(consumer, pose, 1.08f, 0.09f, -3.08f, 1.25f, 0.40f, -2.45f, GOLD_TOP, GOLD_SIDE, GOLD_BOTTOM, light);
            box(consumer, pose, -0.62f, 0.21f, -3.12f, 0.62f, 0.29f, -3.00f, GOLD_TOP, GOLD_SIDE, GOLD_BOTTOM, light);
            box(consumer, pose, -1.10f, 1.02f, 1.10f, 1.10f, 1.14f, 1.55f, CARBON_TOP, CARBON_SIDE, CARBON_BOTTOM, light);
            box(consumer, pose, -1.10f, 0.38f, 1.10f, -0.92f, 1.14f, 1.55f, GOLD_TOP, GOLD_SIDE, GOLD_BOTTOM, light);
            box(consumer, pose, 0.92f, 0.38f, 1.10f, 1.10f, 1.14f, 1.55f, GOLD_TOP, GOLD_SIDE, GOLD_BOTTOM, light);
            box(consumer, pose, -1.10f, 0.78f, 1.22f, 1.10f, 0.88f, 1.36f, SKY_TOP, SKY_SIDE, SKY_BOTTOM, light);
            box(consumer, pose, -0.78f, 0.06f, -1.12f, 0.78f, 0.14f, 1.12f, CARBON_TOP, CARBON_SIDE, CARBON_BOTTOM, light);
            box(consumer, pose, -0.55f, 0.06f, 1.02f, 0.55f, 0.26f, 1.34f, CARBON_TOP, CARBON_SIDE, CARBON_BOTTOM, light);
        });

        nodeCollector.submitCustomGeometry(poseStack, RT_WHEEL, (pose, consumer) -> {
            wheel(consumer, pose, -1.10f, 0.26f, -1.98f, rimColor, light);
            wheel(consumer, pose, 1.10f, 0.26f, -1.98f, rimColor, light);
            wheel(consumer, pose, -1.10f, 0.26f, 1.02f, rimColor, light);
            wheel(consumer, pose, 1.10f, 0.26f, 1.02f, rimColor, light);
        });

        poseStack.popPose();
    }

    private static void wheel(VertexConsumer consumer, PoseStack.Pose pose, float cx, float cy, float cz, int rimColor, int light) {
        float halfWidth = 0.27f;
        float halfHeight = 0.25f;
        float halfLength = 0.33f;

        box(consumer, pose, cx - halfWidth, cy - halfHeight, cz - halfLength, cx + halfWidth, cy + halfHeight, cz + halfLength, TYRE_TOP, TYRE_SIDE, TYRE_DARK, light);
        box(consumer, pose, cx - halfWidth - 0.01f, cy - 0.04f, cz - halfLength, cx + halfWidth + 0.01f, cy + 0.04f, cz - halfLength + 0.04f, TYRE_DARK, TYRE_DARK, TYRE_DARK, light);
        box(consumer, pose, cx - halfWidth - 0.01f, cy - 0.04f, cz + halfLength - 0.04f, cx + halfWidth + 0.01f, cy + 0.04f, cz + halfLength, TYRE_DARK, TYRE_DARK, TYRE_DARK, light);
        box(consumer, pose, cx - halfWidth - 0.01f, cy + halfHeight - 0.05f, cz - 0.25f, cx + halfWidth + 0.01f, cy + halfHeight + 0.01f, cz + 0.25f, TYRE_DARK, TYRE_DARK, TYRE_DARK, light);

        rimFace(consumer, pose, cx - halfWidth - 0.03f, cy, cz, 0.06f, rimColor, light);
        rimFace(consumer, pose, cx + halfWidth + 0.03f, cy, cz, 0.06f, rimColor, light);
    }

    private static void rimFace(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float thickness, int rimColor, int light) {
        float x0 = x - thickness * 0.5f;
        float x1 = x + thickness * 0.5f;
        float top = y + 0.15f;
        float bottom = y - 0.15f;
        float front = z - 0.21f;
        float back = z + 0.21f;

        box(consumer, pose, x0, top - 0.055f, front, x1, top + 0.055f, back, rimColor, rimColor, rimColor, light);
        box(consumer, pose, x0, bottom - 0.055f, front, x1, bottom + 0.055f, back, rimColor, rimColor, rimColor, light);
        box(consumer, pose, x0, bottom, front - 0.055f, x1, top, front + 0.055f, rimColor, rimColor, rimColor, light);
        box(consumer, pose, x0, bottom, back - 0.055f, x1, top, back + 0.055f, rimColor, rimColor, rimColor, light);
        box(consumer, pose, x0 - 0.005f, y - 0.075f, z - 0.075f, x1 + 0.005f, y + 0.075f, z + 0.075f, DISC_FACE, DISC_SIDE, DISC_SIDE, light);
    }

    private static int compoundRimColor(int tyreCompound) {
        return COMPOUND_RIM_COLORS[Math.max(0, Math.min(COMPOUND_RIM_COLORS.length - 1, tyreCompound))];
    }

    private static void box(VertexConsumer consumer, PoseStack.Pose pose,
                            float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                            int top, int side, int bottom, int light) {
        face(consumer, pose, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, minX, minY, minZ, side, light, 0.0f, 0.0f, -1.0f);
        face(consumer, pose, maxX, maxY, maxZ, minX, maxY, maxZ, minX, minY, maxZ, maxX, minY, maxZ, side, light, 0.0f, 0.0f, 1.0f);
        face(consumer, pose, minX, maxY, maxZ, minX, maxY, minZ, minX, minY, minZ, minX, minY, maxZ, side, light, -1.0f, 0.0f, 0.0f);
        face(consumer, pose, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, side, light, 1.0f, 0.0f, 0.0f);
        face(consumer, pose, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ, top, light, 0.0f, 1.0f, 0.0f);
        face(consumer, pose, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, bottom, light, 0.0f, -1.0f, 0.0f);
    }

    private static void face(VertexConsumer consumer, PoseStack.Pose pose,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4,
                             int color, int light, float normalX, float normalY, float normalZ) {
        vertex(consumer, pose, x1, y1, z1, color, light, normalX, normalY, normalZ);
        vertex(consumer, pose, x2, y2, z2, color, light, normalX, normalY, normalZ);
        vertex(consumer, pose, x3, y3, z3, color, light, normalX, normalY, normalZ);
        vertex(consumer, pose, x4, y4, z4, color, light, normalX, normalY, normalZ);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, int color, int light, float normalX, float normalY, float normalZ) {
        consumer.addVertex(pose, x, y, z)
            .setColor(color)
            .setUv(0.0f, 0.0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(pose, normalX, normalY, normalZ);
    }

    private static int rgb(int r, int g, int b) {
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static class CarRenderState extends EntityRenderState {
        public float yRot;
        public int lightCoords;
        public int tyreCompound;
    }
}
