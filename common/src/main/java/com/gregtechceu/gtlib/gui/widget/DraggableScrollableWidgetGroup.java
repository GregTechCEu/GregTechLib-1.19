package com.gregtechceu.gtlib.gui.widget;

import com.gregtechceu.gtlib.client.utils.RenderUtils;
import com.gregtechceu.gtlib.gui.editor.ColorPattern;
import com.gregtechceu.gtlib.gui.editor.annotation.ConfigSetter;
import com.gregtechceu.gtlib.gui.editor.annotation.Configurable;
import com.gregtechceu.gtlib.gui.editor.annotation.NumberRange;
import com.gregtechceu.gtlib.gui.editor.annotation.RegisterUI;
import com.gregtechceu.gtlib.gui.texture.IGuiTexture;
import com.gregtechceu.gtlib.utils.Position;
import com.gregtechceu.gtlib.utils.Size;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;
import java.util.List;

@RegisterUI(name = "draggable_scrollable_group", group = "widget.group")
public class DraggableScrollableWidgetGroup extends WidgetGroup {
    protected int scrollXOffset;
    protected int scrollYOffset;
    @Configurable
    @NumberRange(range = {0, Integer.MAX_VALUE})
    protected int xBarHeight;
    @Configurable
    @NumberRange(range = {0, Integer.MAX_VALUE})
    protected int yBarWidth;
    @Configurable
    protected boolean draggable;
    @Configurable
    protected boolean useScissor;
    protected int maxHeight;
    protected int maxWidth;
    @Configurable(name = "X Bar Background")
    protected IGuiTexture xBarB;
    @Configurable(name = "X Bar Texture")
    protected IGuiTexture xBarF;
    @Configurable(name = "Y Bar Background")
    protected IGuiTexture yBarB;
    @Configurable(name = "Y Bar Texture")
    protected IGuiTexture yBarF;
    protected Widget draggedWidget;
    protected Widget selectedWidget;
    protected boolean isComputingMax;
    private boolean draggedPanel;
    private boolean draggedOnXScrollBar;
    private boolean draggedOnYScrollBar;
    private double lastDeltaX, lastDeltaY;

    public DraggableScrollableWidgetGroup() {
        this(0, 0,50, 50);
        setBackground(ColorPattern.RED.rectTexture());
        setYScrollBarWidth(4).setYBarStyle(ColorPattern.RED.rectTexture(), ColorPattern.WHITE.rectTexture().setRadius(2));
    }

    public DraggableScrollableWidgetGroup(int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
        maxHeight = height;
        maxWidth = width;
        useScissor = true;
    }

    @ConfigSetter(field = "xBarHeight")
    public DraggableScrollableWidgetGroup setXScrollBarHeight(int xBar) {
        this.xBarHeight = xBar;
        computeMax();
        return this;
    }

    @ConfigSetter(field = "yBarWidth")
    public DraggableScrollableWidgetGroup setYScrollBarWidth(int yBar) {
        this.yBarWidth = yBar;
        computeMax();
        return this;
    }

    public DraggableScrollableWidgetGroup setDraggable(boolean draggable) {
        this.draggable = draggable;
        return this;
    }

    public DraggableScrollableWidgetGroup setBackground(IGuiTexture background) {
        super.setBackground(background);
        return this;
    }

    public DraggableScrollableWidgetGroup setXBarStyle(IGuiTexture background, IGuiTexture bar) {
        this.xBarB = background;
        this.xBarF = bar;
        return this;
    }

    public DraggableScrollableWidgetGroup setYBarStyle(IGuiTexture background, IGuiTexture bar) {
        this.yBarB = background;
        this.yBarF = bar;
        return this;
    }

    public void setUseScissor(boolean useScissor) {
        this.useScissor = useScissor;
    }

    public int getScrollYOffset() {
        return scrollYOffset;
    }

    public int getScrollXOffset() {
        return scrollXOffset;
    }

