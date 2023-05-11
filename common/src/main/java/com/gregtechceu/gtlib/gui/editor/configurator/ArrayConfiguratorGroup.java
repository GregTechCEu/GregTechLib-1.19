package com.gregtechceu.gtlib.gui.editor.configurator;

import com.gregtechceu.gtlib.gui.editor.ColorPattern;
import com.gregtechceu.gtlib.gui.editor.Icons;
import com.gregtechceu.gtlib.gui.editor.ui.ConfigPanel;
import com.gregtechceu.gtlib.gui.texture.ColorRectTexture;
import com.gregtechceu.gtlib.gui.texture.GuiTextureGroup;
import com.gregtechceu.gtlib.gui.texture.WidgetTexture;
import com.gregtechceu.gtlib.gui.widget.ButtonWidget;
import com.gregtechceu.gtlib.gui.widget.ImageWidget;
import com.gregtechceu.gtlib.utils.Position;
import com.gregtechceu.gtlib.utils.Size;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2022/12/2
 * @implNote ArrayConfigurator
 */
public class ArrayConfiguratorGroup<T> extends ConfiguratorGroup{
    protected final Supplier<List<T>> source;
    protected final BiFunction<Supplier<T>, Consumer<T>, Configurator> configuratorProvider;
    @Setter
    protected Supplier<T> addDefault;
    @Setter
    protected Consumer<List<T>> onUpdate;
    @Setter
    protected Consumer<T> onAdd, onRemove;
    @Setter
    protected BiConsumer<Integer, T> onReorder;
    @Setter
    protected boolean canAdd = true, canRemove = true, forceUpdate;

    protected boolean addMask;
    protected ItemConfigurator removeMask;

