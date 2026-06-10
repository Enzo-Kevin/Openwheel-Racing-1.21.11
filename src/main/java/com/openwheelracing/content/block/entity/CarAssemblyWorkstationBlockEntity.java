package com.openwheelracing.content.block.entity;

import com.openwheelracing.content.item.PrototypeCarItem;
import com.openwheelracing.content.menu.CarAssemblyMenu;
import com.openwheelracing.registry.OWRBlockEntities;
import com.openwheelracing.registry.OWRItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class CarAssemblyWorkstationBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int SLOT_CHASSIS = 0;
    public static final int SLOT_ENGINE = 1;
    public static final int SLOT_TIRES = 2;
    public static final int SLOT_AERO_KIT = 3;
    public static final int SLOT_GEARBOX = 4;
    public static final int SLOT_STEERING_CONTROLS = 5;
    public static final int SLOT_OUTPUT = 6;
    public static final int SLOT_COUNT = 7;
    private static final int MAX_PROGRESS = 100;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int progress;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> MAX_PROGRESS;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                progress = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public CarAssemblyWorkstationBlockEntity(BlockPos pos, BlockState state) {
        super(OWRBlockEntities.CAR_ASSEMBLY_WORKSTATION.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CarAssemblyWorkstationBlockEntity workstation) {
        if (workstation.canAssemble()) {
            workstation.progress++;
            if (workstation.progress >= MAX_PROGRESS) {
                workstation.assembleCar();
                workstation.progress = 0;
            }
        } else {
            workstation.progress = 0;
        }
        workstation.setChanged();
    }

    public ContainerData getData() {
        return data;
    }

    public boolean isValidForSlot(int slot, ItemStack stack) {
        return slot != SLOT_OUTPUT && stack.is(requiredItemForSlot(slot));
    }

    private boolean canAssemble() {
        for (int slot = SLOT_CHASSIS; slot <= SLOT_STEERING_CONTROLS; slot++) {
            ItemStack stack = getItem(slot);
            if (stack.isEmpty() || !stack.is(requiredItemForSlot(slot))) {
                return false;
            }
        }

        ItemStack output = getItem(SLOT_OUTPUT);
        return output.isEmpty() || output.is(OWRItems.PROTOTYPE_CAR_SPAWN.get()) && output.getCount() < output.getMaxStackSize();
    }

    private void assembleCar() {
        for (int slot = SLOT_CHASSIS; slot <= SLOT_STEERING_CONTROLS; slot++) {
            removeItem(slot, 1);
        }

        ItemStack output = getItem(SLOT_OUTPUT);
        if (output.isEmpty()) {
            setItem(SLOT_OUTPUT, PrototypeCarItem.createWithDefaultSetup());
        } else {
            output.grow(1);
        }
    }

    private Item requiredItemForSlot(int slot) {
        return switch (slot) {
            case SLOT_CHASSIS -> OWRItems.CHASSIS.get();
            case SLOT_ENGINE -> OWRItems.ENGINE.get();
            case SLOT_TIRES -> OWRItems.TIRES.get();
            case SLOT_AERO_KIT -> OWRItems.AERO_KIT.get();
            case SLOT_GEARBOX -> OWRItems.GEARBOX.get();
            case SLOT_STEERING_CONTROLS -> OWRItems.STEERING_CONTROLS.get();
            default -> OWRItems.PROTOTYPE_CAR_SPAWN.get();
        };
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
        if (!stack.isEmpty()) {
            setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.openwheelracing.car_assembly_workstation");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CarAssemblyMenu(containerId, playerInventory, this, data);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, items);
        progress = input.getIntOr("Progress", 0);
    }
}
