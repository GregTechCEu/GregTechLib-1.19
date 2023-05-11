package com.gregtechceu.gtlib.gui.texture;

import com.gregtechceu.gtlib.gui.editor.ColorPattern;
import com.gregtechceu.gtlib.gui.editor.annotation.RegisterUI;
import com.gregtechceu.gtlib.gui.editor.configurator.ConfiguratorGroup;
import com.gregtechceu.gtlib.gui.editor.configurator.IConfigurable;
import com.gregtechceu.gtlib.gui.editor.configurator.WrapperConfigurator;
import com.gregtechceu.gtlib.gui.editor.data.resource.Resource;
import com.gregtechceu.gtlib.gui.editor.runtime.PersistedParser;
import com.gregtechceu.gtlib.gui.editor.runtime.UIDetector;
import com.gregtechceu.gtlib.gui.widget.ImageWidget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.function.Function;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX;

public interface IGuiTexture extends IConfigurable {

    default IGuiTexture setColor(int color){
        return this;
    }

    default IGuiTexture rotate(float degree) {
        return this;
    }

    default IGuiTexture scale(float scale) {
        return this;
    }

    default IGuiTexture transform(int xOffset, int yOffset) {
        return this;
    }

    @Environment(EnvType.CLIENT)
    void draw(PoseStack stack, int mouseX, int mouseY, float x, float y, int width, int height);

    @Environment(EnvType.CLIENT)
    default void updateTick() { }
    
    IGuiTexture EMPTY = new IGuiTexture() {
        @Environment(EnvType.CLIENT)
        @Override
        public void draw(PoseStack stack, int mouseX, int mouseY, float x, float y, int width, int height) {

        }
    };

    IGuiTexture MISSING_TEXTURE = new IGuiTexture() {
        @Environment(EnvType.CLIENT)
        @Override
        public void draw(PoseStack stack, int mouseX, int mouseY, float x, float y, int width, int height) {
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuilder();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, TextureManager.INTENTIONAL_MISSING_TEXTURE);
            Matrix4f matrix4f = stack.last().pose();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, POSITION_TEX);
            bufferbuilder.vertex(matrix4f, x, y + height, 0).uv(0, 1).endVertex();
            bufferbuilder.vertex(matrix4f, x + width, y + height, 0).uv(1, 1).endVertex();
            bufferbuilder.vertex(matrix4f, x + width, y, 0).uv(1, 0).endVertex();
            bufferbuilder.vertex(matrix4f, x, y, 0).uv(0, 0).endVertex();
            tessellator.end();
        }
    };

    @Environment(EnvType.CLIENT)
    default void drawSubArea(PoseStack stack, float x, float y, int width, int height, float drawnU, float drawnV, float drawnWidth, float drawnHeight) {
        draw(stack, 0, 0, x, y, width, height);
    }

    // ***************** EDITOR  ***************** //

    Function<String, UIDetector.Wrapper<RegisterUI, IGuiTexture>> CACHE = Util.memoize(type -> {
        for (var wrapper : UIDetector.REGISTER_TEXTURES) {
            if (wrapper.annotation().name().equals(type)) {
                return wrapper;
            }
        }
        return null;
    });

    default void createPreview(ConfiguratorGroup father) {
        father.addConfigurators(new WrapperConfigurator("gtlib.gui.editor.group.preview",
                new ImageWidget(0, 0, 100, 100, this)
                        .setBorder(2, ColorPattern.T_WHITE.color)));
    }

    @Override
    default void buildConfigurator(ConfiguratorGroup father) {
        createPreview(father);
        IConfigurable.super.buildConfigurator(father);
    }

    @Nullable
    static CompoundTag serializeWrapper(IGuiTexture texture) {
        if (texture.isRegisterUI()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("type", texture.name());
            CompoundTag data = new CompoundTag();
            PersistedParser.serializeNBT(data, texture.getClass(), texture);
            tag.put("data", data);
            return tag;
        }
        return null;
    }

    @NotNull
    static IGuiTexture deserializeWrapper(CompoundTag tag) {
        var type = tag.getString("type");
        var data = tag.getCompound("data");
        var wrapper = CACHE.apply(type);
        IGuiTexture value = wrapper == null ? IGuiTexture.EMPTY : wrapper.creator().get();
        PersistedParser.deserializeNBT(data, new HashMap<>(), value.getClass(), value);
        return value;
    }

    default void setUIResource(Resource<IGuiTexture> texturesResource) {

    }
}
