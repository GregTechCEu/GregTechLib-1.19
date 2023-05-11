package com.gregtechceu.gtlib.gui.widget;


import com.gregtechceu.gtlib.gui.texture.ColorBorderTexture;
import com.gregtechceu.gtlib.gui.texture.IGuiTexture;
import com.gregtechceu.gtlib.utils.Position;
import com.gregtechceu.gtlib.utils.Size;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class DraggableWidgetGroup extends WidgetGroup implements DraggableScrollableWidgetGroup.IDraggable {
    protected boolean isSelected;
    protected IGuiTexture selectedTexture;
    protected Consumer<DraggableWidgetGroup> onSelected;
    protected Consumer<DraggableWidgetGroup> onUnSelected;
    protected Consumer<DraggableWidgetGroup> onStartDrag;
    protected Consumer<DraggableWidgetGroup> onDragging;
    protected Consumer<DraggableWidgetGroup> onEndDrag;

    public DraggableWidgetGroup(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public DraggableWidgetGroup(Position position) {
        super(position);
    }

    public DraggableWidgetGroup(Position position, Size size) {
        super(position, size);
    }

    public boolean isSelected() {
        return isSelected;
    }

    public DraggableWidgetGroup setOnSelected(Consumer<DraggableWidgetGroup> onSelected) {
        this.onSelected = onSelected;
        return this;
    }

    public DraggableWidgetGroup setOnUnSelected(Consumer<DraggableWidgetGroup> onUnSelected) {
        this.onUnSelected = onUnSelected;
        return this;
    }

    public DraggableWidgetGroup setOnStartDrag(Consumer<DraggableWidgetGroup> onStartDrag) {
        this.onStartDrag = onStartDrag;
        return this;
    }

    public DraggableWidgetGroup setOnDragging(Consumer<DraggableWidgetGroup> onDragging) {
        this.onDragging = onDragging;
        return this;
    }

    public DraggableWidgetGroup setOnEndDrag(Consumer<DraggableWidgetGroup> onEndDrag) {
        this.onEndDrag = onEndDrag;
        return this;
    }

    public DraggableWidgetGroup setSelectedTexture(IGuiTexture selectedTexture) {
        this.selectedTexture = selectedTexture;
        return this;
    }

    public DraggableWidgetGroup setSelectedTexture(int border, int color) {
        this.selectedTexture = new ColorBorderTexture(border, color);
        return this;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void drawInBackground(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(matrixStack, mouseX, mouseY, partialTicks);
        if (isSelected && selectedTexture != null) {
            selectedTexture.draw(matrixStack, mouseX, mouseY, getPosition().x, getPosition().y, getSize().width, getSize().height);
        }
    }

    @Override
    public boolean allowSelected(double mouseX, double mouseY, int button) {
        return isMouseOverElement(mouseX, mouseY);
    }

    @Override
    public void onSelected() {
        isSelected = true;
        if (onSelected != null) onSelected.accept(this);
    }

    @Override
    public void onUnSelected() {
        isSelected = false;
        if (onUnSelected != null) onUnSelected.accept(this);
    }

    @Override
    public void startDrag(double mouseX, double mouseY) {
        if (onStartDrag != null) {
            onStartDrag.accept(this);
        }
    }

    @Override
    public boolean dragging(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (onDragging != null) {
            onDragging.accept(this);
        }
        return true;
    }

    @Override
    public void endDrag(double mouseX, double mouseY) {
        if (onEndDrag != null) {
            onEndDrag.accept(this);
        }
    }
}
