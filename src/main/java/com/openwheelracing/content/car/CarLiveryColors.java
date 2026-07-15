package com.openwheelracing.content.car;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CarLiveryColors(int body, int accent1, int accent2) {
    public static final int COLOR_COUNT = 10;
    public static final CarLiveryColors DEFAULT = new CarLiveryColors(0, 1, 2);

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
        body = wrap(body);
        accent1 = wrap(accent1);
        accent2 = wrap(accent2);
    }

    public CarLiveryColors cycle(int channel, int delta) {
        return switch (channel) {
            case 0 -> new CarLiveryColors(body + delta, accent1, accent2);
            case 1 -> new CarLiveryColors(body, accent1 + delta, accent2);
            case 2 -> new CarLiveryColors(body, accent1, accent2 + delta);
            default -> this;
        };
    }

    public int bodyTop() {
        return top(body);
    }

    public int bodySide() {
        return side(body);
    }

    public int bodyBottom() {
        return bottom(body);
    }

    public int accent1Top() {
        return top(accent1);
    }

    public int accent1Side() {
        return side(accent1);
    }

    public int accent1Bottom() {
        return bottom(accent1);
    }

    public int accent2Top() {
        return top(accent2);
    }

    public int accent2Side() {
        return side(accent2);
    }

    public int accent2Bottom() {
        return bottom(accent2);
    }

    public static CarLiveryColors fromPreset(CarLivery livery) {
        return new CarLiveryColors(nearest(livery.bodySide()), nearest(livery.accent1Side()), nearest(livery.accent2Side()));
    }

    public static String colorName(int index) {
        return switch (wrap(index)) {
            case 0 -> "Red";
            case 1 -> "Blue";
            case 2 -> "Yellow";
            case 3 -> "Orange";
            case 4 -> "Green";
            case 5 -> "Cyan";
            case 6 -> "Pink";
            case 7 -> "Purple";
            case 8 -> "White";
            case 9 -> "Silver";
            default -> "Red";
        };
    }

    private static int top(int index) {
        return scale(side(index), 1.12);
    }

    private static int side(int index) {
        return switch (wrap(index)) {
            case 0 -> rgb(235, 18, 32);
            case 1 -> rgb(0, 92, 255);
            case 2 -> rgb(255, 215, 0);
            case 3 -> rgb(255, 112, 0);
            case 4 -> rgb(0, 184, 82);
            case 5 -> rgb(0, 222, 225);
            case 6 -> rgb(255, 55, 145);
            case 7 -> rgb(136, 72, 255);
            case 8 -> rgb(245, 245, 245);
            case 9 -> rgb(185, 190, 198);
            default -> rgb(235, 18, 32);
        };
    }

    private static int bottom(int index) {
        return scale(side(index), 0.60);
    }

    private static int nearest(int rgb) {
        int nearest = 0;
        int nearestDistance = Integer.MAX_VALUE;
        int r = (rgb >> 16) & 255;
        int g = (rgb >> 8) & 255;
        int b = rgb & 255;
        for (int i = 0; i < COLOR_COUNT; i++) {
            int color = side(i);
            int cr = (color >> 16) & 255;
            int cg = (color >> 8) & 255;
            int cb = color & 255;
            int distance = square(r - cr) + square(g - cg) + square(b - cb);
            if (distance < nearestDistance) {
                nearest = i;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private static int wrap(int index) {
        return (index % COLOR_COUNT + COLOR_COUNT) % COLOR_COUNT;
    }

    private static int scale(int rgb, double scale) {
        int r = Math.max(0, Math.min(255, (int) Math.round(((rgb >> 16) & 255) * scale)));
        int g = Math.max(0, Math.min(255, (int) Math.round(((rgb >> 8) & 255) * scale)));
        int b = Math.max(0, Math.min(255, (int) Math.round((rgb & 255) * scale)));
        return rgb(r, g, b);
    }

    private static int square(int value) {
        return value * value;
    }

    private static int rgb(int r, int g, int b) {
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
