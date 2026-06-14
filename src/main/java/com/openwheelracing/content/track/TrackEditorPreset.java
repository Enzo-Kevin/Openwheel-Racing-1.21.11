package com.openwheelracing.content.track;

import net.minecraft.network.chat.Component;

public enum TrackEditorPreset {
    BLANK("screen.openwheelracing.track_editor.preset.blank"),
    STREET("screen.openwheelracing.track_editor.preset.street"),
    HALF_STREET("screen.openwheelracing.track_editor.preset.half_street"),
    FULL_CIRCUIT("screen.openwheelracing.track_editor.preset.full_circuit");

    private final String translationKey;

    TrackEditorPreset(String translationKey) {
        this.translationKey = translationKey;
    }

    public Component displayName() {
        return Component.translatable(translationKey);
    }
}
