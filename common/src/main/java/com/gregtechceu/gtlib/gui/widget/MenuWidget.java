package com.gregtechceu.gtlib.gui.widget;

import com.gregtechceu.gtlib.gui.texture.ColorRectTexture;
import com.gregtechceu.gtlib.gui.texture.IGuiTexture;
import com.gregtechceu.gtlib.gui.texture.TextTexture;
import com.gregtechceu.gtlib.gui.util.TreeNode;
import com.gregtechceu.gtlib.utils.LocalizationUtils;
import com.gregtechceu.gtlib.utils.Position;
import com.gregtechceu.gtlib.utils.Size;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Accessors(chain = true)
public class MenuWidget<K, T> extends WidgetGroup {
    protected final TreeNode<K, T> root;
    protected final int nodeHeight;

    @Setter @Nullable
    protected IGuiTexture nodeTexture;
    @Setter @Nullable
    protected IGuiTexture leafTexture;
    @Setter @Nullable
    protected IGuiTexture nodeHoverTexture;
    @Setter @Nullable
    protected Consumer<TreeNode<K, T>> onNodeClicked;
    @Setter @Nullable
    protected Function<K, IGuiTexture> keyIconSupplier;
    @Setter @Nullable
    protected Function<K, String> keyNameSupplier;
    @Setter @Nullable
    protected Predicate<K> crossLinePredicate;
    @Setter
    protected boolean autoClose;

    protected Map<TreeNode<K, T>, WidgetGroup> children;
    protected MenuWidget<K, T> opened;

    public MenuWidget(int xPosition, int yPosition, int nodeHeight, TreeNode<K, T> root) {
        super(xPosition, yPosition, 100, nodeHeight);
        this.root = root;
        this.autoClose = true;
        this.nodeHeight = nodeHeight;
        this.children = new LinkedHashMap<>();
    }

    public void close(){
        if (this.parent != null) {
            this.parent.waitToRemoved(this);
        }
    }

    @Override
    public void initWidget() {
        int maxWidth = getSize().width;
        int maxHeight = 1;

        if (!root.isLeaf()) {
            if (isRemote()) {
                for (TreeNode<K, T> child : root.getChildren()) {
                    var key = child.getKey();
                    var name = key.toString();
                    if (keyNameSupplier != null) {
                        name = keyNameSupplier.apply(key);
                    }
                    maxWidth = Math.max(Minecraft.getInstance().font.width(LocalizationUtils.format(name)) + 4 + 2 * nodeHeight, maxWidth);
                }
            }
            for (TreeNode<K, T> child : root.getChildren()) {
                var key = child.getKey();
                if (crossLinePredicate != null && crossLinePredicate.test(key)) { // cross line
                    maxHeight += 1;
                    continue;
                }
                var name = key.toString();
                if (keyNameSupplier != null) {
                    name = keyNameSupplier.apply(key);
                }
                var group = new WidgetGroup(0, maxHeight, maxWidth, nodeHeight);
                children.put(child, group);
                if (child.isLeaf()) {
                    group.setBackground(Objects.requireNonNullElseGet(leafTexture, () -> new ColorRectTexture(0xff222222)));
                    group.addWidget(new ButtonWidget(0, 0, maxWidth, nodeHeight, null, cd -> {
                        if (onNodeClicked != null) {
                            onNodeClicked.accept(child);
                        }
                        if (autoClose) {
                            WidgetGroup p = this;
                            while (p != null) {
                                if (p.parent != null && !(p.parent instanceof MenuWidget<?,?>)) {
                                    p.parent.waitToRemoved(p);
                                    return;
                                }
                                p = p.parent;
                            }
                        }
                    }).setHoverTexture(Objects.requireNonNullElseGet(nodeHoverTexture, () -> new ColorRectTexture(0x44aaaaaa))));

                } else {
                    group.setBackground(Objects.requireNonNullElseGet(nodeTexture, () -> new ColorRectTexture(0xff222222)));
                    group.addWidget(new ButtonWidget(0, 0, maxWidth, nodeHeight, null).setHoverTexture(Objects.requireNonNullElseGet(nodeHoverTexture, () -> new ColorRectTexture(0x44aaaaaa))));
                }
                if (keyIconSupplier != null) {
                    group.addWidget(new ImageWidget(2, 1, nodeHeight - 2, nodeHeight - 2, keyIconSupplier.apply(child.getKey())));
                }
                group.addWidget(new ImageWidget(nodeHeight + 2, 0, maxWidth - 2 * nodeHeight - 4, nodeHeight, new TextTexture(name).setType(TextTexture.TextType.LEFT)));
                addWidget(group);
                maxHeight += nodeHeight;
            }
        }
        Position pos = getPosition();
        setSize(new Size(maxWidth, maxHeight));
        // check width
        int rightSpace = getGui().getScreenWidth() - pos.getX();
        int bottomSpace = getGui().getScreenHeight() - pos.getY();
        if (rightSpace < maxWidth) { // move to Left
            if (parent instanceof MenuWidget<?,?> menuWidget) {
                addSelfPosition(-menuWidget.getSize().width - maxWidth, 0);
            }
            rightSpace = getGui().getScreenWidth() - getPosition().getX();
            if (rightSpace < maxWidth) {
                addSelfPosition(-(maxWidth - rightSpace), 0);
            }
            int leftSpace = getPosition().getX();
            if (leftSpace < 0) {
                addSelfPosition(-leftSpace, 0);
            }
        }
        // check height
        if (bottomSpace < maxHeight) {
            if (parent instanceof MenuWidget) {
                addSelfPosition(0, nodeHeight - maxWidth);
            }
            bottomSpace = getGui().getScreenHeight() - getPosition().getY();
            if (bottomSpace < maxHeight) {
                addSelfPosition(0, -(maxHeight - bottomSpace));
            }
            int topSpace = getPosition().getY();
            if (topSpace < 0) {
                addSelfPosition(0, -topSpace);
            }
        }
        super.initWidget();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!super.mouseClicked(mouseX, mouseY, button)) {
            if (autoClose && !(parent instanceof MenuWidget)) {
                close();
            }
            return false;
        }
        return true;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean mouseMoved(double mouseX, double mouseY) {
        if (super.mouseMoved(mouseX, mouseY)) {
            return true;
        }
        int maxHeight = 0;
        for (var node : root.getChildren()) {
            if (crossLinePredicate != null && crossLinePredicate.test(node.getKey())) { // cross line
                maxHeight += 1;
                continue;
            }
            var widget = children.get(node);
            if (widget.isMouseOverElement(mouseX, mouseY)) {
                // if opened
                if (opened != null && opened.root == node) return true;

                // close previous
                if (opened != null) {
                    removeWidget(opened);
                    opened = null;
                }

                // open a new menu
                if (!node.isLeaf()) {
                    opened = new MenuWidget<>(getSize().width, maxHeight, nodeHeight, node)
                            .setNodeHoverTexture(nodeHoverTexture)
                            .setNodeTexture(nodeTexture)
                            .setLeafTexture(leafTexture)
                            .setOnNodeClicked(onNodeClicked)
                            .setKeyIconSupplier(keyIconSupplier)
                            .setKeyNameSupplier(keyNameSupplier);
                    addWidget(opened.setBackground(backgroundTexture));
                }
                return true;
            }
            maxHeight += nodeHeight;
        }
        return false;
    }
}
