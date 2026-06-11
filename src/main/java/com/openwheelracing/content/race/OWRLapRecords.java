package com.openwheelracing.content.race;

import com.mojang.serialization.Codec;
import com.openwheelracing.OpenwheelRacing;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class OWRLapRecords extends SavedData {
    private static final Codec<OWRLapRecords> CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT)
        .fieldOf("player_best_laps")
        .codec()
        .xmap(OWRLapRecords::new, OWRLapRecords::pack);

    private static final SavedDataType<OWRLapRecords> TYPE = new SavedDataType<>(
        OpenwheelRacing.MODID + "_lap_records",
        OWRLapRecords::new,
        CODEC,
        null
    );

    private final Map<UUID, Integer> playerBestLaps = new HashMap<>();

    public OWRLapRecords() {
    }

    private OWRLapRecords(Map<String, Integer> packed) {
        packed.forEach((key, ticks) -> {
            try {
                if (ticks > 0) {
                    playerBestLaps.put(UUID.fromString(key), ticks);
                }
            } catch (IllegalArgumentException ignored) {
            }
        });
    }

    public static OWRLapRecords get(ServerLevel level) {
        return level.getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(TYPE);
    }

    public int getBestLap(UUID playerId) {
        return playerBestLaps.getOrDefault(playerId, 0);
    }

    public boolean setBestLapIfBetter(UUID playerId, int ticks) {
        if (ticks <= 0) {
            return false;
        }
        int previous = getBestLap(playerId);
        if (previous != 0 && ticks >= previous) {
            return false;
        }
        playerBestLaps.put(playerId, ticks);
        setDirty();
        return true;
    }

    private Map<String, Integer> pack() {
        Map<String, Integer> packed = new HashMap<>();
        playerBestLaps.forEach((playerId, ticks) -> packed.put(playerId.toString(), ticks));
        return packed;
    }
}
