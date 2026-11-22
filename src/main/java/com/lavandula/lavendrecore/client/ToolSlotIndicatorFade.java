package com.lavandula.lavendrecore.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/** Composites the indicator as a unit so opaque item models fade along with their frame. */
final class ToolSlotIndicatorFade {
    private static final int SIZE = ToolSlotRenderer.INDICATOR_SIZE;
    private static final Matrix4f GUI_PROJECTION = new Matrix4f().setOrtho(0, SIZE, SIZE, 0, 1000, 21000);
    private static SimpleFramebuffer buffer;

    private ToolSlotIndicatorFade() {
    }

    static void draw(DrawContext context, MinecraftClient client, PlayerEntity player, ItemStack stack, int x, int y, float opacity) {
        context.draw();
        int readFramebuffer = GlStateManager._getInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int drawFramebuffer = GlStateManager._getInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        boolean scissored = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        Matrix4f projection = RenderSystem.getProjectionMatrix();
        VertexSorter sorter = RenderSystem.getVertexSorting();
        Matrix4fStack modelView = RenderSystem.getModelViewStack();

        modelView.pushMatrix().translation(0, 0, -11000);
        try {
            if (scissored) {
                GlStateManager._disableScissorTest();
            }
            int pixels = (int) Math.ceil(SIZE * client.getWindow().getScaleFactor());
            if (buffer == null) {
                buffer = new SimpleFramebuffer(pixels, pixels, true, MinecraftClient.IS_SYSTEM_MAC);
                buffer.setClearColor(0, 0, 0, 0);
            } else if (buffer.textureWidth != pixels) {
                buffer.resize(pixels, pixels, MinecraftClient.IS_SYSTEM_MAC);
            }

            buffer.clear(MinecraftClient.IS_SYSTEM_MAC);
            buffer.beginWrite(true);
            RenderSystem.setProjectionMatrix(GUI_PROJECTION, VertexSorter.BY_Z);
            RenderSystem.applyModelViewMatrix();
            DrawContext layer = new DrawContext(client, context.getVertexConsumers());
            ToolSlotRenderer.drawIndicator(layer, client, player, stack, 0, 0);
            layer.draw();
        } finally {
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFramebuffer);
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFramebuffer);
            GlStateManager._viewport(viewport[0], viewport[1], viewport[2], viewport[3]);
            if (scissored) {
                GlStateManager._enableScissorTest();
            }
            RenderSystem.setProjectionMatrix(projection, sorter);
            modelView.popMatrix();
            RenderSystem.applyModelViewMatrix();
        }

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, buffer.getColorAttachment());
        int tint = Math.round(opacity * 255);
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        BufferBuilder vertices = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        vertices.vertex(matrix, x, y, 0).texture(0, 1).color(tint, tint, tint, tint);
        vertices.vertex(matrix, x, y + SIZE, 0).texture(0, 0).color(tint, tint, tint, tint);
        vertices.vertex(matrix, x + SIZE, y + SIZE, 0).texture(1, 0).color(tint, tint, tint, tint);
        vertices.vertex(matrix, x + SIZE, y, 0).texture(1, 1).color(tint, tint, tint, tint);
        BufferRenderer.drawWithGlobalProgram(vertices.end());
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    static void close() {
        if (buffer != null) {
            buffer.delete();
            buffer = null;
        }
    }
}
