package com.gregtechceu.gtlib.client.utils;

import com.gregtechceu.gtlib.Platform;
import com.gregtechceu.gtlib.client.shader.Shaders;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL43;

/**
 * @author KilaBash
 * @date 2022/12/11
 * @implNote ShaderUtils
 */
@Environment(EnvType.CLIENT)
public class ShaderUtils {

    /**
     * fast blit - fast copy a fbo to another one (color component)
     * @param from fbo
     * @param to fbo
     */
    public static void fastBlit(RenderTarget from, RenderTarget to) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._disableDepthTest();
        GlStateManager._depthMask(false);

        to.bindWrite(true);

        Shaders.getBlitShader().setSampler("DiffuseSampler", from.getColorTextureId());

        Shaders.getBlitShader().apply();
        GlStateManager._enableBlend();
        RenderSystem.defaultBlendFunc();

        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(-1, 1, 0).endVertex();
        bufferbuilder.vertex(-1, -1, 0).endVertex();
        bufferbuilder.vertex(1, -1, 0).endVertex();
        bufferbuilder.vertex(1, 1, 0).endVertex();
        BufferUploader.draw(bufferbuilder.end());
        Shaders.getBlitShader().clear();

        GlStateManager._depthMask(true);
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._enableDepthTest();
    }

    private static final boolean DEBUG_LABEL_AVAILABLE = GL.getCapabilities().GL_KHR_debug;

    public static void warpGLDebugLabel(String message, Runnable block) {
        if (DEBUG_LABEL_AVAILABLE && Platform.isDevEnv()) {
            GL43.glPushDebugGroup(GL43.GL_DEBUG_SOURCE_APPLICATION, 0, message);
            block.run();
            GL43.glPopDebugGroup();
        } else {
            block.run();
        }
    }
}
