package com.openwheelracing.client.input;

import com.openwheelracing.content.entity.OpenwheelCarEntity;
import com.openwheelracing.network.OWRNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.network.PacketDistributor;

public final class OWRClientInputHandler {
    private OWRClientInputHandler() {
    }

    public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        while (!(mc.player.getVehicle() instanceof OpenwheelCarEntity) && OWRKeyMappings.MOUNT_CAR.consumeClick()) {
            OWRNetwork.CHANNEL.send(new OWRNetwork.MountCarMessage(), PacketDistributor.SERVER.noArg());
        }

        if (!(mc.player.getVehicle() instanceof OpenwheelCarEntity)) {
            return;
        }

        Options opts = mc.options;
        // Read directly from held key states — player.zza/xxa are zeroed for passengers
        float throttle = opts.keyUp.isDown() ? 1.0f : 0.0f;
        float brake    = opts.keyDown.isDown() ? 1.0f : 0.0f;
        float steering = (opts.keyRight.isDown() ? 1.0f : 0.0f) - (opts.keyLeft.isDown() ? 1.0f : 0.0f);
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
}
