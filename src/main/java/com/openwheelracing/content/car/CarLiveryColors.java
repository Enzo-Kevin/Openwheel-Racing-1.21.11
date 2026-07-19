package com.openwheelracing.content.car;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CarLiveryColors(int body, int accent1, int accent2) {
    public static final CarLiveryColors DEFAULT = new CarLiveryColors(rgb(235, 18, 32), rgb(0, 92, 255), rgb(255, 215, 0));

    public static final Codec<CarLiveryColors> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("body").forGetter(CarLiveryColors::body),
        Codec.INT.fieldOf("accent1").forGetter(CarLiveryColors::accent1),
        Codec.INT.fieldOf("accent2").forGetter(CarLiveryColors::accent2)
    ).apply(instance, CarLiveryColors::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CarLiveryColors> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, CarLiveryColors::body,
        ByteBufCodecs.INT, CarLiveryColors::accent1,
        ByteBufCodecs.INT, CarLiveryColors::accent2,
        CarLiveryColors::new
    );

    public CarLiveryColors {
        body = normalize(body);
        accent1 = normalize(accent1);
        accent2 = normalize(accent2);
    }

    public CarLiveryColors withChannel(int channel, int color) {
        return switch (channel) {
            case 0 -> new CarLiveryColors(color, accent1, accent2);
            case 1 -> new CarLiveryColors(body, color, accent2);
            case 2 -> new CarLiveryColors(body, accent1, color);
            default -> this;
        };
    }

    public int channel(int channel) {
        return switch (channel) {
            case 0 -> body;
            case 1 -> accent1;
            case 2 -> accent2;
            default -> body;
        };
    }

    public int bodyTop() {
        return body;
    }

    public int bodySide() {
        return body;
    }

    public int bodyBottom() {
        return body;
    }

    public int accent1Top() {
        return accent1;
    }

    public int accent1Side() {
        return accent1;
    }

    public int accent1Bottom() {
        return accent1;
    }

    public int accent2Top() {
        return accent2;
    }

    public int accent2Side() {
        return accent2;
    }

    public int accent2Bottom() {
        return accent2;
    }

    public static CarLiveryColors fromPreset(CarLivery livery) {
        return new CarLiveryColors(livery.bodySide(), livery.accent1Side(), livery.accent2Side());
    }

    public static String colorName(int color) {
        return String.format("#%06X", normalize(color) & 0xFFFFFF);
    }

    public static int rgb(int r, int g, int b) {
        return 0xFF000000 | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    public static int red(int color) {
        return (color >> 16) & 255;
    }

    public static int green(int color) {
        return (color >> 8) & 255;
    }

    public static int blue(int color) {
        return color & 255;
    }

    private static int normalize(int color) {
        return 0xFF000000 | (color & 0xFFFFFF);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
