package com.openwheelracing.registry;

import com.openwheelracing.OpenwheelRacing;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class OWRSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, OpenwheelRacing.MODID);

    public static final RegistryObject<SoundEvent> CAR_ENGINE_LOW = registerVariable("car.engine.low");
    public static final RegistryObject<SoundEvent> CAR_ENGINE_HIGH = registerVariable("car.engine.high");
    public static final RegistryObject<SoundEvent> CAR_TYRE_SQUEAL = registerVariable("car.tyre_squeal");

    private OWRSoundEvents() {
    }

    public static void register(BusGroup modBusGroup) {
        SOUND_EVENTS.register(modBusGroup);
    }

    private static RegistryObject<SoundEvent> registerVariable(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(OpenwheelRacing.MODID, name)));
    }
}