    @Override
    public WidgetGroup addWidget(int index, Widget widget) {
        maxHeight = Math.max(maxHeight, widget.getSize().height + widget.getSelfPosition().y);
        maxWidth = Math.max(maxWidth, widget.getSize().width + widget.getSelfPosition().x);
        Position newPos = widget.addSelfPosition(- scrollXOffset, - scrollYOffset);
        widget.setVisible(newPos.x < getSize().width - yBarWidth && newPos.x + widget.getSize().width > 0);
        widget.setVisible(newPos.y < getSize().height - xBarHeight && newPos.y + widget.getSize().height > 0);
        return super.addWidget(index, widget);
    }

    @Override
    public void removeWidget(Widget widget) {
        super.removeWidget(widget);
        computeMax();
        if (widget == draggedWidget) draggedWidget = null;
        if (widget == selectedWidget) selectedWidget = null;
    }

    @Override
    public void clearAllWidgets() {
        super.clearAllWidgets();
        maxHeight = getSize().height;
        maxWidth = getSize().width;
        scrollXOffset = 0;
        scrollYOffset = 0;
        draggedWidget = null;
        selectedWidget = null;
    }

    @Override
    @ConfigSetter(field = "size")
    public void setSize(Size size) {
        super.setSize(size);
        maxHeight = Math.max(size.height, maxHeight);
        maxWidth = Math.max(size.width, maxWidth);
//        computeMax();
        for (Widget widget : widgets) {
            Position newPos = widget.getSelfPosition();
            widget.setVisible(newPos.x < getSize().width - yBarWidth && newPos.x + widget.getSize().width > 0);
            widget.setVisible(newPos.y < getSize().height - xBarHeight && newPos.y + widget.getSize().height > 0);
        }
    }

    @Override
    protected void onChildSelfPositionUpdate(Widget child) {
        if (!isComputingMax && isInitialized()) {
            computeMax();
        }
    }

    @Override
    protected void onChildSizeUpdate(Widget child) {
        if (!isComputingMax && isInitialized()) {
            computeMax();
        }
    }

    public void computeMax() {
        if (isComputingMax) return;
        isComputingMax = true;
        int mh = 0;
        int mw = 0;
        for (Widget widget : widgets) {
            mh = Math.max(mh, widget.getSize().height + widget.getSelfPosition().y + scrollYOffset);
            mw = Math.max(mw, widget.getSize().width + widget.getSelfPosition().x + scrollXOffset);
        }
        int offsetY = 0;
        int offsetX = 0;
        if (mh > getSize().height) {
            offsetY = maxHeight - mh;
            maxHeight = mh;
            if (scrollYOffset - offsetY < 0) {
                offsetY = scrollYOffset;
            }
            scrollYOffset -= offsetY;
        } else if (mh < getSize().height) {
            offsetY = maxHeight - getSize().height;
            maxHeight = getSize().height;
            if (scrollYOffset - offsetY < 0) {
                offsetY = scrollYOffset;
            }
            scrollYOffset -= offsetY;
        }
        if (mw > getSize().width) {
            offsetX = maxWidth - mw;
            maxWidth = mw;
            if (scrollXOffset - offsetX < 0) {
                offsetX = scrollXOffset;
            }
            scrollXOffset -= offsetX;
        }else if (mw < getSize().width) {
            offsetX = maxWidth - getSize().width;
            maxWidth = getSize().width;
            if (scrollXOffset - offsetX < 0) {
                offsetX = scrollXOffset;
            }
            scrollXOffset -= offsetX;
        }
        for (Widget widget : widgets) {
            Position newPos = widget.addSelfPosition(offsetX, offsetY);
            widget.setVisible(newPos.x < getSize().width - yBarWidth && newPos.x + widget.getSize().width > 0);
            widget.setVisible(newPos.y < getSize().height - xBarHeight && newPos.y + widget.getSize().height > 0);
        }
        isComputingMax = false;
    }

    protected int getMaxHeight() {
        return maxHeight + xBarHeight;
    }

