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

    // All textures remain as solid 1×1 colour anchors. Shading is done per-vertex.
    private static final Identifier BODY_TEX   = Identifier.fromNamespaceAndPath("openwheelracing", "textures/entity/car_body.png");
    private static final Identifier WHITE_TEX  = Identifier.fromNamespaceAndPath("openwheelracing", "textures/entity/car_white.png");
    private static final Identifier DARK_TEX   = Identifier.fromNamespaceAndPath("openwheelracing", "textures/entity/car_dark.png");
    private static final Identifier WHEEL_TEX  = Identifier.fromNamespaceAndPath("openwheelracing", "textures/entity/car_wheel.png");

    private static final RenderType RT_BODY  = RenderTypes.entitySolid(BODY_TEX);
    private static final RenderType RT_WHITE = RenderTypes.entitySolid(WHITE_TEX);
    private static final RenderType RT_DARK  = RenderTypes.entitySolid(DARK_TEX);
    private static final RenderType RT_WHEEL = RenderTypes.entitySolid(WHEEL_TEX);

    // ── Livery palette ───────────────────────────────────────────────────────
    // Primary: deep navy blue body
    private static final int COL_NAV_TOP    = rgb( 18,  38,  98);
    private static final int COL_NAV_SIDE   = rgb( 12,  26,  72);
    private static final int COL_NAV_BOTTOM = rgb(  8,  16,  48);
    // Accent: gold/yellow (nose, wing leading edges, sidepod spine)
    private static final int COL_GLD_TOP    = rgb(240, 185,  20);
    private static final int COL_GLD_SIDE   = rgb(200, 148,  10);
    private static final int COL_GLD_BOTTOM = rgb(150, 108,   5);
    // Sky-blue sidepod panel inset
    private static final int COL_SKY_TOP    = rgb( 80, 160, 220);
    private static final int COL_SKY_SIDE   = rgb( 55, 120, 180);
    private static final int COL_SKY_BOTTOM = rgb( 35,  85, 140);
    // Gloss black halo / cockpit rim
    private static final int COL_HAL_TOP    = rgb( 40,  40,  44);
    private static final int COL_HAL_SIDE   = rgb( 25,  25,  28);
    private static final int COL_HAL_BOTTOM = rgb( 12,  12,  14);
    // Matte carbon (floor, diffuser, rear wing)
    private static final int COL_CRB_TOP    = rgb( 48,  48,  52);
    private static final int COL_CRB_SIDE   = rgb( 32,  32,  36);
    private static final int COL_CRB_BOTTOM = rgb( 18,  18,  20);
    // Tyre rubber (warm black)
    private static final int COL_TYR_OUTER  = rgb( 32,  28,  24);
    private static final int COL_TYR_SIDE   = rgb( 22,  18,  16);
    // Wheel disc – bronze-tinted aluminium
    private static final int COL_DSC_FACE   = rgb(195, 165, 110);
    private static final int COL_DSC_RIM    = rgb(145, 115,  75);

    public OpenwheelCarRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.8f;
    }

    @Override
    public CarRenderState createRenderState() { return new CarRenderState(); }

    @Override
    public void extractRenderState(OpenwheelCarEntity car, CarRenderState state, float pt) {
        super.extractRenderState(car, state, pt);
        state.yRot = car.getYRot(pt);
        state.lightCoords = 15728880;
    }

    @Override
    public void submit(CarRenderState state, PoseStack ps, SubmitNodeCollector nc, CameraRenderState cs) {
        super.submit(state, ps, nc, cs);

        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(180.0f - state.yRot));
        int lx = state.lightCoords;

        // ── Red body ─────────────────────────────────────────────────────────
        nc.submitCustomGeometry(ps, RT_BODY, (pose, vc) -> {
            // Main central tub / monocoque
            box(vc, pose, -0.35f, 0.12f, -0.90f,  0.35f, 0.44f,  0.65f, COL_NAV_TOP, COL_NAV_SIDE, COL_NAV_BOTTOM, lx);
            // Nose cone (tapers narrower – approximate with a thin box)
            box(vc, pose, -0.18f, 0.14f, -1.70f,  0.18f, 0.36f, -0.90f, COL_NAV_TOP, COL_NAV_SIDE, COL_NAV_BOTTOM, lx);
            // Sidepods – left
            box(vc, pose, -0.68f, 0.12f, -0.60f, -0.35f, 0.38f,  0.55f, COL_NAV_TOP, COL_NAV_SIDE, COL_NAV_BOTTOM, lx);
            // Sidepods – right
            box(vc, pose,  0.35f, 0.12f, -0.60f,  0.68f, 0.38f,  0.55f, COL_NAV_TOP, COL_NAV_SIDE, COL_NAV_BOTTOM, lx);
            // Engine cover / shark fin behind cockpit
            box(vc, pose, -0.10f, 0.44f,  0.10f,  0.10f, 0.72f,  0.55f, COL_NAV_TOP, COL_NAV_SIDE, COL_NAV_BOTTOM, lx);
            // Rear diffuser hump
            box(vc, pose, -0.30f, 0.12f,  0.55f,  0.30f, 0.30f,  0.78f, COL_NAV_TOP, COL_NAV_SIDE, COL_NAV_BOTTOM, lx);
        });

        // ── Accent panels / cockpit surround ─────────────────────────────────
        nc.submitCustomGeometry(ps, RT_WHITE, (pose, vc) -> {
            box(vc, pose, -0.25f, 0.44f, -0.55f,  0.25f, 0.52f,  0.10f, COL_HAL_TOP, COL_HAL_SIDE, COL_HAL_BOTTOM, lx);
            box(vc, pose, -0.07f, 0.37f, -1.72f,  0.07f, 0.42f, -0.22f, COL_GLD_TOP, COL_GLD_SIDE, COL_GLD_BOTTOM, lx);
            box(vc, pose, -0.69f, 0.31f, -0.38f, -0.36f, 0.40f,  0.34f, COL_SKY_TOP, COL_SKY_SIDE, COL_SKY_BOTTOM, lx);
            box(vc, pose,  0.36f, 0.31f, -0.38f,  0.69f, 0.40f,  0.34f, COL_SKY_TOP, COL_SKY_SIDE, COL_SKY_BOTTOM, lx);
            box(vc, pose, -0.06f, 0.73f,  0.14f,  0.06f, 0.80f,  0.50f, COL_GLD_TOP, COL_GLD_SIDE, COL_GLD_BOTTOM, lx);
        });

        // ── Carbon / dark elements ────────────────────────────────────────────
        nc.submitCustomGeometry(ps, RT_DARK, (pose, vc) -> {
            // Front wing main plane
            box(vc, pose, -1.00f, 0.10f, -1.92f,  1.00f, 0.17f, -1.55f, COL_CRB_TOP, COL_CRB_SIDE, COL_CRB_BOTTOM, lx);
            // Front wing end-plates
            box(vc, pose, -1.00f, 0.10f, -1.92f, -0.88f, 0.30f, -1.55f, COL_GLD_TOP, COL_GLD_SIDE, COL_GLD_BOTTOM, lx);
            box(vc, pose,  0.88f, 0.10f, -1.92f,  1.00f, 0.30f, -1.55f, COL_GLD_TOP, COL_GLD_SIDE, COL_GLD_BOTTOM, lx);
            box(vc, pose, -0.55f, 0.18f, -1.94f,  0.55f, 0.23f, -1.86f, COL_GLD_TOP, COL_GLD_SIDE, COL_GLD_BOTTOM, lx);
            // Rear wing main plane
            box(vc, pose, -0.90f, 0.70f,  0.62f,  0.90f, 0.78f,  0.90f, COL_CRB_TOP, COL_CRB_SIDE, COL_CRB_BOTTOM, lx);
            // Rear wing end-plates
            box(vc, pose, -0.90f, 0.30f,  0.62f, -0.78f, 0.78f,  0.90f, COL_GLD_TOP, COL_GLD_SIDE, COL_GLD_BOTTOM, lx);
            box(vc, pose,  0.78f, 0.30f,  0.62f,  0.90f, 0.78f,  0.90f, COL_GLD_TOP, COL_GLD_SIDE, COL_GLD_BOTTOM, lx);
            // Rear wing DRS beam
            box(vc, pose, -0.90f, 0.54f,  0.70f,  0.90f, 0.60f,  0.78f, COL_SKY_TOP, COL_SKY_SIDE, COL_SKY_BOTTOM, lx);
            // Floor / skid plate
            box(vc, pose, -0.65f, 0.08f, -0.80f,  0.65f, 0.13f,  0.70f, COL_CRB_TOP, COL_CRB_SIDE, COL_CRB_BOTTOM, lx);
        });

        // ── Wheels: tyre barrel + disc face ──────────────────────────────────
        nc.submitCustomGeometry(ps, RT_WHEEL, (pose, vc) -> {
            wheel(vc, pose, -0.82f, 0.22f, -1.22f, lx); // front-left
            wheel(vc, pose,  0.82f, 0.22f, -1.22f, lx); // front-right
            wheel(vc, pose, -0.82f, 0.22f,  0.55f, lx); // rear-left
            wheel(vc, pose,  0.82f, 0.22f,  0.55f, lx); // rear-right
        });

        ps.popPose();
    }

    // ── Wheel: wide tyre barrel + inset disc ─────────────────────────────────
    private static void wheel(VertexConsumer vc, PoseStack.Pose pose, float cx, float cy, float cz, int light) {
        float tw = 0.22f; // half-width of tyre
        float tr = 0.24f; // tyre radius (half-height)
        float dw = 0.04f; // disc inset depth from outer face
        // Tyre barrel
        box(vc, pose, cx-tw, cy-tr, cz-tr, cx+tw, cy+tr, cz+tr, COL_TYR_OUTER, COL_TYR_SIDE, COL_TYR_SIDE, light);
        // Outer disc face
        flat(vc, pose, cx-tw-0.01f, cy-tr*0.72f, cz-tr*0.72f,
                       cx-tw-0.01f, cy-tr*0.72f, cz+tr*0.72f,
                       cx-tw-0.01f, cy+tr*0.72f, cz+tr*0.72f,
                       cx-tw-0.01f, cy+tr*0.72f, cz-tr*0.72f,
                       COL_DSC_FACE, light, -1,0,0);
        flat(vc, pose, cx+tw+0.01f, cy-tr*0.72f, cz-tr*0.72f,
                       cx+tw+0.01f, cy+tr*0.72f, cz-tr*0.72f,
                       cx+tw+0.01f, cy+tr*0.72f, cz+tr*0.72f,
                       cx+tw+0.01f, cy-tr*0.72f, cz+tr*0.72f,
                       COL_DSC_FACE, light, 1,0,0);
    }

    // ── Shaded box: each face gets a brightness multiplier ───────────────────
    private static void box(VertexConsumer vc, PoseStack.Pose pose,
                            float x0, float y0, float z0, float x1, float y1, float z1,
                            int top, int side, int bottom, int light) {
        // -Z front
        flat(vc,pose, x0,y0,z0, x1,y0,z0, x1,y1,z0, x0,y1,z0, side,  light, 0,0,-1);
        // +Z back
        flat(vc,pose, x1,y0,z1, x0,y0,z1, x0,y1,z1, x1,y1,z1, side,  light, 0,0,1);
        // -X left
        flat(vc,pose, x0,y0,z1, x0,y0,z0, x0,y1,z0, x0,y1,z1, side,  light,-1,0,0);
        // +X right
        flat(vc,pose, x1,y0,z0, x1,y0,z1, x1,y1,z1, x1,y1,z0, side,  light, 1,0,0);
        // +Y top
        flat(vc,pose, x0,y1,z0, x1,y1,z0, x1,y1,z1, x0,y1,z1, top,   light, 0,1,0);
        // -Y bottom
        flat(vc,pose, x0,y0,z1, x1,y0,z1, x1,y0,z0, x0,y0,z0, bottom,light, 0,-1,0);
    }

    private static void flat(VertexConsumer vc, PoseStack.Pose pose,
                             float x1,float y1,float z1, float x2,float y2,float z2,
                             float x3,float y3,float z3, float x4,float y4,float z4,
                             int color, int light, float nx,float ny,float nz) {
        vtx(vc,pose,x1,y1,z1,color,light,nx,ny,nz);
        vtx(vc,pose,x2,y2,z2,color,light,nx,ny,nz);
        vtx(vc,pose,x3,y3,z3,color,light,nx,ny,nz);
        vtx(vc,pose,x4,y4,z4,color,light,nx,ny,nz);
    }

    private static void vtx(VertexConsumer vc, PoseStack.Pose pose,
                            float x, float y, float z, int color, int light,
                            float nx, float ny, float nz) {
        vc.addVertex(pose, x, y, z)
          .setColor(color)
          .setUv(0f, 0f)
          .setOverlay(OverlayTexture.NO_OVERLAY)
          .setLight(light)
          .setNormal(pose, nx, ny, nz);
    }

    private static int rgb(int r, int g, int b) {
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static class CarRenderState extends EntityRenderState {
        public float yRot;
        public int lightCoords;
    }
}
