package com.openwheelracing.content.car;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PrototypeCarSetup(int power, int grip, int aero, int gearing) {
    public static final PrototypeCarSetup DEFAULT = new PrototypeCarSetup(5, 5, 5, 5);

    public static final Codec<PrototypeCarSetup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("power").forGetter(PrototypeCarSetup::power),
        Codec.INT.fieldOf("grip").forGetter(PrototypeCarSetup::grip),
        Codec.INT.fieldOf("aero").forGetter(PrototypeCarSetup::aero),
        Codec.INT.fieldOf("gearing").forGetter(PrototypeCarSetup::gearing)
    ).apply(instance, PrototypeCarSetup::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PrototypeCarSetup> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, PrototypeCarSetup::power,
        ByteBufCodecs.INT, PrototypeCarSetup::grip,
        ByteBufCodecs.INT, PrototypeCarSetup::aero,
        ByteBufCodecs.INT, PrototypeCarSetup::gearing,
        PrototypeCarSetup::new
    );

    public PrototypeCarSetup {
        power = clamp(power);
        grip = clamp(grip);
        aero = clamp(aero);
        gearing = clamp(gearing);
    }

    public double powerMultiplier() {
        return 0.75 + power * 0.06;
    }

    public double gripMultiplier() {
        return 0.75 + grip * 0.06;
    }

    public double aeroMultiplier() {
        return 0.85 + aero * 0.04;
    }

    public double gearingMultiplier() {
        return 0.80 + gearing * 0.05;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(10, value));
    }
}
