package com.lavandula.lavendrecore.mixin.compat;

import com.lavandula.lavendrecore.compat.BetterCombatCompat;
import com.lavandula.lavendrecore.toolslot.ToolSlotUse;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.bettercombat.logic.PlayerAttackHelper", remap = false)
abstract class BetterCombatPlayerAttackHelperMixin {
    @Inject(target = @Desc(value = "isDualWielding", args = PlayerEntity.class, ret = boolean.class),
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void lavendre$useToolSlotSwordInOneHand(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        // Better Combat swaps physical inventory stacks for offhand attacks.
        if (ToolSlotUse.isSwordScoped(player)
                || (player.getWorld().isClient() && !ToolSlotUse.hasHandScope(player.getInventory())
                && BetterCombatCompat.displayedSword(player) != null)) {
            cir.setReturnValue(false);
        }
    }
}