    protected int getMaxWidth() {
        return maxWidth + yBarWidth;
    }

    public int getWidgetBottomHeight() {
        int y = 0;
        for (Widget widget : widgets) {
            y = Math.max(y, widget.getSize().height + widget.getSelfPosition().y);
        }
        return y;
    }

    public void setScrollXOffset(int scrollXOffset) {
        if (scrollXOffset == this.scrollXOffset) return;
        if (scrollXOffset < 0) scrollXOffset = 0;
        int offset = scrollXOffset - this.scrollXOffset;
        this.scrollXOffset = scrollXOffset;
        isComputingMax = true;
        for (Widget widget : widgets) {
            Position newPos = widget.addSelfPosition( - offset, 0);
            widget.setVisible(newPos.x < getSize().width - yBarWidth && newPos.x + widget.getSize().width > 0);
        }
        isComputingMax = false;
    }

    public void setScrollYOffset(int scrollYOffset) {
        if (scrollYOffset == this.scrollYOffset) return;
        if (scrollYOffset < 0) scrollYOffset = 0;
        int offset = scrollYOffset - this.scrollYOffset;
        this.scrollYOffset = scrollYOffset;
        isComputingMax = true;
        for (Widget widget : widgets) {
            Position newPos = widget.addSelfPosition(0, - offset);
            widget.setVisible(newPos.y < getSize().height - xBarHeight && newPos.y + widget.getSize().height > 0);
        }
        isComputingMax = false;
    }

    private boolean isOnXScrollPane(double mouseX, double mouseY) {
        Position pos = getPosition();
        Size size = getSize();
        return isMouseOver(pos.x, pos.y + size.height - xBarHeight, size.width, xBarHeight, mouseX, mouseY);
    }

    private boolean isOnYScrollPane(double mouseX, double mouseY) {
        Position pos = getPosition();
        Size size = getSize();
        return isMouseOver(pos.x + size.width - yBarWidth, pos.y, yBarWidth, size.height, mouseX, mouseY);
    }

