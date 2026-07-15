package com.openwheelracing.content.car;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PrototypeCarSetup(int power, int grip, int aero, int gearing) {
    public static final PrototypeCarSetup DEFAULT = new PrototypeCarSetup(1, 3, 2, 1);

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
        power = clamp(power, 3);
        grip = clamp(grip, 4);
        aero = clamp(aero, 4);
        gearing = clamp(gearing, 2);
    }

    public double powerMultiplier() {
        return 0.70 + power * 0.20;
    }

    public double gripMultiplier() {
        return tyreMuCoefficient();
    }

    public double tyreMuCoefficient() {
        return 1.07 + (grip - DEFAULT.grip()) * 0.07;
    }

    public double aeroMultiplier() {
        return clACoefficient();
    }

    public double clACoefficient() {
        return 1.0 + (aero - DEFAULT.aero()) * 0.08;
    }

    public double dragMultiplier() {
        return cdACoefficient();
    }

    public double cdACoefficient() {
        return 1.0 + (aero - DEFAULT.aero()) * 0.045;
    }

    public double gearingMultiplier() {
        return topSpeedCoefficient();
    }

    public double topSpeedCoefficient() {
        return 1.0 / gearRatioCoefficient();
    }

    public double accelerationMultiplier() {
        return gearRatioCoefficient();
    }

    public double gearRatioCoefficient() {
        return 1.0 + (DEFAULT.gearing() - gearing) * 0.10;
    }

    public double fuelUseMultiplier() {
        return 0.75 + power * 0.25;
    }

    public double tyreWearMultiplier() {
        return 0.93 + (grip - DEFAULT.grip()) * 0.18;
    }

    private static int clamp(int value, int max) {
        return Math.max(0, Math.min(max, value));
    }
}
