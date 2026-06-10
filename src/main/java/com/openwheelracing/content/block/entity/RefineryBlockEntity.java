package com.openwheelracing.content.block.entity;

import com.openwheelracing.content.menu.RefineryMenu;
import com.openwheelracing.registry.OWRBlockEntities;
import com.openwheelracing.registry.OWRItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class RefineryBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int SLOT_CRUDE = 0;
    public static final int SLOT_FUEL = 1;
    public static final int FIRST_OUTPUT_SLOT = 2;
    public static final int SLOT_COUNT = 7;
    private static final int MAX_PROGRESS = 300;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int progress;
    private int burnTime;
    private int maxBurnTime;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> MAX_PROGRESS;
                case 2 -> burnTime;
                case 3 -> maxBurnTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 2 -> burnTime = value;
                case 3 -> maxBurnTime = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public RefineryBlockEntity(BlockPos pos, BlockState state) {
        super(OWRBlockEntities.REFINERY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RefineryBlockEntity refinery) {
        if (refinery.burnTime > 0) {
            refinery.burnTime--;
        }

        if (refinery.canRefine()) {
            if (refinery.burnTime <= 0) {
                refinery.consumeFuel(level);
            }

            if (refinery.burnTime > 0) {
                refinery.progress++;
                if (refinery.progress >= MAX_PROGRESS) {
                    refinery.refine(level);
                    refinery.progress = 0;
                }
            }
        } else {
            refinery.progress = 0;
        }
        refinery.setChanged();
    }

    public ContainerData getData() {
        return data;
    }

    public boolean isValidForSlot(int slot, ItemStack stack) {
        if (slot == SLOT_CRUDE) {
            return stack.is(OWRItems.CRUDE_OIL_BUCKET.get()) || stack.is(OWRItems.CRUDE_OIL_CHUNK.get());
        }
        if (slot == SLOT_FUEL) {
            return getBurnDuration(level, stack) > 0;
        }
        return false;
    }

    private boolean canRefine() {
        return isValidCrude(getItem(SLOT_CRUDE)) && hasAnyOutputSpace();
    }

    private void consumeFuel(Level level) {
        ItemStack fuel = getItem(SLOT_FUEL);
        int duration = getBurnDuration(level, fuel);
        if (duration <= 0) {
            return;
        }

        burnTime = duration;
        maxBurnTime = duration;
        ItemStack remainder = fuel.getCraftingRemainder();
        fuel.shrink(1);
        if (fuel.isEmpty() && !remainder.isEmpty()) {
            setItem(SLOT_FUEL, remainder);
        }
    }

    private void refine(Level level) {
        ItemStack crude = getItem(SLOT_CRUDE);
        ItemStack remainder = crude.getCraftingRemainder();
        crude.shrink(1);
        if (crude.isEmpty() && !remainder.isEmpty()) {
            setItem(SLOT_CRUDE, remainder);
        }

        List<ItemStack> products = List.of(
            new ItemStack(OWRItems.GAS.get()),
            new ItemStack(OWRItems.PETROL_CAN.get()),
            new ItemStack(OWRItems.CRUDE_RUBBER.get()),
            new ItemStack(OWRItems.DIESEL_CAN.get()),
            new ItemStack(OWRItems.ASPHALT_BINDER.get())
        );

        for (int i = 0; i < 4; i++) {
            ItemStack product = products.get(level.random.nextInt(products.size())).copy();
            insertOutput(product);
        }
    }

    private boolean hasAnyOutputSpace() {
        for (int slot = FIRST_OUTPUT_SLOT; slot < SLOT_COUNT; slot++) {
            ItemStack stack = getItem(slot);
            if (stack.isEmpty() || isRefineryProduct(stack) && stack.getCount() < stack.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    private void insertOutput(ItemStack product) {
        for (int slot = FIRST_OUTPUT_SLOT; slot < SLOT_COUNT; slot++) {
            ItemStack stack = getItem(slot);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, product) && stack.getCount() < stack.getMaxStackSize()) {
                stack.grow(1);
                return;
            }
        }
        for (int slot = FIRST_OUTPUT_SLOT; slot < SLOT_COUNT; slot++) {
            if (getItem(slot).isEmpty()) {
                setItem(slot, product);
                return;
            }
        }
    }

    private static boolean isValidCrude(ItemStack stack) {
        return stack.is(OWRItems.CRUDE_OIL_BUCKET.get()) || stack.is(OWRItems.CRUDE_OIL_CHUNK.get());
    }

    private static boolean isRefineryProduct(ItemStack stack) {
        return stack.is(OWRItems.GAS.get()) || stack.is(OWRItems.PETROL_CAN.get()) || stack.is(OWRItems.CRUDE_RUBBER.get()) || stack.is(OWRItems.DIESEL_CAN.get()) || stack.is(OWRItems.ASPHALT_BINDER.get());
    }

    private static int getBurnDuration(@Nullable Level level, ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (stack.is(OWRItems.GAS.get())) {
            return 400;
        }
        if (stack.is(OWRItems.PETROL_CAN.get())) {
            return 1200;
        }
        if (stack.is(OWRItems.DIESEL_CAN.get())) {
            return 1600;
        }
        if (level == null) {
            return 0;
        }
        FuelValues fuelValues = level.fuelValues();
        return fuelValues.burnDuration(stack);
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
        return Component.translatable("container.openwheelracing.refinery");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new RefineryMenu(containerId, playerInventory, this, data);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.putInt("Progress", progress);
        output.putInt("BurnTime", burnTime);
        output.putInt("MaxBurnTime", maxBurnTime);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, items);
        progress = input.getIntOr("Progress", 0);
        burnTime = input.getIntOr("BurnTime", 0);
        maxBurnTime = input.getIntOr("MaxBurnTime", 0);
    }
}
