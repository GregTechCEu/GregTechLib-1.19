package com.gregtechceu.gtlib.gui.texture;

import com.gregtechceu.gtlib.gui.editor.annotation.Configurable;
import com.gregtechceu.gtlib.gui.editor.annotation.NumberColor;
import com.gregtechceu.gtlib.gui.editor.annotation.NumberRange;
import com.gregtechceu.gtlib.gui.editor.annotation.RegisterUI;
import com.gregtechceu.gtlib.gui.util.DrawerHelper;
import com.gregtechceu.gtlib.utils.Rect;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector4f;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.awt.*;

@RegisterUI(name = "color_border_texture", group = "texture")
public class ColorBorderTexture extends TransformTexture{

    @Configurable
    @NumberColor
    public int color;

    @Configurable
    @NumberRange(range = {-100, 100})
    public int border;

    @Configurable
    @Setter
    @NumberRange(range = {0, Float.MAX_VALUE}, wheel = 1)
    public float radiusLTInner;

    @Configurable
    @Setter
    @NumberRange(range = {0, Float.MAX_VALUE}, wheel = 1)
    public float radiusLBInner;

    @Configurable
    @Setter
    @NumberRange(range = {0, Float.MAX_VALUE}, wheel = 1)
    public float radiusRTInner;

    @Configurable
    @Setter
    @NumberRange(range = {0, Float.MAX_VALUE}, wheel = 1)
    public float radiusRBInner;

    @Configurable
    @Setter
    @NumberRange(range = {0, Float.MAX_VALUE}, wheel = 1)
    public float radiusLTOuter;

    @Configurable
    @Setter
    @NumberRange(range = {0, Float.MAX_VALUE}, wheel = 1)
    public float radiusLBOuter;

    @Configurable
    @Setter
    @NumberRange(range = {0, Float.MAX_VALUE}, wheel = 1)
    public float radiusRTOuter;

    @Configurable
    @Setter
    @NumberRange(range = {0, Float.MAX_VALUE}, wheel = 1)
    public float radiusRBOuter;

    public ColorBorderTexture() {
        this(-2, 0x4f0ffddf);
    }

    public ColorBorderTexture(int border, int color) {
        this.color = color;
        this.border = border;
    }

    public ColorBorderTexture(int border, Color color) {
        this.color = color.getRGB();
        this.border = border;
    }

    public ColorBorderTexture setBorder(int border) {
        this.border = border;
        return this;
    }

    public ColorBorderTexture setColor(int color) {
        this.color = color;
        return this;
    }

    public ColorBorderTexture setRadius(float radius) {
        this.radiusLBInner = radius;
        this.radiusRTInner = radius;
        this.radiusRBInner = radius;
        this.radiusLTInner = radius;
        this.radiusLBOuter = radius;
        this.radiusRTOuter = radius;
        this.radiusRBOuter = radius;
        this.radiusLTOuter = radius;
        return this;
    }

    public ColorBorderTexture setLeftRadius(float radius) {
        setLeftRadiusInner(radius);
        setLeftRadiusOuter(radius);
        return this;
    }

    public ColorBorderTexture setRightRadius(float radius) {
        setRightRadiusInner(radius);
        setRightRadiusOuter(radius);
        return this;
    }

    public ColorBorderTexture setTopRadius(float radius) {
        setTopRadiusInner(radius);
        setTopRadiusOuter(radius);
        return this;
    }

    public ColorBorderTexture setBottomRadius(float radius) {
        setBottomRadiusInner(radius);
        setBottomRadiusOuter(radius);
        return this;
    }

    public ColorBorderTexture setLeftRadiusInner(float radius) {
        this.radiusLBInner = radius;
        this.radiusLTInner = radius;
        return this;
    }

    public ColorBorderTexture setRightRadiusInner(float radius) {
        this.radiusRTInner = radius;
        this.radiusRBInner = radius;
        return this;
    }

    public ColorBorderTexture setTopRadiusInner(float radius) {
        this.radiusRTInner = radius;
        this.radiusLTInner = radius;
        return this;
    }

    public ColorBorderTexture setBottomRadiusInner(float radius) {
        this.radiusLBInner = radius;
        this.radiusRBInner = radius;
        return this;
    }

    public ColorBorderTexture setLeftRadiusOuter(float radius) {
        this.radiusLBOuter = radius;
        this.radiusLTOuter = radius;
        return this;
    }

    public ColorBorderTexture setRightRadiusOuter(float radius) {
        this.radiusRTOuter = radius;
        this.radiusRBOuter = radius;
        return this;
    }

    public ColorBorderTexture setTopRadiusOuter(float radius) {
        this.radiusRTOuter = radius;
        this.radiusLTOuter = radius;
        return this;
    }

    public ColorBorderTexture setBottomRadiusOuter(float radius) {
        this.radiusLBOuter = radius;
        this.radiusRBOuter = radius;
        return this;
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void drawInternal(PoseStack stack, int mouseX, int mouseY, float x, float y, int width, int height) {
        if (width == 0 || height == 0) return;
        if (radiusLTInner > 0 || radiusLBInner > 0 || radiusRTInner > 0 ||radiusRBInner > 0 ||
                radiusLTOuter > 0 || radiusLBOuter > 0 || radiusRTOuter > 0 ||radiusRBOuter > 0) {
            float radius = Math.min(width, height) / 2f;
            DrawerHelper.drawFrameRoundBox(stack, Rect.ofRelative((int) x, width, (int) y, height),
                    border,
                    new Vector4f(Math.min(radius, radiusRTInner), Math.min(radiusRBInner, radius), Math.min(radius, radiusLTInner), Math.min(radius, radiusLBInner)),
                    new Vector4f(Math.min(radius, radiusRTOuter), Math.min(radiusRBOuter, radius), Math.min(radius, radiusLTOuter), Math.min(radius, radiusLBOuter)),
                    color);
        } else {
            DrawerHelper.drawBorder(stack, (int)x, (int)y, width, height, color, border);
        }
    }
}
