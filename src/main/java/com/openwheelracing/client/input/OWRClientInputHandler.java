package com.openwheelracing.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.openwheelracing.client.screen.TrackEditorScreen;
import com.openwheelracing.content.entity.OpenwheelCarEntity;
import com.openwheelracing.network.OWRNetwork;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class OWRClientInputHandler {
    private OWRClientInputHandler() {
    }

    public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            return;
        }

        while (OWRKeyMappings.TRACK_EDITOR.consumeClick()) {
            mc.setScreen(new TrackEditorScreen());
            return;
        }

        while (!(mc.player.getVehicle() instanceof OpenwheelCarEntity) && OWRKeyMappings.MOUNT_CAR.consumeClick()) {
            OWRNetwork.CHANNEL.send(new OWRNetwork.MountCarMessage(), PacketDistributor.SERVER.noArg());
        }

        if (!(mc.player.getVehicle() instanceof OpenwheelCarEntity)) {
            return;
        }

        float throttle = isDown(OWRKeyMappings.THROTTLE)    ? 1.0f : 0.0f;
        float brake    = isDown(OWRKeyMappings.BRAKE)        ? 1.0f : 0.0f;
        float steering = (isDown(OWRKeyMappings.STEER_RIGHT) ? 1.0f : 0.0f)
                       - (isDown(OWRKeyMappings.STEER_LEFT)  ? 1.0f : 0.0f);
        OWRNetwork.CHANNEL.send(new OWRNetwork.DriveInputMessage(throttle, brake, steering), PacketDistributor.SERVER.noArg());

        while (OWRKeyMappings.SHIFT_UP.consumeClick()) {
            OWRNetwork.CHANNEL.send(new OWRNetwork.ShiftMessage(1), PacketDistributor.SERVER.noArg());
        }
        while (OWRKeyMappings.SHIFT_DOWN.consumeClick()) {
            OWRNetwork.CHANNEL.send(new OWRNetwork.ShiftMessage(-1), PacketDistributor.SERVER.noArg());
        }
        while (OWRKeyMappings.EXIT_CAR.consumeClick()) {
            OWRNetwork.CHANNEL.send(new OWRNetwork.ExitCarMessage(), PacketDistributor.SERVER.noArg());
        }
    }

    /** Poll the raw GLFW key state regardless of Minecraft conflict context. */
    private static boolean isDown(net.minecraft.client.KeyMapping mapping) {
        InputConstants.Key key = mapping.getKey();
        com.mojang.blaze3d.platform.Window win = Minecraft.getInstance().getWindow();
        // InputConstants.isKeyDown polls the actual GLFW key state on the window,
        // bypassing KeyMapping's conflict-context suppression that zeros passenger input.
        return InputConstants.isKeyDown(win, key.getValue());
    }
}
