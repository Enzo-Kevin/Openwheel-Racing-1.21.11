package com.openwheelracing.content.item;

import com.openwheelracing.content.car.PrototypeCarSetup;
import com.openwheelracing.content.entity.OpenwheelCarEntity;
import com.openwheelracing.registry.OWRDataComponents;
import com.openwheelracing.registry.OWREntities;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PrototypeCarItem extends Item {
    public PrototypeCarItem(Properties properties) {
        super(properties);
    }

    public static PrototypeCarSetup getSetup(ItemStack stack) {
        PrototypeCarSetup setup = stack.get(OWRDataComponents.CAR_SETUP.get());
        return setup == null ? PrototypeCarSetup.DEFAULT : setup;
    }

    public static ItemStack createWithDefaultSetup() {
        ItemStack stack = new ItemStack(com.openwheelracing.registry.OWRItems.PROTOTYPE_CAR_SPAWN.get());
        stack.set(OWRDataComponents.CAR_SETUP.get(), PrototypeCarSetup.DEFAULT);
        return stack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (!level.isClientSide() && player != null) {
            Vec3 spawnPos = Vec3.atBottomCenterOf(context.getClickedPos().relative(context.getClickedFace()));
            OpenwheelCarEntity car = new OpenwheelCarEntity(OWREntities.PROTOTYPE_CAR.get(), level);
            car.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            car.setYRot(player.getYRot());
            car.setSetup(getSetup(stack));
            level.addFreshEntity(car);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        PrototypeCarSetup setup = getSetup(stack);
        tooltip.accept(Component.translatable("tooltip.openwheelracing.prototype_car.single_seat").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.openwheelracing.prototype_car.power", setup.power()).withStyle(ChatFormatting.RED));
        tooltip.accept(Component.translatable("tooltip.openwheelracing.prototype_car.grip", setup.grip()).withStyle(ChatFormatting.GREEN));
        tooltip.accept(Component.translatable("tooltip.openwheelracing.prototype_car.aero", setup.aero()).withStyle(ChatFormatting.AQUA));
        tooltip.accept(Component.translatable("tooltip.openwheelracing.prototype_car.gearing", setup.gearing()).withStyle(ChatFormatting.GOLD));
    }
}
