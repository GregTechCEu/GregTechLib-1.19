package com.gregtechceu.gtlib.gui.texture;

import com.gregtechceu.gtlib.gui.editor.ColorPattern;
import com.gregtechceu.gtlib.gui.editor.annotation.Configurable;
import com.gregtechceu.gtlib.gui.editor.annotation.NumberColor;
import com.gregtechceu.gtlib.gui.editor.annotation.NumberRange;
import com.gregtechceu.gtlib.gui.editor.annotation.RegisterUI;
import com.gregtechceu.gtlib.gui.editor.configurator.ConfiguratorGroup;
import com.gregtechceu.gtlib.gui.editor.configurator.WrapperConfigurator;
import com.gregtechceu.gtlib.gui.editor.ui.Editor;
import com.gregtechceu.gtlib.gui.widget.ButtonWidget;
import com.gregtechceu.gtlib.gui.widget.DialogWidget;
import com.gregtechceu.gtlib.gui.widget.ImageWidget;
import com.gregtechceu.gtlib.gui.widget.WidgetGroup;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

import java.io.File;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR;

/**
 * @author KilaBash
 * @date 2022/9/14
 * @implNote AnimationTexture
 */
@RegisterUI(name = "animation_texture", group = "texture")
public class AnimationTexture extends TransformTexture {

    @Configurable(name = "gtlib.gui.editor.name.resource")
    public ResourceLocation imageLocation;

    @Configurable(tips = "gtlib.gui.editor.tips.cell_size")
    @NumberRange(range = {1, Integer.MAX_VALUE})
    protected int cellSize;

    @Configurable(tips = "gtlib.gui.editor.tips.cell_from")
    @NumberRange(range = {0, Integer.MAX_VALUE})
    protected int from;

    @Configurable(tips = "gtlib.gui.editor.tips.cell_to")
    @NumberRange(range = {0, Integer.MAX_VALUE})
    protected int to;

    @Configurable(tips = "gtlib.gui.editor.tips.cell_animation")
    @NumberRange(range = {0, Integer.MAX_VALUE})
    protected int animation;

    @Configurable
    @NumberColor
    protected int color = -1;

    protected int currentFrame;

    protected int currentTime;
    private long lastTick;

    public AnimationTexture() {
        this("gtlib:textures/gui/particles.png");
        setCellSize(8).setAnimation(32,  44).setAnimation(1);
    }

    public AnimationTexture(String imageLocation) {
        this.imageLocation = new ResourceLocation(imageLocation);
    }

    public AnimationTexture(ResourceLocation imageLocation) {
        this.imageLocation = imageLocation;
    }

    public AnimationTexture copy() {
        return new AnimationTexture(imageLocation).setCellSize(cellSize).setAnimation(from, to).setAnimation(animation).setColor(color);
    }

    public AnimationTexture setCellSize(int cellSize) {
        this.cellSize = cellSize;
        return this;
    }

    public AnimationTexture setAnimation(int from, int to) {
        this.currentFrame = from;
        this.from = from;
        this.to = to;
        return this;
    }

    public AnimationTexture setAnimation(int animation) {
        this.animation = animation;
        return this;
    }

    @Override
    public AnimationTexture setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void updateTick() {
        if (Minecraft.getInstance().level != null) {
            long tick = Minecraft.getInstance().level.getGameTime();
            if (tick == lastTick) return;
            lastTick = tick;
        }
        if (currentTime >= animation) {
            currentTime = 0;
            currentFrame += 1;
        } else {
            currentTime++;
        }
        if (currentFrame > to) {
            currentFrame = from;
        } else if (currentFrame < from) {
            currentFrame = from;
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected void drawInternal(PoseStack stack, int mouseX, int mouseY, float x, float y, int width, int height) {
        float cell = 1f / this.cellSize;
        int X = currentFrame % cellSize;
        int Y = currentFrame / cellSize;

        float imageU = X * cell;
        float imageV = Y * cell;

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, imageLocation);
        Matrix4f matrix4f = stack.last().pose();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, POSITION_TEX_COLOR);
        bufferbuilder.vertex(matrix4f, x, y + height, 0).uv(imageU, imageV + cell).color(color).endVertex();
        bufferbuilder.vertex(matrix4f, x + width, y + height, 0).uv(imageU + cell, imageV + cell).color(color).endVertex();
        bufferbuilder.vertex(matrix4f, x + width, y, 0).uv(imageU + cell, imageV).color(color).endVertex();
        bufferbuilder.vertex(matrix4f, x, y, 0).uv(imageU, imageV).color(color).endVertex();
        tessellator.end();
    }

    @Override
    public void createPreview(ConfiguratorGroup father) {
        super.createPreview(father);
        WidgetGroup widgetGroup = new WidgetGroup(0, 0, 100, 100);
        ImageWidget imageWidget;
        widgetGroup.addWidget(imageWidget = new ImageWidget(0, 0, 100, 100, new GuiTextureGroup(new ResourceTexture(imageLocation.toString()), this::drawGuides)).setBorder(2, ColorPattern.T_WHITE.color));
        widgetGroup.addWidget(new ButtonWidget(0, 0, 100, 100, IGuiTexture.EMPTY, cd -> {
            if (Editor.INSTANCE == null) return;
            File path = new File(Editor.INSTANCE.getWorkSpace(), "assets/gtlib/textures");
            DialogWidget.showFileDialog(Editor.INSTANCE, "gtlib.gui.editor.tips.select_image", path, true,
                    DialogWidget.suffixFilter(".png"), r -> {
                        if (r != null && r.isFile()) {
                            imageLocation = getTextureFromFile(path, r);
                            cellSize = 1;
                            from = 0;
                            to = 0;
                            animation = 0;
                            imageWidget.setImage(new GuiTextureGroup(new ResourceTexture(imageLocation.toString()), this::drawGuides));
                        }
                    });
        }));
        WrapperConfigurator base = new WrapperConfigurator("gtlib.gui.editor.group.base_image", widgetGroup);
        base.setTips("gtlib.gui.editor.tips.click_select_image");
        father.addConfigurators(base);
    }

    private ResourceLocation getTextureFromFile(File path, File r){
        return new ResourceLocation("gtlib:" + r.getPath().replace(path.getPath(), "textures").replace('\\', '/'));
    }

    @Environment(EnvType.CLIENT)
    protected void drawGuides(PoseStack stack, int mouseX, int mouseY, float x, float y, int width, int height) {
        float cell = 1f / this.cellSize;
        int X = from % cellSize;
        int Y = from / cellSize;

        float imageU = X * cell;
        float imageV = Y * cell;

        new ColorBorderTexture(-1, 0xff00ff00).draw(stack, 0, 0,
                x + width * imageU, y + height * imageV,
                (int) (width * (cell)), (int) (height * (cell)));

        X = to % cellSize;
        Y = to / cellSize;

        imageU = X * cell;
        imageV = Y * cell;

        new ColorBorderTexture(-1, 0xffff0000).draw(stack, 0, 0,
                x + width * imageU, y + height * imageV,
                (int) (width * (cell)), (int) (height * (cell)));
    }
}
