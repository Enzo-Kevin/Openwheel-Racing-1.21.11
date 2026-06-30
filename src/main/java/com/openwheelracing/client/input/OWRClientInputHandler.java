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
    private static boolean shiftUpWasDown;
    private static boolean shiftDownWasDown;

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

        OpenwheelCarEntity car = (OpenwheelCarEntity) mc.player.getVehicle();
        float throttle = isDown(OWRKeyMappings.THROTTLE)    ? 1.0f : 0.0f;
        float brake    = isDown(OWRKeyMappings.BRAKE)        ? 1.0f : 0.0f;
        float steering = (isDown(OWRKeyMappings.STEER_RIGHT) ? 1.0f : 0.0f)
                       - (isDown(OWRKeyMappings.STEER_LEFT)  ? 1.0f : 0.0f);
        car.tickLocalClientMovement(throttle, brake, steering);
        OWRNetwork.CHANNEL.send(new OWRNetwork.DriveInputMessage(throttle, brake, steering), PacketDistributor.SERVER.noArg());

        boolean shiftUpDown = isRawKeyDown(GLFW.GLFW_KEY_I);
        boolean shiftDownDown = isRawKeyDown(GLFW.GLFW_KEY_K);
        if (shiftUpDown && !shiftUpWasDown) {
            car.shiftLocal(1);
            OWRNetwork.CHANNEL.send(new OWRNetwork.ShiftMessage(1), PacketDistributor.SERVER.noArg());
        }
        if (shiftDownDown && !shiftDownWasDown) {
            car.shiftLocal(-1);
            OWRNetwork.CHANNEL.send(new OWRNetwork.ShiftMessage(-1), PacketDistributor.SERVER.noArg());
        }
        shiftUpWasDown = shiftUpDown;
        shiftDownWasDown = shiftDownDown;
        while (OWRKeyMappings.EXIT_CAR.consumeClick()) {
            OWRNetwork.CHANNEL.send(new OWRNetwork.ExitCarMessage(), PacketDistributor.SERVER.noArg());
        }
        while (OWRKeyMappings.TOGGLE_ABS.consumeClick()) {
            car.toggleAbs();
            OWRNetwork.CHANNEL.send(new OWRNetwork.ToggleAbsMessage(), PacketDistributor.SERVER.noArg());
        }
        while (OWRKeyMappings.TOGGLE_TC.consumeClick()) {
            car.toggleTractionControl();
            OWRNetwork.CHANNEL.send(new OWRNetwork.ToggleTractionControlMessage(), PacketDistributor.SERVER.noArg());
        }
    }

    /** Poll the raw GLFW key state regardless of Minecraft conflict context. */
    private static boolean isDown(net.minecraft.client.KeyMapping mapping) {
        InputConstants.Key key = mapping.getKey();
        return isRawKeyDown(key.getValue());
    }

    private static boolean isRawKeyDown(int keyCode) {
        com.mojang.blaze3d.platform.Window win = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(win, keyCode);
    }
}
