package com.openwheelracing.content.item;

import com.openwheelracing.content.race.OWRRaceControlState;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class RaceControlItem extends Item {
    public RaceControlItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel) {
            OWRRaceControlState state = OWRRaceControlState.get(serverLevel);
            if (player.isShiftKeyDown()) {
                state.toggleOffTrackCheck();
            } else {
                state.toggleCheckpointCheck();
            }
            player.displayClientMessage(Component.translatable(
                "message.openwheelracing.race_control.status",
                ruleState(state.isCheckpointCheckEnabled()),
                ruleState(state.isOffTrackCheckEnabled())
            ), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.openwheelracing.race_control.use").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.openwheelracing.race_control.sneak_use").withStyle(ChatFormatting.GRAY));
    }

    private static Component ruleState(boolean enabled) {
        return Component.translatable(enabled ? "message.openwheelracing.race_control.enabled" : "message.openwheelracing.race_control.disabled");
    }
}
