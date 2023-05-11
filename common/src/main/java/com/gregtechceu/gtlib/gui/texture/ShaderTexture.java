package com.gregtechceu.gtlib.gui.texture;

import com.gregtechceu.gtlib.GTLib;
import com.gregtechceu.gtlib.client.shader.Shaders;
import com.gregtechceu.gtlib.client.shader.management.Shader;
import com.gregtechceu.gtlib.client.shader.management.ShaderManager;
import com.gregtechceu.gtlib.client.shader.management.ShaderProgram;
import com.gregtechceu.gtlib.client.shader.uniform.UniformCache;
import com.gregtechceu.gtlib.gui.editor.annotation.*;
import com.gregtechceu.gtlib.gui.util.DrawerHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

@RegisterUI(name = "shader_texture", group = "texture")
public class ShaderTexture extends TransformTexture {

    @Configurable(name = "gtlib.gui.editor.name.resource", tips = "gtlib.gui.editor.tips.shader_location")
    public ResourceLocation location;

    @Environment(EnvType.CLIENT)
    private ShaderProgram program;

    @Environment(EnvType.CLIENT)
    private Shader shader;

    @Configurable(tips = "gtlib.gui.editor.tips.shader_resolution")
    @NumberRange(range = {1, 3})
    private float resolution = 2;

    @Configurable
    @NumberColor
    private int color = -1;

    private Consumer<UniformCache> uniformCache;

    private final boolean isRaw;

    private ShaderTexture(boolean isRaw) {
        this.isRaw = isRaw;
    }

    public ShaderTexture() {
        this(false);
        this.location = new ResourceLocation("gtlib:fbm");
        if (GTLib.isRemote() && ShaderManager.allowedShader()) {
            Shader shader = Shaders.load(Shader.ShaderType.FRAGMENT, location);
            if (shader == null) return;
            this.program = new ShaderProgram();
            this.shader = shader;
            program.attach(Shaders.GUI_IMAGE_V);
            program.attach(shader);
        }
    }

    public void dispose() {
        if (isRaw && shader != null) {
            shader.deleteShader();
        }
        if (program != null) {
            program.delete();
        }
        shader = null;
        program = null;
    }

    @Override
    public ShaderTexture setColor(int color) {
        this.color = color;
        return this;
    }

    @ConfigSetter(field = "location")
    public void updateShader(ResourceLocation location) {
        if (GTLib.isRemote() && ShaderManager.allowedShader()) {
            dispose();
            Shader shader = Shaders.load(Shader.ShaderType.FRAGMENT, location);
            if (shader == null) return;
            this.program = new ShaderProgram();
            this.shader = shader;
            program.attach(Shaders.GUI_IMAGE_V);
            program.attach(shader);
        }
    }

    public void updateRawShader(String rawShader) {
        if (GTLib.isRemote() && ShaderManager.allowedShader()) {
            dispose();
            shader = new Shader(Shader.ShaderType.FRAGMENT, rawShader).compileShader();
            program = new ShaderProgram();
            program.attach(Shaders.GUI_IMAGE_V);
            program.attach(shader);
        }
    }

    public String getRawShader() {
        if (GTLib.isRemote() && ShaderManager.allowedShader() && shader !=null) {
            return shader.source;
        }
        return "";
    }

    @Environment(EnvType.CLIENT)
    private ShaderTexture(Shader shader, boolean isRaw) {
        this.isRaw = isRaw;
        if (shader == null) return;
        this.program = new ShaderProgram();
        this.shader = shader;
        program.attach(Shaders.GUI_IMAGE_V);
        program.attach(shader);
    }

    public static ShaderTexture createShader(ResourceLocation location) {
        ShaderTexture texture;
        if (GTLib.isRemote() && ShaderManager.allowedShader()) {
            Shader shader = Shaders.load(Shader.ShaderType.FRAGMENT, location);
            texture = new ShaderTexture(shader, false);
        } else {
            texture = new ShaderTexture(false);
        }
        texture.location = location;
        return texture;
    }

    public static ShaderTexture createRawShader(String rawShader) {
        if (GTLib.isRemote() && ShaderManager.allowedShader()) {
            Shader shader = new Shader(Shader.ShaderType.FRAGMENT, rawShader).compileShader();
            return new ShaderTexture(shader, true);
        } else {
            return new ShaderTexture(true);
        }
    }

    public ShaderTexture setUniformCache(Consumer<UniformCache> uniformCache) {
        this.uniformCache = uniformCache;
        return this;
    }

    public ShaderTexture setResolution(float resolution) {
        this.resolution = resolution;
        return this;
    }

    public float getResolution() {
        return resolution;
    }

    public void bindTexture(String samplerName, int id) {
        if (GTLib.isRemote() && ShaderManager.allowedShader()) {
            if (program != null) {
                program.bindTexture(samplerName, id);
            }
        }
    }

    public void bindTexture(String samplerName, ResourceLocation location) {
        if (GTLib.isRemote() && ShaderManager.allowedShader()) {
            if (program != null) {
                program.bindTexture(samplerName, location);
            }
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected void drawInternal(PoseStack stack, int mouseX, int mouseY, float x, float y, int width, int height) {
        if (program != null) {
            try {
                program.use(cache->{
                    Minecraft mc = Minecraft.getInstance();
                    float time;
                    if (mc.player != null) {
                        time = (mc.player.tickCount + mc.getFrameTime()) / 20;
                    } else {
                        time = System.currentTimeMillis() / 1000f;
                    }
                    float mX = Mth.clamp((mouseX - x), 0, width);
                    float mY = Mth.clamp((mouseY - y), 0, height);
                    cache.glUniformMatrix4F("ModelViewMat", RenderSystem.getModelViewMatrix());
                    cache.glUniformMatrix4F("ProjMat", RenderSystem.getProjectionMatrix());
                    cache.glUniform2F("iResolution", width * resolution, height * resolution);
                    cache.glUniform2F("iMouse", mX * resolution, mY * resolution);
                    cache.glUniform1F("iTime", time);
                    if (uniformCache != null) {
                        uniformCache.accept(cache);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                dispose();
                return;
            }

            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder buffer = tessellator.getBuilder();
            Matrix4f mat = stack.last().pose();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
            buffer.vertex(mat, x, y + height, 0).color(color).uv(0, 0).endVertex();
            buffer.vertex(mat, x + width, y + height, 0).color(color).uv(1, 0).endVertex();
            buffer.vertex(mat, x + width, y, 0).color(color).uv(1, 1).endVertex();
            buffer.vertex(mat, x, y, 0).color(color).uv(0, 1).endVertex();
            BufferUploader.draw(buffer.end());

            program.release();
        } else {
            DrawerHelper.drawText(stack, "Error compiling shader", x + 2, y + 2, 1, 0xffff0000);
        }
    }
}
