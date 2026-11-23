package com.lavandula.lavendrecore.client;

import com.lavandula.lavendrecore.LavendreConfig;
import com.lavandula.lavendrecore.ToolSlotSettings;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public final class ToolSlotHud {
    private static final long FADE_IN_MS = 90;
    private static final long FADE_OUT_MS = 200;
    private static final int GAP = 4;

    private ItemStack displayed = ItemStack.EMPTY;
    private boolean showing;
    private float startOpacity;
    private long transitionStart;
    private PlayerEntity lastPlayer;

    public void render(DrawContext context, MinecraftClient client, ToolSlotHotbarLayout layout) {
        PlayerEntity player = client.player;
        ToolSlotHotbarLayout.Position position = layout.indicatorPosition(ToolSlotRenderer.INDICATOR_SIZE, GAP);
        boolean visible = position != null && player != null && client.getCameraEntity() == player && !client.options.hudHidden;
        if (player != lastPlayer || !visible) {
            displayed = ItemStack.EMPTY;
            showing = false;
            startOpacity = 0;
            transitionStart = 0;
            lastPlayer = player;
        }
        if (!visible) {
            return;
        }

        ToolSlotSettings settings = LavendreConfig.clientToolSlot();
        int x = position.x() + settings.indicatorX();
        int y = position.y() + settings.indicatorY();
        ItemStack tool = ActiveToolSlot.get(client);
        long now = Util.getMeasuringTimeMs();
        boolean active = !tool.isEmpty();
        if (active != showing) {
            startOpacity = opacity(now, settings);
            transitionStart = now;
            showing = active;
        }
        if (active && !ItemStack.areEqual(displayed, tool)) {
            displayed = tool.copy();
        }

        float alpha = opacity(now, settings);
        if (alpha <= 0 || displayed.isEmpty()) {
            if (!showing) {
                displayed = ItemStack.EMPTY;
            }
            return;
        }

        MatrixStack matrices = context.getMatrices();
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        float[] shaderColor = RenderSystem.getShaderColor();
        float red = shaderColor[0];
        float green = shaderColor[1];
        float blue = shaderColor[2];
        float opacity = shaderColor[3];
        matrices.push();
        matrices.loadIdentity();
        try {
            RenderSystem.setShaderColor(1, 1, 1, 1);
            if (alpha >= 1) {
                ToolSlotRenderer.drawIndicator(context, client, player, displayed, x, y);
            } else {
                ToolSlotIndicatorFade.draw(context, client, player, displayed, x, y, alpha);
            }
        } finally {
            matrices.pop();
            RenderSystem.setShaderColor(red, green, blue, opacity);
            if (depth) {
                RenderSystem.enableDepthTest();
            } else {
                RenderSystem.disableDepthTest();
            }
            if (blend) {
                RenderSystem.enableBlend();
            } else {
                RenderSystem.disableBlend();
            }
        }
    }

    private float opacity(long now, ToolSlotSettings settings) {
        long duration = showing ? FADE_IN_MS : FADE_OUT_MS;
        long elapsed = now - transitionStart - (showing ? 0 : settings.indicatorHoldMs());
        if (!settings.fade()) {
            return showing || elapsed < 0 ? 1 : 0;
        }
        float progress = MathHelper.clamp(elapsed / (float) duration, 0, 1);
        return MathHelper.lerp(progress, startOpacity, showing ? 1 : 0);
    }

    public static void close() {
        ToolSlotIndicatorFade.close();
    }
}
