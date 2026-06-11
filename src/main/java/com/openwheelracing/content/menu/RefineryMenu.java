package com.openwheelracing.content.menu;

import com.openwheelracing.content.block.entity.RefineryBlockEntity;
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

public class RefineryMenu extends AbstractContainerMenu {
    private static final int REFINERY_SLOT_COUNT = RefineryBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = REFINERY_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final ContainerData data;
    private final ContainerLevelAccess access;

    public RefineryMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(REFINERY_SLOT_COUNT), new SimpleContainerData(4));
    }

    public RefineryMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(OWRMenus.REFINERY.get(), containerId);
        checkContainerSize(container, REFINERY_SLOT_COUNT);
        checkContainerDataCount(data, 4);
        this.data = data;
        this.access = container instanceof RefineryBlockEntity refinery && refinery.getLevel() != null
            ? ContainerLevelAccess.create(refinery.getLevel(), refinery.getBlockPos())
            : ContainerLevelAccess.NULL;

        addSlot(new InputSlot(container, RefineryBlockEntity.SLOT_CRUDE, 35, 22));
        addSlot(new InputSlot(container, RefineryBlockEntity.SLOT_FUEL, 35, 62));
        addSlot(new OutputSlot(container, 2, 126, 8));
        addSlot(new OutputSlot(container, 3, 126, 27));
        addSlot(new OutputSlot(container, 4, 126, 46));
        addSlot(new OutputSlot(container, 5, 126, 65));
        addSlot(new OutputSlot(container, 6, 126, 84));
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    public int getScaledProgress() {
        int progress = data.get(0);
        int maxProgress = data.get(1);
        return maxProgress == 0 || progress == 0 ? 0 : progress * 24 / maxProgress;
    }

    public int getScaledBurn() {
        int burn = data.get(2);
        int maxBurn = data.get(3);
        return maxBurn == 0 || burn == 0 ? 0 : burn * 14 / maxBurn;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if (index >= 2 && index < REFINERY_SLOT_COUNT) {
                if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, result);
            } else if (index < REFINERY_SLOT_COUNT) {
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
        return stillValid(access, player, OWRBlocks.REFINERY.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 110 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 168));
        }
    }

    private boolean moveToMatchingInput(ItemStack stack) {
        for (int slot = 0; slot <= RefineryBlockEntity.SLOT_FUEL; slot++) {
            Slot inputSlot = slots.get(slot);
            if (inputSlot.mayPlace(stack) && moveItemStackTo(stack, slot, slot + 1, false)) {
                return true;
            }
        }
        return false;
    }

    private static class InputSlot extends Slot {
        InputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return container instanceof RefineryBlockEntity refinery && refinery.isValidForSlot(getSlotIndex(), stack);
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
