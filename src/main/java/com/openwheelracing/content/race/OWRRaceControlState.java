package com.openwheelracing.content.race;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.openwheelracing.OpenwheelRacing;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class OWRRaceControlState extends SavedData {
    private static final Codec<OWRRaceControlState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.optionalFieldOf("checkpoint_check_enabled", false).forGetter(OWRRaceControlState::isCheckpointCheckEnabled),
        Codec.BOOL.optionalFieldOf("off_track_check_enabled", true).forGetter(OWRRaceControlState::isOffTrackCheckEnabled)
    ).apply(instance, OWRRaceControlState::new));

    private static final SavedDataType<OWRRaceControlState> TYPE = new SavedDataType<>(
        OpenwheelRacing.MODID + "_race_control",
        OWRRaceControlState::new,
        CODEC,
        null
    );

    private boolean checkpointCheckEnabled;
    private boolean offTrackCheckEnabled;

    public OWRRaceControlState() {
        this(false, true);
    }

    private OWRRaceControlState(boolean checkpointCheckEnabled, boolean offTrackCheckEnabled) {
        this.checkpointCheckEnabled = checkpointCheckEnabled;
        this.offTrackCheckEnabled = offTrackCheckEnabled;
    }

    public static OWRRaceControlState get(ServerLevel level) {
        return level.getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isCheckpointCheckEnabled() {
        return checkpointCheckEnabled;
    }

    public boolean isOffTrackCheckEnabled() {
        return offTrackCheckEnabled;
    }

    public boolean toggleCheckpointCheck() {
        checkpointCheckEnabled = !checkpointCheckEnabled;
        setDirty();
        return checkpointCheckEnabled;
    }

    public boolean toggleOffTrackCheck() {
        offTrackCheckEnabled = !offTrackCheckEnabled;
        setDirty();
        return offTrackCheckEnabled;
    }
}
