package com.gregtechceu.gtlib.gui.editor.ui;

import com.gregtechceu.gtlib.gui.editor.Icons;
import com.gregtechceu.gtlib.gui.editor.configurator.IConfigurableWidget;
import com.gregtechceu.gtlib.gui.editor.configurator.IConfigurableWidgetGroup;
import com.gregtechceu.gtlib.gui.editor.data.resource.Resource;
import com.gregtechceu.gtlib.gui.editor.data.resource.TexturesResource;
import com.gregtechceu.gtlib.gui.texture.GuiTextureGroup;
import com.gregtechceu.gtlib.gui.texture.IGuiTexture;
import com.gregtechceu.gtlib.gui.texture.UIResourceTexture;
import com.gregtechceu.gtlib.gui.util.TreeBuilder;
import com.gregtechceu.gtlib.gui.widget.Widget;
import com.gregtechceu.gtlib.gui.widget.WidgetGroup;
import com.gregtechceu.gtlib.utils.Position;
import com.gregtechceu.gtlib.utils.Size;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author KilaBash
 * @date 2022/12/5
 * @implNote MainPanel
 */
public class MainPanel extends WidgetGroup {
    public static final String COPY_TYPE = "widgets";

    @Getter
    protected final Editor editor;
    @Getter
    protected final WidgetGroup root;

    @Getter
    private final Set<UIWrapper> selectedUIs = new HashSet<>();

    @Getter
    protected UIWrapper hoverUI;

    private double lastDeltaX, lastDeltaY;
    private boolean isDragPosition, isDragSize;

