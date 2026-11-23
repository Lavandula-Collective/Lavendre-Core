package com.lavandula.lavendrecore.mixin.client;

import com.lavandula.lavendrecore.client.ToolSlotHotbarLayout;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
abstract class DrawContextMixin {
    @Inject(method = "drawGuiTexture(Lnet/minecraft/util/Identifier;IIIII)V", at = @At("HEAD"))
    private void lavendre$measureHotbar(Identifier sprite, int x, int y, int z, int width, int height, CallbackInfo ci) {
        ToolSlotHotbarLayout.onGuiSprite((DrawContext) (Object) this, sprite, x, y, width, height);
    }
}
