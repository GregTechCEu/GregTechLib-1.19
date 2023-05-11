package com.gregtechceu.gtlib.gui.texture;

import com.gregtechceu.gtlib.GTLib;
import com.gregtechceu.gtlib.gui.editor.annotation.*;
import com.gregtechceu.gtlib.gui.util.DrawerHelper;
import com.gregtechceu.gtlib.utils.LocalizationUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RegisterUI(name = "text_texture", group = "texture")
public class TextTexture extends TransformTexture{

    @Configurable
    public String text;

    @Configurable
    @NumberColor
    public int color;

    @Configurable
    @NumberColor
    public int backgroundColor;

    @Configurable(tips = "gtlib.gui.editor.tips.image_text_width")
    @NumberRange(range = {1, Integer.MAX_VALUE})
    public int width;
    @Configurable
    public boolean dropShadow;

    @Configurable(tips = "gtlib.gui.editor.tips.image_text_type")
    public TextType type;

    public Supplier<String> supplier;
    @Environment(EnvType.CLIENT)
    private List<String> texts;

    private long lastTick;

    public TextTexture() {
        this("A", -1);
        setWidth(50);
    }

    public TextTexture(String text, int color) {
        this.color = color;
        this.type = TextType.NORMAL;
        if (GTLib.isClient()) {
            this.text = LocalizationUtils.format(text);
            texts = Collections.singletonList(this.text);
        }
    }

    public TextTexture(String text) {
        this(text, -1);
        setDropShadow(true);
    }

    public TextTexture setSupplier(Supplier<String> supplier) {
        this.supplier = supplier;
        return this;
    }

    @Override
    public void updateTick() {
        if (Minecraft.getInstance().level != null) {
            long tick = Minecraft.getInstance().level.getGameTime();
            if (tick == lastTick) return;
            lastTick = tick;
        }
        if (supplier != null) {
            updateText(supplier.get());
        }
    }

    @ConfigSetter(field = "text")
    public void updateText(String text) {
        if (GTLib.isClient()) {
            this.text = LocalizationUtils.format(text);
            texts = Collections.singletonList(this.text);
            setWidth(this.width);
        }
    }

    public TextTexture setBackgroundColor(int color) {
        this.backgroundColor = color;
        return this;
    }

    public TextTexture setColor(int color) {
        this.color = color;
        return this;
    }

    public TextTexture setDropShadow(boolean dropShadow) {
        this.dropShadow = dropShadow;
        return this;
    }

    public TextTexture setWidth(int width) {
        this.width = width;
        if (GTLib.isClient()) {
            if (this.width > 0) {
                texts = Minecraft.getInstance()
                        .font.getSplitter()
                        .splitLines(text, width, Style.EMPTY)
                        .stream().map(FormattedText::getString)
                        .collect(Collectors.toList());
                if (texts.size() == 0) {
                    texts = Collections.singletonList(text);
                }
            } else {
                texts = Collections.singletonList(text);
            }
        }
        return this;
    }

    public TextTexture setType(TextType type) {
        this.type = type;
        return this;
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void drawInternal(PoseStack stack, int mouseX, int mouseY, float x, float y, int width, int height) {
        if (backgroundColor != 0) {
            DrawerHelper.drawSolidRect(stack, (int) x, (int) y, width, height, backgroundColor);
        }
        stack.pushPose();
        stack.translate(0, 0, 400);
        Font fontRenderer = Minecraft.getInstance().font;
        int textH = fontRenderer.lineHeight;
        if (type == TextType.NORMAL) {
            textH *= texts.size();
            for (int i = 0; i < texts.size(); i++) {
                String resultText = texts.get(i);
                int textW = fontRenderer.width(resultText);
                float _x = x + (width - textW) / 2f;
                float _y = y + (height - textH) / 2f + i * fontRenderer.lineHeight;
                if (dropShadow) {
                    fontRenderer.drawShadow(stack, resultText, _x, _y, color);
                } else {
                    fontRenderer.draw(stack, resultText, _x, _y, color);
                }
            }
        } else if (type == TextType.HIDE) {
            String resultText = texts.get(0) + (texts.size() > 1 ? ".." : "");
            int textW = fontRenderer.width(resultText);
            float _x = x + (width - textW) / 2f;
            float _y = y + (height - textH) / 2f;
            if (dropShadow) {
                fontRenderer.drawShadow(stack, resultText, _x, _y, color);
            } else {
                fontRenderer.draw(stack, resultText, _x, _y, color);
            }
        } else if (type == TextType.ROLL || type == TextType.ROLL_ALWAYS) {
            int i = 0;
            if (type == TextType.ROLL_ALWAYS || (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height)) {
                i = (int) (Math.abs(System.currentTimeMillis() / 1000) % texts.size());
            }
            String resultText = texts.get(i);
            int textW = fontRenderer.width(resultText);
            float _x = x + (width - textW) / 2f;
            float _y = y + (height - textH) / 2f;
            if (dropShadow) {
                fontRenderer.drawShadow(stack, resultText, _x, _y, color);
            } else {
                fontRenderer.draw(stack, resultText, _x, _y, color);
            }
        } else if (type == TextType.LEFT) {
            textH *= texts.size();
            for (int i = 0; i < texts.size(); i++) {
                String resultText = texts.get(i);
                float _y = y + (height - textH) / 2f + i * fontRenderer.lineHeight;
                if (dropShadow) {
                    fontRenderer.drawShadow(stack, resultText, x, _y, color);
                } else {
                    fontRenderer.draw(stack, resultText, x, _y, color);
                }
            }
        } else if (type == TextType.RIGHT) {
            textH *= texts.size();
            for (int i = 0; i < texts.size(); i++) {
                String resultText = texts.get(i);
                int textW = fontRenderer.width(resultText);
                float _y = y + (height - textH) / 2f + i * fontRenderer.lineHeight;
                if (dropShadow) {
                    fontRenderer.drawShadow(stack, resultText, x + width - textW, _y, color);
                } else {
                    fontRenderer.draw(stack, resultText, x + width - textW, _y, color);
                }
            }
        }
        stack.popPose();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    public enum TextType{
        NORMAL,
        HIDE,
        ROLL,
        ROLL_ALWAYS,
        LEFT,
        RIGHT
    }
}
