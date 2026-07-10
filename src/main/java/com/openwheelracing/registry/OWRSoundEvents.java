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

    public static final RegistryObject<SoundEvent> CAR_ENGINE_FERRARI_LOW  = registerVariable("car.engine.ferrari.low");
    public static final RegistryObject<SoundEvent> CAR_ENGINE_FERRARI_HIGH = registerVariable("car.engine.ferrari.high");
    public static final RegistryObject<SoundEvent> CAR_ENGINE_RENAULT_LOW  = registerVariable("car.engine.renault.low");
    public static final RegistryObject<SoundEvent> CAR_ENGINE_RENAULT_HIGH = registerVariable("car.engine.renault.high");
    public static final RegistryObject<SoundEvent> CAR_ENGINE_MERCEDES_LOW  = registerVariable("car.engine.mercedes.low");
    public static final RegistryObject<SoundEvent> CAR_ENGINE_MERCEDES_HIGH = registerVariable("car.engine.mercedes.high");
    public static final RegistryObject<SoundEvent> CAR_ENGINE_RBPT_LOW  = registerVariable("car.engine.rbpt.low");
    public static final RegistryObject<SoundEvent> CAR_ENGINE_RBPT_HIGH = registerVariable("car.engine.rbpt.high");
    public static final RegistryObject<SoundEvent> CAR_TYRE_SQUEAL = registerVariable("car.tyre_squeal");
    public static final RegistryObject<SoundEvent> DRS_BEEP = registerVariable("car.drs_beep");

    private OWRSoundEvents() {
    }

    public static void register(BusGroup modBusGroup) {
        SOUND_EVENTS.register(modBusGroup);
    }

    private static RegistryObject<SoundEvent> registerVariable(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(OpenwheelRacing.MODID, name)));
    }
}