    public MainPanel(Editor editor, WidgetGroup root) {
        super(0, 0, editor.getSize().width, editor.getSize().height);
        this.editor = editor;
        this.root = root;
        addWidget(root);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean mouseMoved(double mouseX, double mouseY) {
        // find hover widget
        var hovered = getHoverElement(mouseX, mouseY);
        if (hovered instanceof IConfigurableWidget configurableWidget && hovered != this) {
            if (hoverUI == null ||  !hoverUI.is(configurableWidget)) {
                hoverUI = new UIWrapper(this, configurableWidget);
            }
        } else {
            hoverUI = null;
        }
        return super.mouseMoved(mouseX, mouseY);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hoverUI == null) {
            selectedUIs.clear();
        } else {
            if (isCtrlDown()) {
                if (selectedUIs.contains(hoverUI)) {
                    selectedUIs.remove(hoverUI);
                } else {
                    selectedUIs.add(hoverUI);
                }
            } else if (!selectedUIs.contains(hoverUI)){
                selectedUIs.clear();
                selectedUIs.add(hoverUI);
            }
        }

        lastDeltaX = 0;
        lastDeltaY = 0;
        isDragPosition = false;
        isDragSize = false;

        if (!selectedUIs.isEmpty()) {
            if (button == 0 && hoverUI != null) {
                editor.configPanel.openConfigurator(ConfigPanel.Tab.WIDGET, hoverUI);
            }
            if (isAltDown()) { // start dragging pos and size
                if (button == 0) {
                    isDragPosition = true;
                } else if (button == 1) {
                    isDragSize = true;
                }
                return true;
            }
            if (isShiftDown()) { // dragging itself
                var uiWrappers = selectedUIs.toArray(UIWrapper[]::new);
                getGui().getModularUIGui().setDraggingElement(uiWrappers, new GuiTextureGroup(selectedUIs.stream().map(w -> w.toDraggingTexture((int) mouseX, (int) mouseY)).toArray(IGuiTexture[]::new)));
                return true;
            }
        }

        if (button == 1) {
            editor.openMenu(mouseX, mouseY, createMenu());
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected void removeSelected() {
        for (UIWrapper selectedUI : selectedUIs) {
            selectedUI.remove();
        }
        hoverUI = null;
        selectedUIs.clear();
    }

    protected TreeBuilder.Menu createMenu() {
        return TreeBuilder.Menu.start()
                .leaf(Icons.DELETE, "gtlib.gui.editor.menu.remove", this::removeSelected)
                .leaf(Icons.COPY, "gtlib.gui.editor.menu.copy", this::copy)
                .leaf(Icons.CUT, "gtlib.gui.editor.menu.cut", this::cut)
                .leaf(Icons.PASTE, "gtlib.gui.editor.menu.paste", this::paste)
                .crossLine()
                .branch("gtlib.gui.editor.menu.align", menu -> {
                    menu.leaf(Icons.ALIGN_H_C, "gtlib.gui.editor.menu.align.hc", this::alignHC)
                            .leaf(Icons.ALIGN_H_D, "gtlib.gui.editor.menu.align.hd", this::alignHD)
                            .leaf(Icons.ALIGN_H_L, "gtlib.gui.editor.menu.align.hl", this::alignHL)
                            .leaf(Icons.ALIGN_H_R, "gtlib.gui.editor.menu.align.hr", this::alignHR)
                            .leaf(Icons.ALIGN_V_C, "gtlib.gui.editor.menu.align.vc", this::alignVC)
                            .leaf(Icons.ALIGN_V_D, "gtlib.gui.editor.menu.align.vd", this::alignVD)
                            .leaf(Icons.ALIGN_V_T, "gtlib.gui.editor.menu.align.vt", this::alignVT)
                            .leaf(Icons.ALIGN_V_B, "gtlib.gui.editor.menu.align.vb", this::alignVB);
                });
    }

    private void cut() {
        copy();
        if (!selectedUIs.isEmpty()) {
            for (UIWrapper selectedUI : selectedUIs) {
                selectedUI.inner().widget().getParent().onWidgetRemoved(selectedUI.inner());
            }
            selectedUIs.clear();
        }
    }

    @SuppressWarnings("unchecked")
    protected void copy() {
        if (editor.getResourcePanel().resources != null) {
            UIResourceTexture.setCurrentResource((Resource<IGuiTexture>) editor.getResourcePanel().resources.resources.get(TexturesResource.RESOURCE_NAME), true);
        }
        List<CompoundTag> list = new ArrayList<>();
        if (!selectedUIs.isEmpty()) {
            for (UIWrapper selectedUI : selectedUIs) {
                list.add(selectedUI.inner().serializeWrapper());
            }
        }
        UIResourceTexture.clearCurrentResource();
        getEditor().setCopy("widgets", list);
    }

    @SuppressWarnings("unchecked")
    protected void paste() {
        if (hoverUI != null) {
            getEditor().ifCopiedPresent(COPY_TYPE, c -> {
                if (editor.getResourcePanel().resources != null) {
                    UIResourceTexture.setCurrentResource((Resource<IGuiTexture>) editor.getResourcePanel().resources.resources.get(TexturesResource.RESOURCE_NAME), true);
                }
                List<CompoundTag> list = (List<CompoundTag>) c;
                for (var tag : list) {
                    var widget = IConfigurableWidget.deserializeWrapper(tag);
                    if (widget != null && hoverUI.inner() instanceof IConfigurableWidgetGroup group) {
                        widget.widget().addSelfPosition(5,5);
                        if (group.canWidgetAccepted(widget)) {
                            group.acceptWidget(widget);
                        }
                    }
                }
                UIResourceTexture.clearCurrentResource();
            });
        }
    }

    protected void alignVB() {
        if (selectedUIs.size() > 0) {
            int max = Integer.MIN_VALUE;
            for (UIWrapper ui : selectedUIs) {
                max = Math.max(max, ui.inner().widget().getRect().down);
            }
            for (UIWrapper ui : selectedUIs) {
                ui.inner().widget().addSelfPosition(0, max - ui.inner().widget().getRect().down);
            }
        }
    }

    protected void alignVT() {
        if (selectedUIs.size() > 0) {
            int min = Integer.MAX_VALUE;
            for (UIWrapper ui : selectedUIs) {
                min = Math.min(min, ui.inner().widget().getRect().up);
            }
            for (UIWrapper ui : selectedUIs) {
                ui.inner().widget().addSelfPosition(0, min - ui.inner().widget().getRect().up);
            }
        }
    }

    protected void alignVD() {
        if (selectedUIs.size() > 2) {
            var uis = selectedUIs.stream().map(ui -> ui.inner().widget()).sorted(Comparator.comparingInt(w -> w.getRect().getHeightCenter())).toList();
            int min = uis.get(0).getRect().getHeightCenter(), max = uis.get(uis.size() - 1).getRect().getHeightCenter();
            for (int i = 0; i < uis.size(); i++) {
                int centerY = min + (max - min) * i / (selectedUIs.size() - 1);
                var ui = uis.get(i);
                ui.addSelfPosition(0, centerY - ui.getRect().getHeightCenter());
            }
        }
    }

    protected void alignVC() {
        if (selectedUIs.size() > 0) {
            int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
            for (UIWrapper ui : selectedUIs) {
                min = Math.min(min, ui.inner().widget().getRect().up);
                max = Math.max(max, ui.inner().widget().getRect().down);
            }
            int mid = (min + max) / 2;
            for (UIWrapper ui : selectedUIs) {
                ui.inner().widget().addSelfPosition(0, mid - ui.inner().widget().getRect().getHeightCenter());
            }
        }
    }

    protected void alignHR() {
        if (selectedUIs.size() > 0) {
            int max = Integer.MIN_VALUE;
            for (UIWrapper ui : selectedUIs) {
                max = Math.max(max, ui.inner().widget().getRect().right);
            }
            for (UIWrapper ui : selectedUIs) {
                ui.inner().widget().addSelfPosition(max - ui.inner().widget().getRect().right, 0);
            }
        }
    }

    protected void alignHL() {
        if (selectedUIs.size() > 0) {
            int min = Integer.MAX_VALUE;
            for (UIWrapper ui : selectedUIs) {
                min = Math.min(min, ui.inner().widget().getRect().left);
            }
            for (UIWrapper ui : selectedUIs) {
                ui.inner().widget().addSelfPosition(min - ui.inner().widget().getRect().left, 0);
            }
        }
    }

    protected void alignHD() {
        if (selectedUIs.size() > 2) {
            var uis = selectedUIs.stream().map(ui -> ui.inner().widget()).sorted(Comparator.comparingInt(w -> w.getRect().getWidthCenter())).toList();
            int min = uis.get(0).getRect().getWidthCenter(), max = uis.get(uis.size() - 1).getRect().getWidthCenter();
            for (int i = 0; i < uis.size(); i++) {
                int centerX = min + (max - min) * i / (selectedUIs.size() - 1);
                var ui = uis.get(i);
                ui.addSelfPosition(centerX - ui.getRect().getWidthCenter(), 0);
            }
        }
    }

    protected void alignHC() {
        if (selectedUIs.size() > 0) {
            int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
            for (UIWrapper ui : selectedUIs) {
                min = Math.min(min, ui.inner().widget().getRect().left);
                max = Math.max(max, ui.inner().widget().getRect().right);
            }
            int mid = (min + max) / 2;
            for (UIWrapper ui : selectedUIs) {
                ui.inner().widget().addSelfPosition(mid - ui.inner().widget().getRect().getWidthCenter(), 0);
            }
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void drawInBackground(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(poseStack, mouseX, mouseY, partialTicks);

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (UIWrapper selectedUI : selectedUIs) {
            selectedUI.drawInBackground(poseStack, mouseX, mouseY, partialTicks);
            var pos = selectedUI.inner().widget().getPosition();
            var size = selectedUI.inner().widget().getSize();
            minX = Math.min(minX, pos.x);
            minY = Math.min(minY, pos.y);
            maxX = Math.max(maxX, pos.x + size.width);
            maxY = Math.max(maxY, pos.y + size.height);
        }

        if (hoverUI != null) {
            hoverUI.drawInBackground(poseStack, mouseX, mouseY, partialTicks);
        }

        if (!selectedUIs.isEmpty() && Widget.isAltDown()) {
            Position pos = new Position(minX, minY);
            Size size = new Size(maxX - minX, maxY - minY);


            float middleX = pos.x + (size.width - 16) / 2f;
            float middleY = pos.y + (size.height - 16) / 2f;
            if (isDragPosition) {
                Icons.UP.copy().setColor(-1).draw(poseStack, mouseX, mouseY, middleX, pos.y - 10 - 16, 16, 16);
                Icons.LEFT.copy().setColor(-1).draw(poseStack, mouseX, mouseY, pos.x - 10 - 16, middleY, 16, 16);
            }
            if (isDragPosition || isDragSize) {
                Icons.DOWN.copy().setColor(-1).draw(poseStack, mouseX, mouseY, middleX, pos.y + size.height + 10, 16, 16);
                Icons.RIGHT.copy().setColor(-1).draw(poseStack, mouseX, mouseY, pos.x + size.width + 10, middleY, 16, 16);
            }
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        double dx = deltaX + lastDeltaX;
        double dy = deltaY + lastDeltaY;
        deltaX = (int) dx;
        deltaY = (int) dy;
        lastDeltaX = dx - deltaX;
        lastDeltaY = dy - deltaY;
        if (!selectedUIs.isEmpty() && isAltDown()) {
            if (isDragPosition) {
                for (UIWrapper selectedUI : selectedUIs) {
                    selectedUI.onDragPosition((int) deltaX, (int) deltaY);
                }
            } else if (isDragSize) {
                for (UIWrapper selectedUI : selectedUIs) {
                    selectedUI.onDragSize((int) deltaX, (int) deltaY);

                }
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (hoverUI != null && hoverUI.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
