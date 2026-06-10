package com.openwheelracing.content.menu;

import com.openwheelracing.content.block.entity.CarAssemblyWorkstationBlockEntity;
import com.openwheelracing.registry.OWRBlocks;
import com.openwheelracing.registry.OWRMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CarAssemblyMenu extends AbstractContainerMenu {
    private static final int WORKSTATION_SLOT_COUNT = CarAssemblyWorkstationBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = WORKSTATION_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public CarAssemblyMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(WORKSTATION_SLOT_COUNT), new SimpleContainerData(2));
    }

    public CarAssemblyMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(OWRMenus.CAR_ASSEMBLY.get(), containerId);
        checkContainerSize(container, WORKSTATION_SLOT_COUNT);
        checkContainerDataCount(data, 2);
        this.container = container;
        this.data = data;
        this.access = container instanceof CarAssemblyWorkstationBlockEntity workstation && workstation.getLevel() != null
            ? ContainerLevelAccess.create(workstation.getLevel(), workstation.getBlockPos())
            : ContainerLevelAccess.NULL;

        addWorkstationSlots(container);
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    public int getScaledProgress() {
        int progress = data.get(0);
        int maxProgress = data.get(1);
        return maxProgress == 0 || progress == 0 ? 0 : progress * 24 / maxProgress;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if (index == CarAssemblyWorkstationBlockEntity.SLOT_OUTPUT) {
                if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, result);
            } else if (index < WORKSTATION_SLOT_COUNT) {
                if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveToMatchingInput(stack)) {
                if (index < PLAYER_INVENTORY_END) {
                    if (!moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
        }

        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, OWRBlocks.CAR_ASSEMBLY_WORKSTATION.get());
    }

    private void addWorkstationSlots(Container container) {
        addSlot(new ComponentSlot(container, CarAssemblyWorkstationBlockEntity.SLOT_CHASSIS, 35, 17));
        addSlot(new ComponentSlot(container, CarAssemblyWorkstationBlockEntity.SLOT_ENGINE, 53, 17));
        addSlot(new ComponentSlot(container, CarAssemblyWorkstationBlockEntity.SLOT_TIRES, 71, 17));
        addSlot(new ComponentSlot(container, CarAssemblyWorkstationBlockEntity.SLOT_AERO_KIT, 35, 35));
        addSlot(new ComponentSlot(container, CarAssemblyWorkstationBlockEntity.SLOT_GEARBOX, 53, 35));
        addSlot(new ComponentSlot(container, CarAssemblyWorkstationBlockEntity.SLOT_STEERING_CONTROLS, 71, 35));
        addSlot(new OutputSlot(container, CarAssemblyWorkstationBlockEntity.SLOT_OUTPUT, 125, 26));
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
    }

    private boolean moveToMatchingInput(ItemStack stack) {
        for (int slot = 0; slot <= CarAssemblyWorkstationBlockEntity.SLOT_STEERING_CONTROLS; slot++) {
            Slot inputSlot = slots.get(slot);
            if (inputSlot.mayPlace(stack) && moveItemStackTo(stack, slot, slot + 1, false)) {
                return true;
            }
        }
        return false;
    }

    private static class ComponentSlot extends Slot {
        ComponentSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return container instanceof CarAssemblyWorkstationBlockEntity workstation && workstation.isValidForSlot(getSlotIndex(), stack);
        }
    }

    private static class OutputSlot extends Slot {
        OutputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
