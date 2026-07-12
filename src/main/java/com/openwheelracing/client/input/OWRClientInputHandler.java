package com.openwheelracing.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.openwheelracing.client.screen.TrackEditorScreen;
import com.openwheelracing.content.entity.OpenwheelCarEntity;
import com.openwheelracing.network.OWRNetwork;
import com.openwheelracing.registry.OWRSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.network.PacketDistributor;

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

        TrackEditorScreen.preloadAroundPlayer(mc);

        while (OWRKeyMappings.TRACK_EDITOR.consumeClick()) {
            mc.setScreen(new TrackEditorScreen());
            return;
        }

        WheelInputManager.Output wheel = WheelInputManager.poll(WheelInputSettings.get());
        while (!(mc.player.getVehicle() instanceof OpenwheelCarEntity) && OWRKeyMappings.MOUNT_CAR.consumeClick()) {
            OWRNetwork.CHANNEL.send(new OWRNetwork.MountCarMessage(), PacketDistributor.SERVER.noArg());
        }
        if (!(mc.player.getVehicle() instanceof OpenwheelCarEntity) && wheel.pressed(WheelInputSettings.ButtonRole.MOUNT_CAR)) {
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
        throttle = Math.max(throttle, wheel.throttle());
        brake = Math.max(brake, wheel.brake());
        if (Math.abs(wheel.steering()) > 0.0f) {
            steering = wheel.steering();
        }
        car.tickLocalClientMovement(throttle, brake, steering);
        OWRNetwork.CHANNEL.send(new OWRNetwork.DriveInputMessage(throttle, brake, steering), PacketDistributor.SERVER.noArg());

        boolean shiftUpDown = isDown(OWRKeyMappings.SHIFT_UP) || wheel.pressed(WheelInputSettings.ButtonRole.SHIFT_UP);
        boolean shiftDownDown = isDown(OWRKeyMappings.SHIFT_DOWN) || wheel.pressed(WheelInputSettings.ButtonRole.SHIFT_DOWN);
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
        if (wheel.pressed(WheelInputSettings.ButtonRole.EXIT_CAR)) {
            OWRNetwork.CHANNEL.send(new OWRNetwork.ExitCarMessage(), PacketDistributor.SERVER.noArg());
        }
        while (OWRKeyMappings.TOGGLE_ABS.consumeClick()) {
            car.toggleAbs();
            OWRNetwork.CHANNEL.send(new OWRNetwork.ToggleAbsMessage(), PacketDistributor.SERVER.noArg());
        }
        if (wheel.pressed(WheelInputSettings.ButtonRole.TOGGLE_ABS)) {
            car.toggleAbs();
            OWRNetwork.CHANNEL.send(new OWRNetwork.ToggleAbsMessage(), PacketDistributor.SERVER.noArg());
        }
        while (OWRKeyMappings.TOGGLE_TC.consumeClick()) {
            car.toggleTractionControl();
            OWRNetwork.CHANNEL.send(new OWRNetwork.ToggleTractionControlMessage(), PacketDistributor.SERVER.noArg());
        }
        if (wheel.pressed(WheelInputSettings.ButtonRole.TOGGLE_TC)) {
            car.toggleTractionControl();
            OWRNetwork.CHANNEL.send(new OWRNetwork.ToggleTractionControlMessage(), PacketDistributor.SERVER.noArg());
        }
        while (OWRKeyMappings.TOGGLE_DRS.consumeClick()) {
            car.toggleDrs();
            OWRNetwork.CHANNEL.send(new OWRNetwork.ToggleDrsMessage(), PacketDistributor.SERVER.noArg());
            mc.player.playSound(OWRSoundEvents.DRS_BEEP.get(), 1.0f, 1.0f);
        }
        if (wheel.pressed(WheelInputSettings.ButtonRole.TOGGLE_DRS)) {
            car.toggleDrs();
            OWRNetwork.CHANNEL.send(new OWRNetwork.ToggleDrsMessage(), PacketDistributor.SERVER.noArg());
            mc.player.playSound(OWRSoundEvents.DRS_BEEP.get(), 1.0f, 1.0f);
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
