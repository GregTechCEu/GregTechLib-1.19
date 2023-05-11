package com.gregtechceu.gtlib.gui.widget;


import com.gregtechceu.gtlib.gui.editor.annotation.Configurable;
import com.gregtechceu.gtlib.gui.editor.annotation.NumberColor;
import com.gregtechceu.gtlib.gui.editor.annotation.NumberRange;
import com.gregtechceu.gtlib.gui.editor.annotation.RegisterUI;
import com.gregtechceu.gtlib.gui.editor.configurator.IConfigurableWidget;
import com.gregtechceu.gtlib.gui.texture.IGuiTexture;
import com.gregtechceu.gtlib.gui.texture.ResourceTexture;
import com.gregtechceu.gtlib.gui.util.DrawerHelper;
import com.gregtechceu.gtlib.utils.Position;
import com.gregtechceu.gtlib.utils.Size;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import javax.annotation.Nonnull;

@Configurable(name = "gtlib.gui.editor.register.widget.image", collapse = false)
@RegisterUI(name = "image", group = "widget.basic")
public class ImageWidget extends Widget implements IConfigurableWidget {

    @Configurable(name = "gtlib.gui.editor.name.border")
    @NumberRange(range = {-100, 100})
    private int border;
    @Configurable(name = "gtlib.gui.editor.name.border_color")
    @NumberColor
    private int borderColor = -1;

    public ImageWidget() {
        this(0, 0, 50, 50, new ResourceTexture());
    }

    public ImageWidget(int xPosition, int yPosition, int width, int height) {
        super(xPosition, yPosition, width, height);
    }

    public ImageWidget(int xPosition, int yPosition, int width, int height, IGuiTexture area) {
        this(xPosition, yPosition, width, height);
        setImage(area);
    }

    public ImageWidget setImage(IGuiTexture area) {
        setBackground(area);
        return this;
    }

    public IGuiTexture getImage() {
        return backgroundTexture;
    }

    public ImageWidget setBorder(int border, int color) {
        this.border = border;
        this.borderColor = color;
        return this;
    }

    @Environment(EnvType.CLIENT)
    public void drawInBackground(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(matrixStack, mouseX, mouseY, partialTicks);
        Position position = getPosition();
        Size size = getSize();
        if (border > 0) {
            DrawerHelper.drawBorder(matrixStack, position.x, position.y, size.width, size.height, borderColor, border);
        }
    }
}

