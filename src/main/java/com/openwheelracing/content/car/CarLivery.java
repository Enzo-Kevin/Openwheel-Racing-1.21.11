package com.openwheelracing.content.car;

public enum CarLivery {
    RED_BULL("Red Bull", palette(10, 26, 92), palette(204, 30, 30), palette(255, 215, 0)),
    FERRARI("Ferrari", palette(220, 0, 0), palette(245, 245, 245), palette(255, 215, 0)),
    MERCEDES("Mercedes", palette(39, 244, 210), palette(26, 26, 26), palette(192, 192, 192)),
    MCLAREN("McLaren", palette(255, 128, 0), palette(26, 26, 26), palette(232, 96, 0)),
    ASTON_MARTIN("Aston Martin", palette(0, 95, 63), palette(151, 192, 61), palette(255, 105, 180)),
    ALPINE("Alpine", palette(0, 51, 160), palette(255, 64, 129), palette(204, 0, 0)),
    WILLIAMS("Williams", palette(0, 90, 255), palette(240, 240, 240), palette(255, 215, 0)),
    VCARB("VCARB", palette(26, 31, 110), palette(240, 240, 240), palette(204, 30, 30)),
    HAAS("Haas", palette(245, 245, 245), palette(204, 30, 30), palette(51, 51, 51)),
    KICK_SAUBER("Kick Sauber", palette(26, 42, 26), palette(0, 230, 118), palette(102, 102, 102));

    private final String displayName;
    private final int bodyTop;
    private final int bodySide;
    private final int bodyBottom;
    private final int accent1Top;
    private final int accent1Side;
    private final int accent1Bottom;
    private final int accent2Top;
    private final int accent2Side;
    private final int accent2Bottom;

    CarLivery(String displayName, int[] body, int[] accent1, int[] accent2) {
        this.displayName = displayName;
        this.bodyTop = body[0];
        this.bodySide = body[1];
        this.bodyBottom = body[2];
        this.accent1Top = accent1[0];
        this.accent1Side = accent1[1];
        this.accent1Bottom = accent1[2];
        this.accent2Top = accent2[0];
        this.accent2Side = accent2[1];
        this.accent2Bottom = accent2[2];
    }

    public String displayName() {
        return displayName;
    }

    public int bodyTop() {
        return bodyTop;
    }

    public int bodySide() {
        return bodySide;
    }

    public int bodyBottom() {
        return bodyBottom;
    }

    public int accent1Top() {
        return accent1Top;
    }

    public int accent1Side() {
        return accent1Side;
    }

    public int accent1Bottom() {
        return accent1Bottom;
    }

    public int accent2Top() {
        return accent2Top;
    }

    public int accent2Side() {
        return accent2Side;
    }

    public int accent2Bottom() {
        return accent2Bottom;
    }

    public static CarLivery fromIndex(int index) {
        CarLivery[] liveries = values();
        return liveries[Math.max(0, Math.min(liveries.length - 1, index))];
    }

    public static int wrapIndex(int index) {
        int count = count();
        return (index % count + count) % count;
    }

    public static int count() {
        return values().length;
    }

    private static int[] palette(int r, int g, int b) {
        return new int[] {rgb(scale(r, 1.15), scale(g, 1.15), scale(b, 1.15)), rgb(r, g, b), rgb(scale(r, 0.55), scale(g, 0.55), scale(b, 0.55))};
    }

    private static int scale(int value, double scale) {
        return Math.max(0, Math.min(255, (int) Math.round(value * scale)));
    }

    private static int rgb(int r, int g, int b) {
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
