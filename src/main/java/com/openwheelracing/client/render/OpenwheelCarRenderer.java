package com.openwheelracing.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.openwheelracing.content.car.CarLivery;
import com.openwheelracing.content.entity.OpenwheelCarEntity;
import java.util.List;

import net.minecraft.client.Minecraft;
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
    private static final Identifier CAR_OBJ = Identifier.fromNamespaceAndPath("openwheelracing", "objmodels/f1_car_2026.obj");
    private static final Identifier WHITE_TEX = Identifier.fromNamespaceAndPath("openwheelracing", "textures/entity/car_white.png");
    private static final RenderType RT_CAR = RenderTypes.entityCutoutNoCull(WHITE_TEX);

    private static final float SOURCE_MIN_X = -0.806891f;
    private static final float SOURCE_MAX_X = 0.806891f;
    private static final float SOURCE_MIN_Y = -0.341034f;
    private static final float SOURCE_MAX_Y = 0.639583f;
    private static final float SOURCE_MIN_Z = -5.237548f;
    private static final float SOURCE_MAX_Z = -0.467113f;
    private static final float TARGET_WIDTH = 2.0f;
    private static final float TARGET_HEIGHT = 1.2f;
    private static final float TARGET_LENGTH = 5.5f;
    private static final float MODEL_X_SCALE = TARGET_WIDTH / (SOURCE_MAX_X - SOURCE_MIN_X);
    private static final float MODEL_Y_SCALE = TARGET_HEIGHT / (SOURCE_MAX_Y - SOURCE_MIN_Y);
    private static final float MODEL_Z_SCALE = TARGET_LENGTH / (SOURCE_MAX_Z - SOURCE_MIN_Z);
    private static final float MODEL_Z_CENTER = (SOURCE_MIN_Z + SOURCE_MAX_Z) * 0.5f;

    private static final int CARBON = rgb(24, 24, 28);
    private static final int TYRE = rgb(18, 18, 22);
    private static final int METAL = rgb(155, 155, 150);

    private static ColoredObjModel carModel;
    // Pre-baked color array per livery index — populated once per livery, reused every frame.
    private static final java.util.HashMap<Integer, int[]> BAKED_COLORS = new java.util.HashMap<>();

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
        state.frontWheelSteerDegrees = car.getFrontWheelSteerDegrees();
        state.lightCoords = 15728880;
        state.tyreCompound = car.getTyreCompound();
        state.livery = car.getLivery();
    }

    @Override
    public void submit(CarRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraState) {
        super.submit(state, poseStack, nodeCollector, cameraState);
        loadModel();

        int[] bakedColors = BAKED_COLORS.computeIfAbsent(state.livery, liveryIndex -> {
            CarLivery livery = CarLivery.fromIndex(liveryIndex);
            return carModel.bakeColors(face -> liveryColor(face, livery));
        });

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yRot));

        int light = state.lightCoords;
        nodeCollector.submitCustomGeometry(poseStack, RT_CAR, (pose, consumer) -> drawModel(consumer, pose, carModel, bakedColors, light));

        poseStack.popPose();
    }

    private static void loadModel() {
        if (carModel == null) {
            carModel = ColoredObjModel.load(Minecraft.getInstance().getResourceManager(), CAR_OBJ);
        }
    }

    private static void drawModel(VertexConsumer consumer, PoseStack.Pose pose, ColoredObjModel model, int[] bakedColors, int light) {
        List<ColoredObjModel.Face> faces = model.faces;
        for (int i = 0; i < faces.size(); i++) {
            ColoredObjModel.Face face = faces.get(i);
            int color = bakedColors[i];
            vertex(consumer, pose, face.x0(), face.y0(), face.z0(), face.nx(), face.ny(), face.nz(), color, light);
            vertex(consumer, pose, face.x1(), face.y1(), face.z1(), face.nx(), face.ny(), face.nz(), color, light);
            vertex(consumer, pose, face.x2(), face.y2(), face.z2(), face.nx(), face.ny(), face.nz(), color, light);
            vertex(consumer, pose, face.x2(), face.y2(), face.z2(), face.nx(), face.ny(), face.nz(), color, light);
        }
    }

    private static int liveryColor(ColoredObjModel.Face face, CarLivery livery) {
        int materialRgb = face.materialRgb();
        int r = (materialRgb >> 16) & 255;
        int g = (materialRgb >> 8) & 255;
        int b = materialRgb & 255;
        int brightness = Math.max(r, Math.max(g, b));

        if (isWheelFace(face)) {
            return TYRE;
        }
        if (r == 0 && g == 0 && b == 0) {
            return CARBON;
        }
        if (brightness <= 4) {
            return livery.bodySide();
        }
        if (brightness <= 40) {
            return TYRE;
        }
        if (r > 180 && g < 90 && b < 80) {
            return livery.accent2Side();
        }
        if (r > 220 && g > 180 && b > 120) {
            return livery.accent2Top();
        }
        if (brightness > 120 && Math.abs(r - g) < 18 && Math.abs(g - b) < 18) {
            return livery.accent1Side();
        }
        if (brightness > 120) {
            return METAL;
        }
        return livery.bodyBottom();
    }

    private static boolean isWheelFace(ColoredObjModel.Face face) {
        float centerX = (face.x0() + face.x1() + face.x2()) / 3.0f;
        float centerY = (face.y0() + face.y1() + face.y2()) / 3.0f;
        float centerZ = (face.z0() + face.z1() + face.z2()) / 3.0f;
        return inWheelBox(centerX, centerY, centerZ, -0.84f, -0.40f, -4.78f, -4.05f)
            || inWheelBox(centerX, centerY, centerZ, 0.40f, 0.84f, -4.78f, -4.05f)
            || inWheelBox(centerX, centerY, centerZ, -0.84f, -0.40f, -1.85f, -1.20f)
            || inWheelBox(centerX, centerY, centerZ, 0.40f, 0.84f, -1.85f, -1.20f);
    }

    private static boolean inWheelBox(float x, float y, float z, float minX, float maxX, float minZ, float maxZ) {
        return x >= minX && x <= maxX
            && y >= -0.32f && y <= 0.25f
            && z >= minZ && z <= maxZ;
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float normalX, float normalY, float normalZ, int color, int light) {
        consumer.addVertex(pose, x * MODEL_X_SCALE, (y - SOURCE_MIN_Y) * MODEL_Y_SCALE, (z - MODEL_Z_CENTER) * MODEL_Z_SCALE)
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
        public float frontWheelSteerDegrees;
        public int lightCoords;
        public int tyreCompound;
        public int livery;
    }
}
