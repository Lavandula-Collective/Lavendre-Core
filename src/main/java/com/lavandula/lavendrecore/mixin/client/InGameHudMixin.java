package com.lavandula.lavendrecore.mixin.client;

import com.lavandula.lavendrecore.client.ToolSlotHotbarLayout;
import com.lavandula.lavendrecore.client.ToolSlotHud;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(InGameHud.class)
abstract class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;
    @Unique private final ToolSlotHud lavendre$toolSlotHud = new ToolSlotHud();

    @WrapMethod(method = "render")
    private void lavendre$drawToolSlot(DrawContext context, RenderTickCounter tickCounter, Operation<Void> original) {
        ToolSlotHotbarLayout layout = new ToolSlotHotbarLayout(context);
        layout.capture(() -> original.call(context, tickCounter));
        lavendre$toolSlotHud.render(context, client, layout);
    }
}
