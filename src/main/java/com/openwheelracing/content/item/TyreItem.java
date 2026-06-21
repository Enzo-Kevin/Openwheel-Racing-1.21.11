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
        return create(compound, 1);
    }

    public static ItemStack create(int compound, int count) {
        ItemStack stack = new ItemStack(OWRItems.TIRES.get(), count);
        stack.set(OWRDataComponents.TYRE_COMPOUND.get(), clampCompound(compound));
        return stack;
    }

    public static int getCompound(ItemStack stack) {
        Integer compound = stack.get(OWRDataComponents.TYRE_COMPOUND.get());
        return compound == null ? PrototypeCarSetup.DEFAULT.grip() : clampCompound(compound);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.openwheelracing.tires.compound", getCompound(stack) + 1).withStyle(ChatFormatting.GREEN));
    }

    private static int clampCompound(int compound) {
        return Math.max(0, Math.min(4, compound));
    }
}