    @Environment(EnvType.CLIENT)
    protected boolean hookDrawInBackground(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        return false;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void drawInForeground(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (isMouseOverElement(mouseX, mouseY)) {
            super.drawInForeground(matrixStack, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void drawInBackground(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        int x = getPosition().x;
        int y = getPosition().y;
        int width = getSize().width;
        int height = getSize().height;
        if (useScissor) {
            RenderUtils.useScissor(matrixStack, x, y, width, height, ()->{
                if(!hookDrawInBackground(matrixStack, mouseX, mouseY, partialTicks)) {
                    super.drawInBackground(matrixStack, mouseX, mouseY, partialTicks);
                }
            });
        } else {
            if(!hookDrawInBackground(matrixStack, mouseX, mouseY, partialTicks)) {
                super.drawInBackground(matrixStack, mouseX, mouseY, partialTicks);
            }
        }

        if (xBarHeight > 0) {
            if (xBarB != null) {
                xBarB.draw(matrixStack, mouseX, mouseY, x, y + height - xBarHeight, width, xBarHeight);
            }
            if (xBarF != null) {
                int barWidth = (int) (width * 1.0f / getMaxWidth() * width);
                xBarF.draw(matrixStack, mouseX, mouseY, x + scrollXOffset * width * 1.0f / getMaxWidth(), y + height - xBarHeight, barWidth, xBarHeight);
            }
        }
        if (yBarWidth > 0) {
            if (yBarB != null) {
                yBarB.draw(matrixStack, mouseX, mouseY, x + width  - yBarWidth, y, yBarWidth, height);
            }
            if (yBarF != null) {
                int barHeight = (int) (height * 1.0f / getMaxHeight() * height);
                yBarF.draw(matrixStack, mouseX, mouseY, x + width  - yBarWidth, y + scrollYOffset * height * 1.0f / getMaxHeight(), yBarWidth, barHeight);
            }
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        lastDeltaX = 0;
        lastDeltaY = 0;
        if (xBarHeight > 0 && isOnXScrollPane(mouseX, mouseY)) {
            this.draggedOnXScrollBar = true;
            setFocus(true);
            return true;
        }
        else if (yBarWidth > 0 && isOnYScrollPane(mouseX, mouseY)) {
            this.draggedOnYScrollBar = true;
            setFocus(true);
            return true;
        } else if(isMouseOverElement(mouseX, mouseY)){
            if (checkClickedDragged(mouseX, mouseY, button)) {
                setFocus(true);
                return true;
            }
            setFocus(true);
            if (draggable) {
                this.draggedPanel = true;
                return true;
            }
            return false;
        }
        setFocus(false);
        return false;
    }

    @Environment(EnvType.CLIENT)
    protected boolean checkClickedDragged(double mouseX, double mouseY, int button) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if(widget.isVisible()) {
                boolean result = widget.mouseClicked(mouseX, mouseY, button);
                if (waitToRemoved == null || !waitToRemoved.contains(widget))  {
                    if (widget instanceof IDraggable && ((IDraggable) widget).allowDrag(mouseX, mouseY, button)) {
                        draggedWidget = widget;
                        ((IDraggable) widget).startDrag(mouseX, mouseY);
                        if (selectedWidget != null && selectedWidget != widget) {
                            ((ISelected) selectedWidget).onUnSelected();
                        }
                        selectedWidget = widget;
                        ((ISelected) selectedWidget).onSelected();
                        return true;
                    }
                    if (widget instanceof ISelected && ((ISelected) widget).allowSelected(mouseX, mouseY, button)) {
                        if (selectedWidget != null && selectedWidget != widget) {
                            ((ISelected) selectedWidget).onUnSelected();
                        }
                        selectedWidget = widget;
                        ((ISelected) selectedWidget).onSelected();
                        return true;
                    }
                }
                if (result) return true;
            }
        }
        draggedWidget = null;
        return false;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            if (super.mouseWheelMove(mouseX, mouseY, wheelDelta)) {
                setFocus(true);
                return true;
            }
            setFocus(true);
            if (isFocus()) {
                int moveDelta = (int) (-Mth.clamp(wheelDelta, -1, 1) * 13);
                if (getMaxHeight() - getSize().height > 0 || scrollYOffset > getMaxHeight() - getSize().height) {
                    setScrollYOffset(Mth.clamp(scrollYOffset + moveDelta, 0, getMaxHeight() - getSize().height));
                }
            }
            return true;
        }
        setFocus(false);
        return false;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        double dx = deltaX + lastDeltaX;
        double dy = deltaY + lastDeltaY;
        deltaX = (int) dx;
        deltaY = (int) dy;
        lastDeltaX = dx- deltaX;
        lastDeltaY = dy - deltaY;
        if (draggedOnXScrollBar && (getMaxWidth() - getSize().width > 0 || scrollYOffset > getMaxWidth() - getSize().width)) {
            setScrollXOffset((int) Mth.clamp(scrollXOffset + deltaX * getMaxWidth() / getSize().width, 0, getMaxWidth() - getSize().width));
            return true;
        } else if (draggedOnYScrollBar && (getMaxHeight() - getSize().height > 0 || scrollYOffset > getMaxHeight() - getSize().height)) {
            setScrollYOffset((int) Mth.clamp(scrollYOffset + deltaY * getMaxHeight() / getSize().height, 0, getMaxHeight() - getSize().height));
            return true;
        } else if (draggedWidget != null) {
            if (((IDraggable)draggedWidget).dragging(mouseX, mouseY, deltaX, deltaY)) {
                if (draggedWidget.getPosition().x < getPosition().x) {
                    deltaX = getPosition().x - draggedWidget.getPosition().x;
                } else if (draggedWidget.getPosition().x + draggedWidget.getSize().width + scrollXOffset > getPosition().x + getSize().width) {
                    deltaX = (getPosition().x + getSize().width) - (draggedWidget.getPosition().x + draggedWidget.getSize().width + scrollXOffset);
                }
                if (draggedWidget.getPosition().y < getPosition().y) {
                    deltaY = getPosition().y - draggedWidget.getPosition().y;
                } else if (draggedWidget.getPosition().y + draggedWidget.getSize().height + scrollYOffset > getPosition().y + getSize().height) {
                    deltaY = (getPosition().y + getSize().height) - (draggedWidget.getPosition().y + draggedWidget.getSize().height + scrollYOffset);
                }
                isComputingMax = true;
                draggedWidget.addSelfPosition((int) deltaX, (int) deltaY);
                isComputingMax = false;
            }
            computeMax();
            return true;
        } else if (draggedPanel) {
            setScrollXOffset((int) Mth.clamp(scrollXOffset - deltaX, 0, Math.max(getMaxWidth() - yBarWidth - getSize().width, 0)));
            setScrollYOffset((int) Mth.clamp(scrollYOffset - deltaY, 0, Math.max(getMaxHeight() - xBarHeight - getSize().height, 0)));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggedOnXScrollBar) {
            draggedOnXScrollBar = false;
        } else if (draggedOnYScrollBar) {
            draggedOnYScrollBar = false;
        } else if (draggedWidget != null) {
            ((IDraggable)draggedWidget).endDrag(mouseX, mouseY);
            draggedWidget = null;
        } else if (draggedPanel) {
            draggedPanel = false;
        } else {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        return true;
    }

    @Environment(EnvType.CLIENT)
    public List<Rect2i> getGuiExtraAreas(Rect2i guiRect, List<Rect2i> list) {
        Rect2i rect2i = toRectangleBox();
        if (rect2i.getX() < guiRect.getX()
                || rect2i.getX() + rect2i.getWidth() >  guiRect.getX() + guiRect.getWidth()
                || rect2i.getY() < guiRect.getY()
                || rect2i.getY() + rect2i.getHeight() >  guiRect.getY() + guiRect.getHeight()){
            list.add(toRectangleBox());
        }
        return list;
    }

    public void setSelected(Widget widget) {
        if (widget instanceof ISelected) {
            if (selectedWidget != null && selectedWidget != widget) {
                ((ISelected) selectedWidget).onUnSelected();
            }
            selectedWidget = widget;
            ((ISelected) selectedWidget).onSelected();
        }
    }

    @Override
    public CompoundTag serializeInnerNBT() {
        CompoundTag tag = super.serializeInnerNBT();
        tag.putInt("scrollXOffset", scrollXOffset);
        tag.putInt("scrollYOffset", scrollYOffset);
        tag.putInt("maxHeight", maxHeight);
        tag.putInt("maxWidth", maxWidth);
        return tag;
    }

    @Override
    public void deserializeInnerNBT(CompoundTag nbt) {
        super.deserializeInnerNBT(nbt);
        this.scrollXOffset= nbt.getInt("scrollXOffset");
        this.scrollYOffset= nbt.getInt("scrollYOffset");
        this.maxHeight= nbt.getInt("maxHeight");
        this.maxWidth= nbt.getInt("maxWidth");
        isComputingMax = true;
        for (Widget widget : widgets) {
            widget.addSelfPosition(-scrollXOffset, -scrollYOffset);
        }
        isComputingMax = false;
    }

    public interface IDraggable extends ISelected {
        default boolean allowDrag(double mouseX, double mouseY, int button) {
            return allowSelected(mouseX, mouseY, button);
        }
        default void startDrag(double mouseX, double mouseY) {}
        default boolean dragging(double mouseX, double mouseY, double deltaX, double deltaY) {return true;}
        default void endDrag(double mouseX, double mouseY) {}
    }

    public interface ISelected {
        boolean allowSelected(double mouseX, double mouseY, int button);
        default void onSelected() {}
        default void onUnSelected() {}
    }
}
