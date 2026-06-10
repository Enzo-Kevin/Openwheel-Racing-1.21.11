package com.openwheelracing.client.render;

import com.openwheelracing.content.entity.OpenwheelCarEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;

public class OpenwheelCarRenderer extends NoopRenderer<OpenwheelCarEntity> {
    public OpenwheelCarRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
