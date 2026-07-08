package com.openwheelracing.content.item;

import com.openwheelracing.content.car.PrototypeCarSetup;
import com.openwheelracing.registry.OWRDataComponents;
import com.openwheelracing.registry.OWRItems;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class TyreItem extends Item {
    public TyreItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(int compound) {
        return create(compound, 1, 100);
    }

    public static ItemStack create(int compound, int count) {
        return create(compound, count, 100);
    }

    public static ItemStack create(int compound, int count, double remainingPercent) {
        ItemStack stack = new ItemStack(OWRItems.TIRES.get(), count);
        stack.set(OWRDataComponents.TYRE_COMPOUND.get(), clampCompound(compound));
        setRemainingPercent(stack, remainingPercent);
        return stack;
    }

    public static int getCompound(ItemStack stack) {
        Integer compound = stack.get(OWRDataComponents.TYRE_COMPOUND.get());
        return compound == null ? PrototypeCarSetup.DEFAULT.grip() : clampCompound(compound);
    }

    public static int getRemainingPercent(ItemStack stack) {
        Integer remainingPercent = stack.get(OWRDataComponents.TYRE_REMAINING_PERCENT.get());
        return remainingPercent == null ? 100 : normalizeRemainingPercent(remainingPercent);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.openwheelracing.tires.compound", getCompound(stack) + 1).withStyle(ChatFormatting.GREEN));
        tooltip.accept(Component.translatable("tooltip.openwheelracing.tires.remaining", getRemainingPercent(stack)).withStyle(ChatFormatting.YELLOW));
    }

    public static int normalizeRemainingPercent(double remainingPercent) {
        return (int) Math.floor(Math.max(0.0, Math.min(100.0, remainingPercent)));
    }

    private static void setRemainingPercent(ItemStack stack, double remainingPercent) {
        int normalizedRemainingPercent = normalizeRemainingPercent(remainingPercent);
        if (normalizedRemainingPercent >= 100) {
            stack.remove(OWRDataComponents.TYRE_REMAINING_PERCENT.get());
        } else {
            stack.set(OWRDataComponents.TYRE_REMAINING_PERCENT.get(), normalizedRemainingPercent);
        }
    }

    private static int clampCompound(int compound) {
        return Math.max(0, Math.min(4, compound));
    }
}
