package com.gregtechceu.gtlib.gui.editor.ui;

import com.gregtechceu.gtlib.gui.editor.configurator.ConfiguratorGroup;
import com.gregtechceu.gtlib.gui.editor.configurator.IConfigurable;
import com.gregtechceu.gtlib.gui.editor.configurator.IConfigurableWidget;
import com.gregtechceu.gtlib.gui.editor.configurator.IConfigurableWidgetGroup;
import com.gregtechceu.gtlib.gui.editor.ui.tool.WidgetToolBox;
import com.gregtechceu.gtlib.gui.texture.ColorBorderTexture;
import com.gregtechceu.gtlib.gui.texture.IGuiTexture;
import com.gregtechceu.gtlib.gui.texture.WidgetTexture;
import com.gregtechceu.gtlib.gui.widget.Widget;
import com.gregtechceu.gtlib.utils.Position;
import com.gregtechceu.gtlib.utils.Size;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Arrays;

/**
 * @author KilaBash
 * @date 2022/12/6
 * @implNote UIWrapper
 */
public record UIWrapper(MainPanel panel, IConfigurableWidget inner) implements IConfigurable {

    public boolean isSelected() {
        return panel.getSelectedUIs().contains(this);
    }

    public boolean isHover() {
        return panel.getHoverUI() == this;
    }

    public boolean checkAcceptable(UIWrapper uiWrapper) {
        return inner instanceof IConfigurableWidgetGroup group && group.canWidgetAccepted(uiWrapper.inner);
    }

    @Environment(EnvType.CLIENT)
    public void drawInBackground(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        Position pos = inner.widget().getPosition();
        Size size = inner.widget().getSize();
        // render border
        int borderColor = 0;
        if (isSelected()) {
            borderColor = 0xffff0000;
        }
        if (isHover()) {
            if (!isSelected()) {
                borderColor = 0x4f0000ff;
            }
            var dragging = panel.getGui().getModularUIGui().getDraggingElement();
            boolean drawDragging = false;

            if (inner.canDragIn(dragging)) {
                drawDragging = true;
            } else if (dragging instanceof WidgetToolBox.IWidgetPanelDragging widgetPanelDragging && checkAcceptable(new UIWrapper(panel, widgetPanelDragging.get()))) {
                drawDragging = true;
            } else if (dragging instanceof UIWrapper[] uiWrappers && Arrays.stream(uiWrappers).allMatch(this::checkAcceptable)) { // can accept
                drawDragging = true;
            }

            if (drawDragging) {
                borderColor = 0xff55aa55;
            }
        }
        if (borderColor != 0) {
            new ColorBorderTexture(1, borderColor).draw(poseStack, mouseX, mouseY, pos.x, pos.y, size.width, size.height);
        }
    }

    @Environment(EnvType.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isHover()) {
            var dragging = panel.getGui().getModularUIGui().getDraggingElement();

            if (dragging instanceof WidgetToolBox.IWidgetPanelDragging widgetPanelDragging) {
                UIWrapper uiWrapper = new UIWrapper(panel, widgetPanelDragging.get());
                if (inner instanceof IConfigurableWidgetGroup group && checkAcceptable(uiWrapper)) {
                    var parent = uiWrapper.inner.widget().getParent(); // remove from original parent

                    if (parent != null) {
                        parent.onWidgetRemoved(uiWrapper.inner);
                    }

                    // accept it with correct position
                    Position position = new Position((int) mouseX, (int) mouseY).subtract(group.widget().getPosition());
                    uiWrapper.inner.widget().setSelfPosition(new Position(
                            position.x - uiWrapper.inner.widget().getSize().width / 2,
                            position.y - uiWrapper.inner.widget().getSize().height / 2));
                    group.acceptWidget(uiWrapper.inner);
                    return true;
                }
            } else if (inner instanceof IConfigurableWidgetGroup group && dragging instanceof UIWrapper[] uiWrappers && Arrays.stream(uiWrappers).allMatch(this::checkAcceptable)) {
                for (UIWrapper uiWrapper : uiWrappers) {
                    var parent = uiWrapper.inner.widget().getParent(); // remove from original parent

                    if (parent != null) {
                        parent.onWidgetRemoved(uiWrapper.inner);
                    }

                    // accept it with correct position
                    Position position = new Position((int) mouseX, (int) mouseY).subtract(inner.widget().getPosition());
                    uiWrapper.inner.widget().setSelfPosition(new Position(
                            position.x - uiWrapper.inner.widget().getSize().width / 2,
                            position.y - uiWrapper.inner.widget().getSize().height / 2));
                    group.acceptWidget(uiWrapper.inner);
                }

                return true;
            } else {
                return inner.handleDragging(dragging);
            }
        }
        return false;
    }

    @Override
    public void buildConfigurator(ConfiguratorGroup father) {
        if (inner.isRegisterUI()) {
            ConfiguratorGroup common = new ConfiguratorGroup(inner.getTranslateKey(), false);
            common.setCanCollapse(false);
            father.addConfigurators(common);
            father = common;
        }
        inner.buildConfigurator(father);
    }

    public void remove() {
        var parent = inner.widget().getParent();
        if (inner.widget() != panel.root) {
            parent.waitToRemoved(inner.widget());
        }
    }

    public void onDragPosition(int deltaX, int deltaY) {
        inner().widget().addSelfPosition(deltaX, deltaY);
    }

    public void onDragSize(int deltaX, int deltaY) {
        Widget selected = inner().widget();
        selected.setSize(new Size(selected.getSize().width + deltaX, selected.getSize().getHeight() + deltaY));
    }

    public boolean is(IConfigurableWidget configurableWidget) {
        return inner == configurableWidget;
    }

    public IGuiTexture toDraggingTexture(int mouseX, int mouseY) {
        return new WidgetTexture(mouseX, mouseY, inner.widget());
    }
}
