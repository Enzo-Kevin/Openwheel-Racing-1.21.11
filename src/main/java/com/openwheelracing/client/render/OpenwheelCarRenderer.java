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
    private static final Identifier BODY_TEXTURE = Identifier.fromNamespaceAndPath("openwheelracing", "textures/entity/car_body.png");
    private static final Identifier WHITE_TEXTURE = Identifier.fromNamespaceAndPath("openwheelracing", "textures/entity/car_white.png");
    private static final Identifier DARK_TEXTURE = Identifier.fromNamespaceAndPath("openwheelracing", "textures/entity/car_dark.png");
    private static final Identifier WHEEL_TEXTURE = Identifier.fromNamespaceAndPath("openwheelracing", "textures/entity/car_wheel.png");
    private static final RenderType BODY_RENDER_TYPE = RenderTypes.entitySolid(BODY_TEXTURE);
    private static final RenderType WHITE_RENDER_TYPE = RenderTypes.entitySolid(WHITE_TEXTURE);
    private static final RenderType DARK_RENDER_TYPE = RenderTypes.entitySolid(DARK_TEXTURE);
    private static final RenderType WHEEL_RENDER_TYPE = RenderTypes.entitySolid(WHEEL_TEXTURE);
    private static final int WHITE_COLOR = 0xFFFFFFFF;

    public OpenwheelCarRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.7f;
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
    }

    @Override
    public void submit(CarRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraState) {
        super.submit(state, poseStack, nodeCollector, cameraState);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - state.yRot));
        int light = state.lightCoords;
        nodeCollector.submitCustomGeometry(poseStack, BODY_RENDER_TYPE, (pose, consumer) ->
            cuboid(consumer, pose, -0.55f, 0.10f, -1.0f, 0.55f, 0.42f, 0.8f, WHITE_COLOR, light)
        );
        nodeCollector.submitCustomGeometry(poseStack, WHITE_RENDER_TYPE, (pose, consumer) ->
            cuboid(consumer, pose, -0.22f, 0.18f, -1.9f, 0.22f, 0.32f, -1.0f, WHITE_COLOR, light)
        );
        nodeCollector.submitCustomGeometry(poseStack, DARK_RENDER_TYPE, (pose, consumer) -> {
            cuboid(consumer, pose, -1.1f, 0.14f, -2.0f, 1.1f, 0.26f, -1.6f, WHITE_COLOR, light);
            cuboid(consumer, pose, -1.0f, 0.24f, 0.55f, 1.0f, 0.40f, 1.0f, WHITE_COLOR, light);
        });
        nodeCollector.submitCustomGeometry(poseStack, WHEEL_RENDER_TYPE, (pose, consumer) -> {
            wheel(consumer, pose, -0.78f, 0.15f, -1.3f, light);
            wheel(consumer, pose,  0.78f, 0.15f, -1.3f, light);
            wheel(consumer, pose, -0.78f, 0.15f,  0.65f, light);
            wheel(consumer, pose,  0.78f, 0.15f,  0.65f, light);
        });
        poseStack.popPose();
    }

    private static void wheel(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, int light) {
        cuboid(consumer, pose, x - 0.17f, y - 0.22f, z - 0.27f, x + 0.17f, y + 0.22f, z + 0.27f, WHITE_COLOR, light);
    }

    private static void cuboid(VertexConsumer consumer, PoseStack.Pose pose, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int color, int light) {
        quad(consumer, pose, minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, color, light, 0.0f, 0.0f, -1.0f);
        quad(consumer, pose, maxX, minY, maxZ, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, color, light, 0.0f, 0.0f, 1.0f);
        quad(consumer, pose, minX, minY, maxZ, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, color, light, -1.0f, 0.0f, 0.0f);
        quad(consumer, pose, maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, color, light, 1.0f, 0.0f, 0.0f);
        quad(consumer, pose, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, color, light, 0.0f, 1.0f, 0.0f);
        quad(consumer, pose, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, minX, minY, minZ, color, light, 0.0f, -1.0f, 0.0f);
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, int color, int light, float normalX, float normalY, float normalZ) {
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

    public static class CarRenderState extends EntityRenderState {
        public float yRot;
    }
}
