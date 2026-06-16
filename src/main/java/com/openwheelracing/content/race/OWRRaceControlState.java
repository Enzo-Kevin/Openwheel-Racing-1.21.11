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
        Codec.BOOL.optionalFieldOf("off_track_check_enabled", true).forGetter(OWRRaceControlState::isOffTrackCheckEnabled),
        Codec.INT.optionalFieldOf("minimum_valid_lap_ticks", OWRLapRecords.DEFAULT_MIN_VALID_LAP_TICKS).forGetter(OWRRaceControlState::getMinimumValidLapTicks)
    ).apply(instance, OWRRaceControlState::new));

    private static final SavedDataType<OWRRaceControlState> TYPE = new SavedDataType<>(
        OpenwheelRacing.MODID + "_race_control",
        OWRRaceControlState::new,
        CODEC,
        null
    );

    private boolean checkpointCheckEnabled;
    private boolean offTrackCheckEnabled;
    private int minimumValidLapTicks;
    private int revision;

    public OWRRaceControlState() {
        this(false, true, OWRLapRecords.DEFAULT_MIN_VALID_LAP_TICKS);
    }

    private OWRRaceControlState(boolean checkpointCheckEnabled, boolean offTrackCheckEnabled, int minimumValidLapTicks) {
        this.checkpointCheckEnabled = checkpointCheckEnabled;
        this.offTrackCheckEnabled = offTrackCheckEnabled;
        this.minimumValidLapTicks = Math.max(1, minimumValidLapTicks);
    }

    public static OWRRaceControlState get(ServerLevel level) {
        return level.getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(TYPE);
    }

    public int getRevision() {
        return revision;
    }

    public boolean isCheckpointCheckEnabled() {
        return checkpointCheckEnabled;
    }

    public boolean isOffTrackCheckEnabled() {
        return offTrackCheckEnabled;
    }

    public int getMinimumValidLapTicks() {
        return minimumValidLapTicks;
    }

    public boolean toggleCheckpointCheck() {
        checkpointCheckEnabled = !checkpointCheckEnabled;
        markChanged();
        return checkpointCheckEnabled;
    }

    public boolean toggleOffTrackCheck() {
        offTrackCheckEnabled = !offTrackCheckEnabled;
        markChanged();
        return offTrackCheckEnabled;
    }

    public void setMinimumValidLapTicks(int ticks) {
        int clamped = Math.max(1, ticks);
        if (minimumValidLapTicks == clamped) {
            return;
        }
        minimumValidLapTicks = clamped;
        markChanged();
    }

    private void markChanged() {
        revision++;
        setDirty();
    }
}
