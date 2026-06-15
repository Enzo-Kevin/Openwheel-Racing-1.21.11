package com.openwheelracing.network;

import com.openwheelracing.content.entity.OpenwheelCarEntity;
import com.openwheelracing.OpenwheelRacing;
import com.openwheelracing.content.car.CarLivery;
import com.openwheelracing.content.car.PrototypeCarSetup;
import com.openwheelracing.content.item.PrototypeCarItem;
import com.openwheelracing.content.menu.CarAssemblyMenu;
import com.openwheelracing.content.track.TrackEditorMaterial;
import com.openwheelracing.content.track.TrackEditorMode;
import com.openwheelracing.content.track.TrackEditorOperation;
import com.openwheelracing.content.track.TrackEditorPlacementService;
import com.openwheelracing.content.track.TrackEditorPreset;
import com.openwheelracing.content.track.TrackEditorUndoStore;
import com.openwheelracing.registry.OWRDataComponents;
import com.openwheelracing.registry.OWRItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
        CHANNEL.messageBuilder(CycleLiveryMessage.class)
            .encoder(CycleLiveryMessage::encode)
            .decoder(CycleLiveryMessage::decode)
            .consumerMainThread(CycleLiveryMessage::handle)
            .add();
        CHANNEL.messageBuilder(ShiftMessage.class)
            .encoder(ShiftMessage::encode)
            .decoder(ShiftMessage::decode)
            .consumerMainThread(ShiftMessage::handle)
            .add();
        CHANNEL.messageBuilder(ExitCarMessage.class)
            .encoder(ExitCarMessage::encode)
            .decoder(ExitCarMessage::decode)
            .consumerMainThread(ExitCarMessage::handle)
            .add();
        CHANNEL.messageBuilder(DriveInputMessage.class)
            .encoder(DriveInputMessage::encode)
            .decoder(DriveInputMessage::decode)
            .consumerMainThread(DriveInputMessage::handle)
            .add();
        CHANNEL.messageBuilder(ToggleAbsMessage.class)
            .encoder(ToggleAbsMessage::encode)
            .decoder(ToggleAbsMessage::decode)
            .consumerMainThread(ToggleAbsMessage::handle)
            .add();
        CHANNEL.messageBuilder(MountCarMessage.class)
            .encoder(MountCarMessage::encode)
            .decoder(MountCarMessage::decode)
            .consumerMainThread(MountCarMessage::handle)
            .add();
        CHANNEL.messageBuilder(TrackEditorPlaceMessage.class)
            .encoder(TrackEditorPlaceMessage::encode)
            .decoder(TrackEditorPlaceMessage::decode)
            .consumerMainThread(TrackEditorPlaceMessage::handle)
            .add();
        CHANNEL.messageBuilder(TrackEditorUndoMessage.class)
            .encoder(TrackEditorUndoMessage::encode)
            .decoder(TrackEditorUndoMessage::decode)
            .consumerMainThread(TrackEditorUndoMessage::handle)
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

    public record CycleLiveryMessage(int delta) {
        private static void encode(CycleLiveryMessage message, FriendlyByteBuf buffer) {
            buffer.writeInt(message.delta);
        }

        private static CycleLiveryMessage decode(FriendlyByteBuf buffer) {
            return new CycleLiveryMessage(buffer.readInt());
        }

        private static void handle(CycleLiveryMessage message, CustomPayloadEvent.Context context) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null || !(player.containerMenu instanceof CarAssemblyMenu menu)) {
                    return;
                }
                ItemStack stack = menu.getOutputStack();
                if (!stack.is(OWRItems.PROTOTYPE_CAR_SPAWN.get())) {
                    return;
                }
                int current = PrototypeCarItem.getLivery(stack);
                int livery = CarLivery.wrapIndex(current + message.delta);
                stack.set(OWRDataComponents.CAR_LIVERY.get(), livery);
                PrototypeCarItem.applyLiveryItemDisplay(stack, livery);
                menu.slotsChanged(menu.getContainer());
            });
            context.setPacketHandled(true);
        }
    }

    public record ShiftMessage(int direction) {
        private static void encode(ShiftMessage message, FriendlyByteBuf buffer) {
            buffer.writeInt(message.direction);
        }

        private static ShiftMessage decode(FriendlyByteBuf buffer) {
            return new ShiftMessage(buffer.readInt());
        }

        private static void handle(ShiftMessage message, CustomPayloadEvent.Context context) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null || !(player.getVehicle() instanceof OpenwheelCarEntity car)) {
                    return;
                }
                if (message.direction > 0) {
                    car.shiftUp();
                } else {
                    car.shiftDown();
                }
            });
            context.setPacketHandled(true);
        }
    }

    public record ExitCarMessage() {
        private static void encode(ExitCarMessage message, FriendlyByteBuf buffer) {
        }

        private static ExitCarMessage decode(FriendlyByteBuf buffer) {
            return new ExitCarMessage();
        }

        private static void handle(ExitCarMessage message, CustomPayloadEvent.Context context) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player != null) {
                    player.stopRiding();
                }
            });
            context.setPacketHandled(true);
        }
    }

    public record DriveInputMessage(float throttle, float brake, float steering) {
        private static void encode(DriveInputMessage message, FriendlyByteBuf buffer) {
            buffer.writeFloat(message.throttle);
            buffer.writeFloat(message.brake);
            buffer.writeFloat(message.steering);
        }

        private static DriveInputMessage decode(FriendlyByteBuf buffer) {
            return new DriveInputMessage(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        }

        private static void handle(DriveInputMessage message, CustomPayloadEvent.Context context) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null || !(player.getVehicle() instanceof OpenwheelCarEntity car)) {
                    return;
                }
                car.applyDriveInput(message.throttle, message.brake, message.steering);
            });
            context.setPacketHandled(true);
        }
    }

    public record ToggleAbsMessage() {
        private static void encode(ToggleAbsMessage message, FriendlyByteBuf buffer) {
        }

        private static ToggleAbsMessage decode(FriendlyByteBuf buffer) {
            return new ToggleAbsMessage();
        }

        private static void handle(ToggleAbsMessage message, CustomPayloadEvent.Context context) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null || !(player.getVehicle() instanceof OpenwheelCarEntity car)) {
                    return;
                }
                car.toggleAbs();
            });
            context.setPacketHandled(true);
        }
    }

    public record MountCarMessage() {
        private static void encode(MountCarMessage message, FriendlyByteBuf buffer) {
        }

        private static MountCarMessage decode(FriendlyByteBuf buffer) {
            return new MountCarMessage();
        }

        private static void handle(MountCarMessage message, CustomPayloadEvent.Context context) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null || player.getVehicle() != null) {
                    return;
                }

                Vec3 eye = player.getEyePosition();
                Vec3 look = player.getLookAngle();
                Vec3 reach = eye.add(look.scale(5.0));
                AABB search = player.getBoundingBox().inflate(5.0);

                OpenwheelCarEntity best = null;
                double bestDistance = Double.MAX_VALUE;
                for (Entity entity : player.level().getEntities(player, search, e -> e instanceof OpenwheelCarEntity && e.getPassengers().isEmpty())) {
                    AABB box = entity.getBoundingBox().inflate(0.35);
                    if (box.clip(eye, reach).isPresent()) {
                        double distance = entity.distanceToSqr(player);
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            best = (OpenwheelCarEntity) entity;
                        }
                    }
                }

                if (best != null) {
                    player.startRiding(best);
                    best.prepareForDriver(player);
                }
            });
            context.setPacketHandled(true);
        }
    }

    public record TrackEditorPlaceMessage(TrackEditorOperation operation) {
        private static void encode(TrackEditorPlaceMessage message, FriendlyByteBuf buffer) {
            buffer.writeEnum(message.operation.mode());
            buffer.writeEnum(message.operation.material());
            buffer.writeVarInt(message.operation.width());
            buffer.writeEnum(message.operation.facing());
            buffer.writeEnum(message.operation.preset());
            buffer.writeEnum(message.operation.runoffMaterial());
            buffer.writeVarInt(message.operation.points().size());
            for (BlockPos point : message.operation.points()) {
                buffer.writeBlockPos(point);
            }
        }

        private static TrackEditorPlaceMessage decode(FriendlyByteBuf buffer) {
            TrackEditorMode mode = buffer.readEnum(TrackEditorMode.class);
            TrackEditorMaterial material = buffer.readEnum(TrackEditorMaterial.class);
            int width = buffer.readVarInt();
            Direction facing = buffer.readEnum(Direction.class);
            TrackEditorPreset preset = buffer.readEnum(TrackEditorPreset.class);
            TrackEditorMaterial runoffMaterial = buffer.readEnum(TrackEditorMaterial.class);
            int declaredSize = buffer.readVarInt();
            int size = Math.min(declaredSize, TrackEditorOperation.MAX_POINTS);
            java.util.List<BlockPos> points = new java.util.ArrayList<>(size);
            for (int i = 0; i < declaredSize; i++) {
                BlockPos point = buffer.readBlockPos();
                if (i < size) {
                    points.add(point);
                }
            }
            return new TrackEditorPlaceMessage(new TrackEditorOperation(mode, material, width, points, facing, preset, runoffMaterial));
        }

        private static void handle(TrackEditorPlaceMessage message, CustomPayloadEvent.Context context) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player != null) {
                    TrackEditorPlacementService.place(player, message.operation());
                }
            });
            context.setPacketHandled(true);
        }
    }

    public record TrackEditorUndoMessage() {
        private static void encode(TrackEditorUndoMessage message, FriendlyByteBuf buffer) {
        }

        private static TrackEditorUndoMessage decode(FriendlyByteBuf buffer) {
            return new TrackEditorUndoMessage();
        }

        private static void handle(TrackEditorUndoMessage message, CustomPayloadEvent.Context context) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player != null) {
                    TrackEditorUndoStore.undo(player);
                }
            });
            context.setPacketHandled(true);
        }
    }
}