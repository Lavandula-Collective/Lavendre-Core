package com.lavandula.lavendrecore.mixin;

import com.lavandula.lavendrecore.toolslot.ToolSlotMining;
import com.lavandula.lavendrecore.toolslot.ToolSlotUse;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractBlock.AbstractBlockState.class)
abstract class AbstractBlockStateMixin {
    @WrapMethod(method = "calcBlockBreakingDelta")
    private float lavendre$calculateMiningProgress(PlayerEntity player, BlockView world, BlockPos pos, Operation<Float> original) {
        BlockState state = (BlockState) (Object) this;
        return ToolSlotUse.withTool(player, ToolSlotMining.select(player, state, pos), () -> original.call(player, world, pos));
    }

    @WrapMethod(method = "onBlockBreakStart")
    private void lavendre$beginMining(World world, BlockPos pos, PlayerEntity player, Operation<Void> original) {
        BlockState state = (BlockState) (Object) this;
        ToolSlotUse.withTool(player, ToolSlotMining.select(player, state, pos), () -> original.call(world, pos, player));
    }
}
