package com.openwheelracing.content.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BrakingDistanceTest {
    @Test
    void pitLimiterUsesEightyKmhConversion() {
        assertEquals(80.0 / 72.0, VehiclePhysics.PIT_SPEED_LIMIT_BLOCKS_PER_TICK, 1.0E-12);
        assertEquals(VehiclePhysics.PIT_SPEED_LIMIT_BLOCKS_PER_TICK, VehiclePhysics.speedKmhToBlocksPerTick(VehiclePhysics.PIT_SPEED_LIMIT_KMH), 1.0E-12);
    }

    @Test
    void pitLaneUsesAsphaltGripAndDrag() {
        assertEquals(VehiclePhysics.ASPHALT_GRIP, VehiclePhysics.PIT_LANE_GRIP, 1.0E-12);
        assertEquals(VehiclePhysics.ASPHALT_DRAG, VehiclePhysics.PIT_LANE_DRAG, 1.0E-12);
    }

    @Test
    void pitLaneBrakingDistanceMatchesAsphalt() {
        double speedMetersPerSecond = VehiclePhysics.PIT_SPEED_LIMIT_KMH / 3.6;
        double asphaltDistance = VehiclePhysics.brakingDistanceMeters(speedMetersPerSecond, VehiclePhysics.ASPHALT_GRIP);
        double pitLaneDistance = VehiclePhysics.brakingDistanceMeters(speedMetersPerSecond, VehiclePhysics.PIT_LANE_GRIP);

        assertEquals(asphaltDistance, pitLaneDistance, 1.0E-12);
        assertTrue(pitLaneDistance < 12.0);
    }
}
