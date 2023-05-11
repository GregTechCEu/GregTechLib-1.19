package com.gregtechceu.gtlib.gui.widget;

import com.gregtechceu.gtlib.gui.editor.annotation.ConfigSetter;
import com.gregtechceu.gtlib.gui.editor.annotation.Configurable;
import com.gregtechceu.gtlib.gui.editor.annotation.NumberColor;
import com.gregtechceu.gtlib.gui.editor.annotation.RegisterUI;
import com.gregtechceu.gtlib.gui.editor.configurator.IConfigurableWidget;
import com.gregtechceu.gtlib.utils.LocalizationUtils;
import com.gregtechceu.gtlib.utils.Position;
import com.gregtechceu.gtlib.utils.Size;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@Configurable(name = "gtlib.gui.editor.register.widget.text_box", collapse = false)
@RegisterUI(name = "text_box", group = "widget.basic")
public class TextBoxWidget extends Widget implements IConfigurableWidget {

    // config
    @Configurable
    public final List<String> content = new ArrayList<>();

    @Configurable
    public int space = 1;

    @Configurable
    public int fontSize = 9;

    @Configurable
    @NumberColor
    public int fontColor = 0xff000000;

    @Configurable
    public boolean isShadow = false;

    @Configurable
    public boolean isCenter = false;

    private transient List<String> textLines;

    public TextBoxWidget() {
        this(0, 0, 60, List.of("gtlib.author", "Lorem ipsum"));
        setFontColor(-1);
    }

    public TextBoxWidget(int x, int y, int width, List<String> content) {
        super(x, y, width, 0);
        this.content.addAll(content);
        this.calculate();
    }

    @Override
    @ConfigSetter(field = "size")
    public void setSize(Size size) {
        int lastWidth = getSize().width;
        super.setSize(size);
        if (size.width != lastWidth) {
            calculate();
        }
    }

    @ConfigSetter(field = "content")
    public TextBoxWidget setContent(List<String> content) {
        if (this.content != content) {
            this.content.clear();
            this.content.addAll(content);
        }
        this.calculate();
        return this;
    }

    @ConfigSetter(field = "space")
    public TextBoxWidget setSpace(int space) {
        this.space = space;
        this.calculate();
        return this;
    }

    @ConfigSetter(field = "fontSize")
    public TextBoxWidget setFontSize(int fontSize) {
        this.fontSize = fontSize;
        this.calculate();
        return this;
    }

    public TextBoxWidget setFontColor(int fontColor) {
        this.fontColor = fontColor;
        return this;
    }

    public TextBoxWidget setShadow(boolean shadow) {
        isShadow = shadow;
        return this;
    }

    public TextBoxWidget setCenter(boolean center) {
        isCenter = center;
        return this;
    }

    public int getMaxContentWidth() {
        return content.stream().mapToInt(Minecraft.getInstance().font::width).max().orElse(0);
    }

    protected void calculate() {
        if (isRemote()) {
            this.textLines = new ArrayList<>();
            Font font = Minecraft.getInstance().font;
            this.space = Math.max(space, 0);
            this.fontSize = Math.max(fontSize, 1);
            int wrapWidth = getSize().width * font.lineHeight / fontSize;
            for (String textLine : content) {
                this.textLines.addAll(font.getSplitter()
                        .splitLines(LocalizationUtils.format(textLine), wrapWidth, Style.EMPTY)
                        .stream().map(FormattedText::getString).toList());
            }
            this.setSize(new Size(this.getSize().width, this.textLines.size() * (fontSize + space)));
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void drawInBackground(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(matrixStack, mouseX, mouseY, partialTicks);
        if (!textLines.isEmpty()) {
            Position position = getPosition();
            Size size = getSize();
            Font font = Minecraft.getInstance().font;
            float scale = fontSize * 1.0f / font.lineHeight;
            matrixStack.pushPose();
            matrixStack.scale(scale, scale, 1);
            matrixStack.translate(position.x / scale, position.y / scale, 0);
            float x = 0;
            float y = 0;
            float ySpace = font.lineHeight + space / scale;
            for (String textLine : textLines) {
                if (isCenter) {
                    x = (size.width / scale - font.width(textLine)) / 2;
                }
                if (!isShadow) {
                    font.draw(matrixStack, textLine, x, y, fontColor);
                } else {
                    font.drawShadow(matrixStack, textLine, x, y, fontColor);
                }
                y += ySpace;
            }
            matrixStack.popPose();
        }
    }

    @Override
    public boolean handleDragging(Object dragging) {
        if (dragging instanceof String string) {
            List<String> list = new ArrayList<>();
            list.add(string);
            setContent(list);
            return true;
        } else return IConfigurableWidget.super.handleDragging(dragging);
    }
}