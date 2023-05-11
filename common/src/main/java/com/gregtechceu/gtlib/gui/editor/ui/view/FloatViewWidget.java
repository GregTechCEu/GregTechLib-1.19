package com.gregtechceu.gtlib.gui.editor.ui.view;

import com.gregtechceu.gtlib.gui.editor.ColorPattern;
import com.gregtechceu.gtlib.gui.editor.IRegisterUI;
import com.gregtechceu.gtlib.gui.editor.ui.Editor;
import com.gregtechceu.gtlib.gui.texture.GuiTextureGroup;
import com.gregtechceu.gtlib.gui.texture.IGuiTexture;
import com.gregtechceu.gtlib.gui.util.ClickData;
import com.gregtechceu.gtlib.gui.widget.ButtonWidget;
import com.gregtechceu.gtlib.gui.widget.WidgetGroup;
import com.gregtechceu.gtlib.utils.Size;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * @author KilaBash
 * @date 2022/12/17
 * @implNote FloatViewWidget, view are some float window widgets.
 * They are technically project-independent and can be used for any project.
 */
public class FloatViewWidget extends WidgetGroup implements IRegisterUI {
    protected final Editor editor;
    protected final boolean isFixedView;
    protected WidgetGroup title, content;
    private boolean isDragging, isCollapse;
    private double lastDeltaX, lastDeltaY;

    public FloatViewWidget(int x, int y, int width, int height, boolean isFixedView) {
        super(x, y, width, height + 15);
        this.editor = Editor.INSTANCE;
        if (this.editor == null) {
            throw new RuntimeException("editor is null while creating a float view %s".formatted(name()));
        }
        this.isFixedView = isFixedView;
        setClientSideWidget();
    }

    @Override
    public void initWidget() {
        if (!isFixedView) {
            addWidget(title = new WidgetGroup(0, 0, getSize().width, 15));
            title.setBackground(new GuiTextureGroup(ColorPattern.T_RED.rectTexture().setTopRadius(5f), ColorPattern.GRAY.borderTexture(-1).setTopRadius(5f)));
            title.addWidget(new ButtonWidget(2, 2, 11, 11, getIcon(), this::collapse).setHoverTexture(getIcon().setColor(ColorPattern.GREEN.color)));
            addWidget(content = new WidgetGroup(0, 15, getSize().width, getSize().height - 15));
            content.setBackground(new GuiTextureGroup(ColorPattern.BLACK.rectTexture().setBottomRadius(5f), ColorPattern.GRAY.borderTexture(-1).setBottomRadius(5f)));
        }
        super.initWidget();
    }

    private void collapse(ClickData clickData) {
        isCollapse = !isCollapse;
        if (isCollapse) {
            title.setSize(new Size(15, 15));
            title.setBackground(new GuiTextureGroup(ColorPattern.T_RED.rectTexture().setRadius(5f), ColorPattern.GRAY.borderTexture(-1).setRadius(5f)));
            content.setVisible(false);
            content.setActive(false);
        } else {
            title.setSize(new Size(getSize().width, 15));
            title.setBackground(new GuiTextureGroup(ColorPattern.T_RED.rectTexture().setTopRadius(5f), ColorPattern.GRAY.borderTexture(-1).setTopRadius(5f)));
            content.setVisible(true);
            content.setActive(true);
        }
    }

    public IGuiTexture getIcon() {
        return IGuiTexture.EMPTY;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        isDragging = false;
        if (title != null && title.isMouseOverElement(mouseX, mouseY)) {
            lastDeltaX = 0;
            lastDeltaY = 0;
            isDragging = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            double dx = dragX + lastDeltaX;
            double dy = dragY + lastDeltaY;
            dragX = (int) dx;
            dragY = (int) dy;
            lastDeltaX = dx- dragX;
            lastDeltaY = dy - dragY;
            addSelfPosition((int) dragX, (int) dragY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
