package com.openwheelracing.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public final class OWRKeyMappings {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath("openwheelracing", "controls"));

    public static final KeyMapping SHIFT_UP = new KeyMapping("key.openwheelracing.shift_up", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_SPACE, CATEGORY);
    public static final KeyMapping SHIFT_DOWN = new KeyMapping("key.openwheelracing.shift_down", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_SHIFT, CATEGORY);
    public static final KeyMapping EXIT_CAR = new KeyMapping("key.openwheelracing.exit_car", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);

    private OWRKeyMappings() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(SHIFT_UP);
        event.register(SHIFT_DOWN);
        event.register(EXIT_CAR);
    }
}
