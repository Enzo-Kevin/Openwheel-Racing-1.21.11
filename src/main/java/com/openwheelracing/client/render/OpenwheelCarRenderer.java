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
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/misc/white.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entitySolid(TEXTURE);
    private static final int BODY_COLOR = 0xFFE01B24;
    private static final int NOSE_COLOR = 0xFFFFFFFF;
    private static final int WING_COLOR = 0xFF202020;
    private static final int WHEEL_COLOR = 0xFF080808;

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
    }

    @Override
    public void submit(CarRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraState) {
        super.submit(state, poseStack, nodeCollector, cameraState);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - state.yRot));
        nodeCollector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, consumer) -> {
            cuboid(consumer, pose, -0.45f, 0.15f, -0.85f, 0.45f, 0.45f, 0.65f, BODY_COLOR, state.lightCoords);
            cuboid(consumer, pose, -0.18f, 0.22f, -1.55f, 0.18f, 0.34f, -0.85f, NOSE_COLOR, state.lightCoords);
            cuboid(consumer, pose, -0.85f, 0.18f, -1.6f, 0.85f, 0.28f, -1.35f, WING_COLOR, state.lightCoords);
            cuboid(consumer, pose, -0.8f, 0.28f, 0.45f, 0.8f, 0.42f, 0.8f, WING_COLOR, state.lightCoords);
            wheel(consumer, pose, -0.62f, 0.18f, -1.05f, state.lightCoords);
            wheel(consumer, pose, 0.62f, 0.18f, -1.05f, state.lightCoords);
            wheel(consumer, pose, -0.62f, 0.18f, 0.55f, state.lightCoords);
            wheel(consumer, pose, 0.62f, 0.18f, 0.55f, state.lightCoords);
        });
        poseStack.popPose();
    }

    private static void wheel(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, int light) {
        cuboid(consumer, pose, x - 0.14f, y - 0.18f, z - 0.22f, x + 0.14f, y + 0.18f, z + 0.22f, WHEEL_COLOR, light);
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