    public ArrayConfiguratorGroup(String name, boolean isCollapse, Supplier<List<T>> source, BiFunction<Supplier<T>, Consumer<T>, Configurator> configuratorProvider, boolean forceUpdate) {
        super(name, isCollapse);
        this.configuratorProvider = configuratorProvider;
        this.source = source;
        this.forceUpdate = forceUpdate;
        for (T object : source.get()) {
            addConfigurators(new ItemConfigurator(object, configuratorProvider));
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        boolean rebuild = false;
        var newSource = source.get();
        if (newSource.size() == configurators.size()) {
            for (int i = 0; i < newSource.size(); i++) {
                var itemConfigurator = (ItemConfigurator)configurators.get(i);
                if (!itemConfigurator.object.equals(newSource.get(i))) {
                    rebuild = true;
                    break;
                }
            }
        } else {
            rebuild = true;
        }

        if (rebuild) {
            configurators.forEach(this::removeWidget);
            configurators.clear();
            for (T object : source.get()) {
                addConfigurators(new ItemConfigurator(object, configuratorProvider));
            }
            computeLayout();
            addMask = false;
            removeMask = null;
            return;
        }

        if (addMask) {
            addMask = false;
            if (addDefault != null && canAdd) {
                var newItem = addDefault.get();
                if (onAdd != null) {
                    onAdd.accept(newItem);
                }
                addConfigurators(new ItemConfigurator(newItem, configuratorProvider));
                notifyListUpdate();
                computeLayout();
            }
        } if (removeMask != null) {
            if (canRemove) {
                if (onRemove != null) {
                    onRemove.accept(removeMask.object);
                }
                configurators.remove(removeMask);
                removeWidget(removeMask);
            }
            removeMask = null;
            notifyListUpdate();
            computeLayout();
        }
    }

    public void notifyListUpdate() {
        if (onUpdate != null) {
            onUpdate.accept(configurators.stream().map(c -> ((ItemConfigurator)c).object).toList());
        }
    }

    @Override
    @Deprecated
    public void addConfigurators(Configurator... configurators) {
        super.addConfigurators(configurators);
    }

    @Override
    public void init(int width) {
        super.init(width);
        if (addDefault != null) {
            this.addWidget(new ButtonWidget(width - (tips.length > 0 ? 24 : 12), 2, 9, 9,
                    Icons.ADD,
                    cd -> addMask = true).setHoverTooltips("gtlib.gui.editor.tips.add_item"));
        }
    }

    public void updateOrder(ItemConfigurator src, ItemConfigurator dst, boolean before) {
        if (configurators.remove(src)) {
            removeWidget(src);

            int index = configurators.indexOf(dst);
            if (!before) {
                index++;
            }

            this.configurators.add(index, src);
            addWidget(index, src);

            if (onReorder != null) {
                onReorder.accept(index, src.object);
            }
        }
        notifyListUpdate();
        computeLayout();
    }

    private class ItemConfigurator extends Configurator {
        T object;
        Configurator inner;

        public ItemConfigurator(T object, BiFunction<Supplier<T>, Consumer<T>, Configurator> provider) {
            this.object = object;
            inner = provider.apply(this::getter, this::setter);
            this.addWidget(inner);
            this.addWidget(new ButtonWidget(2, 2, 9, 9,
                    Icons.REMOVE,
                    cd -> removeMask = this)
                    .setHoverTooltips("gtlib.gui.editor.tips.remove_item"));
        }

        private void setter(T t) {
            object = t;
            notifyListUpdate();
        }

        private T getter() {
            return object;
        }

        @Override
        public void setConfigPanel(ConfigPanel configPanel, ConfigPanel.Tab tab) {
            super.setConfigPanel(configPanel, tab);
            inner.setConfigPanel(configPanel, tab);
        }

        @Override
        public void setSelfPosition(Position selfPosition) {
            super.setSelfPosition(selfPosition);
        }

        @Override
        public void computeHeight() {
            inner.computeHeight();
            inner.setSelfPosition(new Position(13, 0));
            int height = inner.getSize().height;
            setSize(new Size(getSize().width, height));
        }

        @Override
        public void init(int width) {
            super.init(width);
            inner.init(width - 10 - 15);
            ImageWidget imageWidget = new ImageWidget(width - 12, 2, 9, 9, new ColorRectTexture(-1).setRadius(4.5f));
            imageWidget.setHoverTooltips("gtlib.gui.editor.tips.drag_item");
            if (onReorder != null) {
                this.setDraggingProvider(() -> this, (t, p) -> new GuiTextureGroup(new WidgetTexture(p.x, p.y, inner), new WidgetTexture(p.x, p.y, imageWidget)));
                imageWidget.setDraggingProvider(() -> this, (t, p) -> new GuiTextureGroup(new WidgetTexture(p.x, p.y, inner), new WidgetTexture(p.x, p.y, imageWidget)));
            }
            this.addWidget(imageWidget);
        }

        @Override
        @Environment(EnvType.CLIENT)
        public void drawInBackground(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(poseStack, mouseX, mouseY, partialTicks);
            if (isMouseOverElement(mouseX, mouseY) && onReorder != null) {
                var object = getGui().getModularUIGui().getDraggingElement();
                Position pos = getPosition();
                Size size = getSize();
                if (object != null && object.getClass() == this.getClass() && object != this) {
                    if (mouseY > pos.y + size.height / 2) { // down
                        ColorPattern.T_GREEN.rectTexture().draw(poseStack, 0, 0, pos.x, pos.y + size.height, size.width, 2);
                    } else { // up
                        ColorPattern.T_GREEN.rectTexture().draw(poseStack, 0, 0, pos.x, pos.y - 1, size.width, 2);
                    }
                }
            }
        }

        @Override
        @Environment(EnvType.CLIENT)
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (isMouseOverElement(mouseX, mouseY) && onReorder != null) {
                var object = getGui().getModularUIGui().getDraggingElement();
                Position pos = getPosition();
                Size size = getSize();
                if (object != null && object.getClass() == this.getClass() && object != this) {
                    updateOrder((ItemConfigurator)object, this, mouseY < pos.y + size.height / 2f);
                    return true;
                }
            }
            return super.mouseReleased(mouseX, mouseY, button);
        }
    }

}
