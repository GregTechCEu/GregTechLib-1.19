package com.gregtechceu.gtlib.gui.util;

import com.gregtechceu.gtlib.client.shader.Shaders;
import com.gregtechceu.gtlib.client.shader.management.ShaderProgram;
import com.gregtechceu.gtlib.client.shader.uniform.UniformCache;
import com.gregtechceu.gtlib.client.utils.RenderBufferUtils;
import com.gregtechceu.gtlib.side.fluid.FluidHelper;
import com.gregtechceu.gtlib.side.fluid.FluidStack;
import com.gregtechceu.gtlib.utils.GTLibUtils;
import com.gregtechceu.gtlib.utils.Position;
import com.gregtechceu.gtlib.utils.Rect;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec2;

import javax.annotation.Nullable;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DrawerHelper {

    public static ShaderProgram ROUND;
    public static ShaderProgram PANEL_BG;
    public static ShaderProgram ROUND_BOX;
    public static ShaderProgram PROGRESS_ROUND_BOX;
    public static ShaderProgram FRAME_ROUND_BOX;
    public static ShaderProgram ROUND_LINE;

    public static void init() {
        ROUND = GTLibUtils.make(new ShaderProgram(), program
                -> program.attach(Shaders.ROUND_F).attach(Shaders.SCREEN_V));
        PANEL_BG = GTLibUtils.make(new ShaderProgram(), program
                -> program.attach(Shaders.PANEL_BG_F).attach(Shaders.SCREEN_V));
        ROUND_BOX = GTLibUtils.make(new ShaderProgram(), program
                -> program.attach(Shaders.ROUND_BOX_F).attach(Shaders.SCREEN_V));
        PROGRESS_ROUND_BOX = GTLibUtils.make(new ShaderProgram(), program
                -> program.attach(Shaders.PROGRESS_ROUND_BOX_F).attach(Shaders.SCREEN_V));
        FRAME_ROUND_BOX = GTLibUtils.make(new ShaderProgram(), program
                -> program.attach(Shaders.FRAME_ROUND_BOX_F).attach(Shaders.SCREEN_V));
        ROUND_LINE = GTLibUtils.make(new ShaderProgram(), program
                -> program.attach(Shaders.ROUND_LINE_F).attach(Shaders.SCREEN_V));
    }


    @Environment(EnvType.CLIENT)
    public static void drawFluidTexture(PoseStack poseStack, float xCoord, float yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, float zLevel, int fluidColor) {
        float uMin = textureSprite.getU0();
        float uMax = textureSprite.getU1();
        float vMin = textureSprite.getV0();
        float vMax = textureSprite.getV1();
        uMax = uMax - maskRight / 16f * (uMax - uMin);
        vMax = vMax - maskTop / 16f * (vMax - vMin);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        Matrix4f mat = poseStack.last().pose();
        buffer.vertex(mat, xCoord, yCoord + 16, zLevel).uv(uMin, vMax).color(fluidColor).endVertex();
        buffer.vertex(mat, xCoord + 16 - maskRight, yCoord + 16, zLevel).uv(uMax, vMax).color(fluidColor).endVertex();
        buffer.vertex(mat, xCoord + 16 - maskRight, yCoord + maskTop, zLevel).uv(uMax, vMin).color(fluidColor).endVertex();
        buffer.vertex(mat, xCoord, yCoord + maskTop, zLevel).uv(uMin, vMin).color(fluidColor).endVertex();

        BufferUploader.drawWithShader(buffer.end());
    }

    @Environment(EnvType.CLIENT)
    public static void drawFluidForGui(PoseStack poseStack, FluidStack contents, long tankCapacity, int startX, int startY, int widthT, int heightT) {
        ResourceLocation LOCATION_BLOCKS_TEXTURE = InventoryMenu.BLOCK_ATLAS;
        TextureAtlasSprite fluidStillSprite = FluidHelper.getStillTexture(contents);
        int fluidColor = FluidHelper.getColor(contents) | 0xff000000;
        int scaledAmount = (int) (contents.getAmount() * heightT / tankCapacity);
        if (contents.getAmount() > 0 && scaledAmount < 1) {
            scaledAmount = 1;
        }
        if (scaledAmount > heightT || contents.getAmount() == tankCapacity) {
            scaledAmount = heightT;
        }
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, LOCATION_BLOCKS_TEXTURE);

        final int xTileCount = widthT / 16;
        final int xRemainder = widthT - xTileCount * 16;
        final int yTileCount = scaledAmount / 16;
        final int yRemainder = scaledAmount - yTileCount * 16;

        final int yStart = startY + heightT;

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int width = xTile == xTileCount ? xRemainder : 16;
                int height = yTile == yTileCount ? yRemainder : 16;
                int x = startX + xTile * 16;
                int y = yStart - (yTile + 1) * 16;
                if (width > 0 && height > 0) {
                    int maskTop = 16 - height;
                    int maskRight = 16 - width;
                    drawFluidTexture(poseStack, x, y, fluidStillSprite, maskTop, maskRight, 0, fluidColor);
                }
            }
        }
        RenderSystem.enableBlend();
    }

    @Environment(EnvType.CLIENT)
    public static void drawBorder(PoseStack poseStack, int x, int y, int width, int height, int color, int border) {
        drawSolidRect(poseStack,x - border, y - border, width + 2 * border, border, color);
        drawSolidRect(poseStack,x - border, y + height, width + 2 * border, border, color);
        drawSolidRect(poseStack,x - border, y, border, height, color);
        drawSolidRect(poseStack,x + width, y, border, height, color);
    }

    @Environment(EnvType.CLIENT)
    public static void drawStringSized(PoseStack poseStack, String text, float x, float y, int color, boolean dropShadow, float scale, boolean center) {
        poseStack.pushPose();
        Font fontRenderer = Minecraft.getInstance().font;
        double scaledTextWidth = center ? fontRenderer.width(text) * scale : 0.0;
        poseStack.translate(x - scaledTextWidth / 2.0, y, 0.0f);
        poseStack.scale(scale, scale, scale);
        if (dropShadow) {
            fontRenderer.drawShadow(poseStack, text, 0, 0, color);
        } else {
            fontRenderer.draw(poseStack, text, 0, 0, color);
        }
        poseStack.popPose();
    }

    @Environment(EnvType.CLIENT)
    public static void drawStringFixedCorner(PoseStack poseStack, String text, float x, float y, int color, boolean dropShadow, float scale) {
        Font fontRenderer = Minecraft.getInstance().font;
        float scaledWidth = fontRenderer.width(text) * scale;
        float scaledHeight = fontRenderer.lineHeight * scale;
        drawStringSized(poseStack, text, x - scaledWidth, y - scaledHeight, color, dropShadow, scale, false);
    }

    @Environment(EnvType.CLIENT)
    public static void drawText(PoseStack poseStack, String text, float x, float y, float scale, int color) {
        drawText(poseStack, text, x, y, scale, color, false);
    }

    @Environment(EnvType.CLIENT)
    public static void drawText(PoseStack poseStack, String text, float x, float y, float scale, int color, boolean shadow) {
        Font fontRenderer = Minecraft.getInstance().font;
        RenderSystem.disableBlend();
        poseStack.pushPose();
        poseStack.scale(scale, scale, 0f);
        float sf = 1 / scale;
        if (shadow) {
            fontRenderer.drawShadow(poseStack, text, x * sf, y * sf, color);
        } else {
            fontRenderer.draw(poseStack, text, x * sf, y * sf, color);
        }
        poseStack.popPose();
        RenderSystem.enableBlend();
    }

    @Environment(EnvType.CLIENT)
    public static void drawItemStack(PoseStack poseStack, ItemStack itemStack, int x, int y, int color, @Nullable String altTxt) {
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.mulPoseMatrix(poseStack.last().pose());
        posestack.translate(0.0D, 0.0D, 32.0D);
        RenderSystem.applyModelViewMatrix();
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r   = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >>  8 & 255) / 255.0F;
        float b  = (float)(color       & 255) / 255.0F;
        RenderSystem.setShaderColor(r, g, b, a);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();

        itemRenderer.blitOffset = 200.0F;
        itemRenderer.renderAndDecorateItem(itemStack, x, y);
        itemRenderer.renderGuiItemDecorations(mc.font, itemStack, x, y, altTxt);
        itemRenderer.blitOffset = 0.0F;

        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
    }

    @Environment(EnvType.CLIENT)
    public static List<Component> getItemToolTip(ItemStack itemStack) {
        Minecraft mc = Minecraft.getInstance();
        return itemStack.getTooltipLines(mc.player, mc.options.advancedItemTooltips ?  TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
    }

    @Environment(EnvType.CLIENT)
    public static void drawSolidRect(PoseStack poseStack, int x, int y, int width, int height, int color) {
        Gui.fill(poseStack, x, y, x + width, y + height, color);
        RenderSystem.enableBlend();
    }

    @Environment(EnvType.CLIENT)
    public static void drawSolidRect(PoseStack poseStack, Rect rect, int color) {
        drawSolidRect(poseStack, rect.left, rect.up, rect.right, rect.down, color);
    }

    @Environment(EnvType.CLIENT)
    public static void drawRectShadow(PoseStack poseStack, int x, int y, int width, int height, int distance) {
        drawGradientRect(poseStack, x + distance, y + height, width - distance, distance, 0x4f000000, 0, false);
        drawGradientRect(poseStack, x + width, y + distance, distance, height - distance, 0x4f000000, 0, true);

        float startAlpha = (float) (0x4f) / 255.0F;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        x += width;
        y += height;
        Matrix4f mat = poseStack.last().pose();
        buffer.vertex(mat, x, y, 0).color(0, 0, 0, startAlpha).endVertex();
        buffer.vertex(mat, x, y + distance, 0).color(0, 0, 0, 0).endVertex();
        buffer.vertex(mat, x + distance, y + distance, 0).color(0, 0, 0, 0).endVertex();

        buffer.vertex(mat, x, y, 0).color(0, 0, 0, startAlpha).endVertex();
        buffer.vertex(mat, x + distance, y + distance, 0).color(0, 0, 0, 0).endVertex();
        buffer.vertex(mat, x + distance, y, 0).color(0, 0, 0, 0).endVertex();
        tesselator.end();
        RenderSystem.enableTexture();
    }

    @Environment(EnvType.CLIENT)
    public static void drawGradientRect(PoseStack poseStack, int x, int y, int width, int height, int startColor, int endColor) {
        drawGradientRect(poseStack, x, y, width, height, startColor, endColor, false);
    }

    @Environment(EnvType.CLIENT)
    public static void drawGradientRect(PoseStack poseStack, float x, float y, float width, float height, int startColor, int endColor, boolean horizontal) {
        float startAlpha = (float)(startColor >> 24 & 255) / 255.0F;
        float startRed   = (float)(startColor >> 16 & 255) / 255.0F;
        float startGreen = (float)(startColor >>  8 & 255) / 255.0F;
        float startBlue  = (float)(startColor       & 255) / 255.0F;
        float endAlpha   = (float)(endColor   >> 24 & 255) / 255.0F;
        float endRed     = (float)(endColor   >> 16 & 255) / 255.0F;
        float endGreen   = (float)(endColor   >>  8 & 255) / 255.0F;
        float endBlue    = (float)(endColor         & 255) / 255.0F;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        Matrix4f mat = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        if (horizontal) {
            buffer.vertex(mat,x + width, y, 0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
            buffer.vertex(mat,x, y, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.vertex(mat,x, y + height, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.vertex(mat,x + width, y + height, 0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
            tesselator.end();
        } else {
            buffer.vertex(mat,x + width, y, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.vertex(mat,x, y, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.vertex(mat,x, y + height, 0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
            buffer.vertex(mat,x + width, y + height, 0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
            tesselator.end();
        }
        RenderSystem.enableTexture();
    }

    @Environment(EnvType.CLIENT)
    public static void drawLines(PoseStack poseStack, List<Vec2> points, int startColor, int endColor, float width) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        RenderBufferUtils.drawColorLines(poseStack, bufferbuilder, points, startColor, endColor, width);

        tesselator.end();
        RenderSystem.enableTexture();
        RenderSystem.defaultBlendFunc();
    }

    @Environment(EnvType.CLIENT)
    public static void drawTextureRect(PoseStack poseStack, float x, float y, float width, float height) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        Matrix4f mat = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(mat, x, y + height, 0).uv(0, 0).endVertex();
        buffer.vertex(mat, x + width, y + height, 0).uv(1, 0).endVertex();
        buffer.vertex(mat, x + width, y, 0).uv(1, 1).endVertex();
        buffer.vertex(mat, x, y, 0).uv(0, 1).endVertex();
        tesselator.end();
    }

    public static void updateScreenVshUniform(PoseStack poseStack, UniformCache uniform) {
        var window = Minecraft.getInstance().getWindow();

        uniform.glUniform1F("GuiScale", (float) window.getGuiScale());
        uniform.glUniform2F("ScreenSize", (float) window.getWidth(), (float) window.getHeight());
        uniform.glUniformMatrix4F("PoseStack",poseStack.last().pose());
        uniform.glUniformMatrix4F("ProjMat", RenderSystem.getProjectionMatrix());
    }

    @Environment(EnvType.CLIENT)
    public static void drawRound(PoseStack poseStack, int color, float radius, Position centerPos) {
        DrawerHelper.ROUND.use(uniform -> {

            DrawerHelper.updateScreenVshUniform(poseStack, uniform);

            uniform.fillRGBAColor("Color", color);

            uniform.glUniform1F("StepLength", 1f);
            uniform.glUniform1F("Radius", radius);
            uniform.glUniform2F("CenterPos", centerPos.x, centerPos.y);
        });

        RenderSystem.enableBlend();
        uploadScreenPosVertex();
    }

    @Environment(EnvType.CLIENT)
    public static void drawPanelBg(PoseStack poseStack) {
        DrawerHelper.PANEL_BG.use(uniform -> {

            DrawerHelper.updateScreenVshUniform(poseStack, uniform);

            uniform.glUniform1F("Density", 5);
            uniform.glUniform1F("SquareSize", 0.1f);
            var bg = 20f / 255f;
            uniform.glUniform4F("BgColor", bg, bg, bg, 0.95f);
            var square = 40f / 255f;
            uniform.glUniform4F("SquareColor", square, square, square, 0.95f);
        });

        RenderSystem.enableBlend();
        uploadScreenPosVertex();
    }

    @Environment(EnvType.CLIENT)
    public static void drawRoundBox(PoseStack poseStack, Rect square, Vector4f radius, int color) {
        DrawerHelper.ROUND_BOX.use(uniform -> {
            DrawerHelper.updateScreenVshUniform(poseStack, uniform);

            uniform.glUniform4F("SquareVertex", square.left - 1f, square.up - 1f, square.right - 1f, square.down - 1f);
            uniform.glUniform4F("RoundRadius", radius.x(), radius.y(), radius.z(), radius.w());
            uniform.fillRGBAColor("Color", color);
            uniform.glUniform1F("Blur", 2);
        });

        RenderSystem.enableBlend();
        uploadScreenPosVertex();
    }

    @Environment(EnvType.CLIENT)
    public static void drawProgressRoundBox(PoseStack poseStack, Rect square, Vector4f radius, int color1, int color2, float progress) {
        DrawerHelper.PROGRESS_ROUND_BOX.use(uniform -> {
            DrawerHelper.updateScreenVshUniform(poseStack, uniform);

            uniform.glUniform4F("SquareVertex", square.left, square.up, square.right, square.down);
            uniform.glUniform4F("RoundRadius", radius.x(), radius.y(), radius.z(), radius.w());
            uniform.fillRGBAColor("Color1", color1);
            uniform.fillRGBAColor("Color2", color2);
            uniform.glUniform1F("Blur", 2);
            uniform.glUniform1F("Progress", progress);
        });

        RenderSystem.enableBlend();
        uploadScreenPosVertex();
    }

    @Environment(EnvType.CLIENT)
    public static void drawFrameRoundBox(PoseStack poseStack, Rect square, float thickness, Vector4f radius1, Vector4f radius2, int color) {
        DrawerHelper.FRAME_ROUND_BOX.use(uniform -> {
            DrawerHelper.updateScreenVshUniform(poseStack, uniform);

            uniform.glUniform4F("SquareVertex", square.left - 1, square.up - 1, square.right - 1, square.down - 1);
            uniform.glUniform4F("RoundRadius1", radius1.x(), radius1.y(), radius1.z(), radius1.w());
            uniform.glUniform4F("RoundRadius2", radius2.x(), radius2.y(), radius2.z(), radius2.w());
            uniform.glUniform1F("Thickness", thickness);
            uniform.fillRGBAColor("Color", color);
            uniform.glUniform1F("Blur", 2);
        });

        RenderSystem.enableBlend();
        uploadScreenPosVertex();
    }

    @Environment(EnvType.CLIENT)
    public static void drawRoundLine(PoseStack poseStack, Position begin, Position end, int width, int color1, int color2) {
        DrawerHelper.ROUND_LINE.use(uniform -> {
            DrawerHelper.updateScreenVshUniform(poseStack, uniform);

            uniform.glUniform1F("Width", width);
            uniform.glUniform2F("Point1", begin.x, begin.y);
            uniform.glUniform2F("Point2", end.x, end.y);
            uniform.fillRGBAColor("Color1", color1);
            uniform.fillRGBAColor("Color2", color2);
            uniform.glUniform1F("Blur", 2);
        });

        RenderSystem.enableBlend();
        uploadScreenPosVertex();
    }

    @Environment(EnvType.CLIENT)
    private static void uploadScreenPosVertex() {
        var builder = Tesselator.getInstance().getBuilder();

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        builder.vertex(-1.0, 1.0, 0.0).endVertex();
        builder.vertex(-1.0, -1.0, 0.0).endVertex();
        builder.vertex(1.0, -1.0, 0.0).endVertex();
        builder.vertex(1.0, 1.0, 0.0).endVertex();
        BufferUploader.draw(builder.end());
    }

}
