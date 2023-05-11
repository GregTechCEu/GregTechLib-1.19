package com.gregtechceu.gtlib.client.shader.management;

import com.gregtechceu.gtlib.client.shader.Shaders;
import com.gregtechceu.gtlib.client.shader.uniform.IUniformCallback;
import com.gregtechceu.gtlib.utils.PositionedRect;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ShaderManager {

	private static final ShaderManager INSTANCE = new ShaderManager();

	public static ShaderManager getInstance() {
		return INSTANCE;
	}

	public static boolean allowedShader() {
		return true;
	}

	private final Reference2ReferenceMap<Shader, ShaderProgram> programs;

	private ShaderManager() {
		this.programs = new Reference2ReferenceOpenHashMap<>();
	}

	private static TextureTarget TEMP_TARGET;
	public static TextureTarget getTempTarget() {
		if (TEMP_TARGET == null) {
			TEMP_TARGET = new TextureTarget(1024, 1024, false, Minecraft.ON_OSX);
			TEMP_TARGET.setFilterMode(9729);
			TEMP_TARGET.setClearColor(0, 0, 0, 0);
		}
		return TEMP_TARGET;
	}

	public void reload() {
		programs.forEach((shader, shaderProgram) -> {
			shader.deleteShader();
			shaderProgram.delete();
		});
		programs.clear();
	}

	private PositionedRect viewPort;

	public void setViewPort(PositionedRect viewPort) {
		this.viewPort = viewPort;
	}

	public boolean hasViewPort() {
		return this.viewPort != null;
	}

	public void clearViewPort() {
		this.viewPort = null;
	}

	public RenderTarget renderFullImageInFramebuffer(RenderTarget fbo, Shader frag, IUniformCallback consumeCache, Consumer<ShaderProgram> programCreated) {
		if (fbo == null || frag == null || !allowedShader() || frag.shaderType != Shader.ShaderType.FRAGMENT) {
			return fbo;
		}

		fbo.bindWrite(true);
		ShaderProgram program = programs.get(frag);
		if (program == null) {
			programs.put(frag, program = new ShaderProgram());
			program.attach(Shaders.IMAGE_V).attach(frag);
			if (programCreated != null) {
				programCreated.accept(program);
			}
		}

		program.use(cache -> {
			Minecraft mc = Minecraft.getInstance();
			float time;
			if (mc.player != null) {
				time = (mc.player.tickCount + mc.getFrameTime()) / 20;
			} else {
				time = System.currentTimeMillis() / 1000f;
			}
			cache.glUniform1F("iTime", time);
			cache.glUniform2F("iResolution", fbo.width, fbo.height);
			if (consumeCache != null) {
				consumeCache.apply(cache);
			}
		});

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
		buffer.vertex(-1, 1, 0).endVertex();
		buffer.vertex(-1, -1, 0).endVertex();
		buffer.vertex(1, -1, 0).endVertex();
		buffer.vertex(1, 1, 0).endVertex();
		buffer.end();
		BufferUploader.draw(buffer.end());

		program.release();

		if (viewPort != null) {
			RenderSystem.viewport(viewPort.position.x, viewPort.position.y, viewPort.size.width, viewPort.size.height);
		}
		return fbo;
	}

}
