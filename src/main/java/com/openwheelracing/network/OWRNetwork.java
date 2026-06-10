package com.openwheelracing.network;

import com.openwheelracing.OpenwheelRacing;
import com.openwheelracing.content.car.PrototypeCarSetup;
import com.openwheelracing.content.item.PrototypeCarItem;
import com.openwheelracing.content.menu.CarAssemblyMenu;
import com.openwheelracing.registry.OWRDataComponents;
import com.openwheelracing.registry.OWRItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;

public final class OWRNetwork {
    private static final int PROTOCOL = 1;
    public static final SimpleChannel CHANNEL = ChannelBuilder
        .named(Identifier.fromNamespaceAndPath(OpenwheelRacing.MODID, "main"))
        .networkProtocolVersion(PROTOCOL)
        .simpleChannel();

    private OWRNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(TuneCarMessage.class)
            .encoder(TuneCarMessage::encode)
            .decoder(TuneCarMessage::decode)
            .consumerMainThread(TuneCarMessage::handle)
            .add();
        CHANNEL.messageBuilder(RepairCarMessage.class)
            .encoder(RepairCarMessage::encode)
            .decoder(RepairCarMessage::decode)
            .consumerMainThread(RepairCarMessage::handle)
            .add();
    }

    public record TuneCarMessage(int slot, int delta) {
        private static void encode(TuneCarMessage message, FriendlyByteBuf buffer) {
            buffer.writeInt(message.slot);
            buffer.writeInt(message.delta);
        }

        private static TuneCarMessage decode(FriendlyByteBuf buffer) {
            return new TuneCarMessage(buffer.readInt(), buffer.readInt());
        }

        private static void handle(TuneCarMessage message, CustomPayloadEvent.Context context) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null || !(player.containerMenu instanceof CarAssemblyMenu menu)) {
                    return;
                }
                ItemStack stack = menu.getOutputStack();
                if (!stack.is(OWRItems.PROTOTYPE_CAR_SPAWN.get())) {
                    return;
                }
                PrototypeCarSetup setup = PrototypeCarItem.getSetup(stack);
                PrototypeCarSetup updated = switch (message.slot) {
                    case 0 -> new PrototypeCarSetup(setup.power() + message.delta, setup.grip(), setup.aero(), setup.gearing());
                    case 1 -> new PrototypeCarSetup(setup.power(), setup.grip() + message.delta, setup.aero(), setup.gearing());
                    case 2 -> new PrototypeCarSetup(setup.power(), setup.grip(), setup.aero() + message.delta, setup.gearing());
                    case 3 -> new PrototypeCarSetup(setup.power(), setup.grip(), setup.aero(), setup.gearing() + message.delta);
                    default -> setup;
                };
                stack.set(OWRDataComponents.CAR_SETUP.get(), updated);
                menu.slotsChanged(menu.getContainer());
            });
            context.setPacketHandled(true);
        }
    }

    public record RepairCarMessage() {
        private static void encode(RepairCarMessage message, FriendlyByteBuf buffer) {
        }

        private static RepairCarMessage decode(FriendlyByteBuf buffer) {
            return new RepairCarMessage();
        }

        private static void handle(RepairCarMessage message, CustomPayloadEvent.Context context) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null || !(player.containerMenu instanceof CarAssemblyMenu menu)) {
                    return;
                }
                ItemStack stack = menu.getOutputStack();
                if (!stack.is(OWRItems.PROTOTYPE_CAR_SPAWN.get())) {
                    return;
                }
                int damage = PrototypeCarItem.getDamage(stack);
                if (damage <= 0 || !player.getInventory().contains(new ItemStack(OWRItems.RUBBER.get()))) {
                    return;
                }
                player.getInventory().clearOrCountMatchingItems(item -> item.is(OWRItems.RUBBER.get()), 1, player.inventoryMenu.getCraftSlots());
                stack.set(OWRDataComponents.CAR_DAMAGE.get(), Math.max(0, damage - 25));
                menu.slotsChanged(menu.getContainer());
            });
            context.setPacketHandled(true);
        }
    }
}
