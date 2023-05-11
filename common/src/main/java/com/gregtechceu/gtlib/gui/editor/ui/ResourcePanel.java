package com.gregtechceu.gtlib.gui.editor.ui;

import com.gregtechceu.gtlib.gui.animation.Transform;
import com.gregtechceu.gtlib.gui.editor.ColorPattern;
import com.gregtechceu.gtlib.gui.editor.Icons;
import com.gregtechceu.gtlib.gui.editor.data.Resources;
import com.gregtechceu.gtlib.gui.editor.data.resource.Resource;
import com.gregtechceu.gtlib.gui.modular.ModularUI;
import com.gregtechceu.gtlib.gui.texture.GuiTextureGroup;
import com.gregtechceu.gtlib.gui.texture.TextTexture;
import com.gregtechceu.gtlib.gui.widget.*;
import com.gregtechceu.gtlib.utils.LocalizationUtils;
import com.gregtechceu.gtlib.utils.Size;
import com.gregtechceu.gtlib.utils.interpolate.Eases;
import lombok.Getter;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;

/**
 * @author KilaBash
 * @date 2022/12/2
 * @implNote ResourcePanel
 */
public class ResourcePanel extends WidgetGroup {
    public static final int HEIGHT = 100;

    @Getter
    protected Editor editor;
    protected ButtonWidget buttonHide;
    protected TabContainer tabContainer;

    @Getter
    @Nullable
    protected Resources resources;

    @Getter
    protected boolean isShow = true;

    public ResourcePanel(Editor editor) {
        super(0, editor.getSize().height - HEIGHT, editor.getSize().getWidth() - ConfigPanel.WIDTH, HEIGHT);
        setClientSideWidget();
        this.editor = editor;
    }

    private void dispose() {
        if (resources != null) {
            resources.dispose();
        }
    }

    @Override
    public void setGui(ModularUI gui) {
        super.setGui(gui);
        if (gui == null) {
            dispose();
        } else {
            getGui().registerCloseListener(this::dispose);
        }
    }

    @Override
    public void initWidget() {
        Size size = getSize();
        this.setBackground(ColorPattern.BLACK.rectTexture());
        addWidget(buttonHide = new ButtonWidget((getSize().width - 30) / 2, -10, 30, 10, new GuiTextureGroup(
                ColorPattern.BLACK.rectTexture(),
                ColorPattern.T_GRAY.borderTexture(1),
                Icons.DOWN
        ), cd -> {
            if (isShow()) {
                hide();
            } else {
                show();
            }
        }).setHoverBorderTexture(1, -1));
        addWidget(new LabelWidget(3, 3, "gtlib.gui.editor.group.resources"));
        addWidget(tabContainer = new TabContainer(0, 15, size.width, size.height - 14));
        tabContainer.setBackground(ColorPattern.T_GRAY.borderTexture(-1));
        super.initWidget();
    }

    public void hide() {
        if (isShow() && !inAnimate()) {
            isShow = !isShow;
            animation(new Transform()
                    .setOffset(0, HEIGHT)
                    .setEase(Eases.EaseQuadOut)
                    .setDuration(500)
                    .setOnFinish(() -> {
                        addSelfPosition(0, HEIGHT);
                        buttonHide.setButtonTexture(ColorPattern.BLACK.rectTexture(), ColorPattern.T_GRAY.borderTexture(1), Icons.UP);
                    }));
        }
    }

    public void show() {
        if (!isShow() && !inAnimate()) {
            isShow = !isShow;
            animation(new Transform()
                    .setOffset(0, -HEIGHT)
                    .setEase(Eases.EaseQuadOut)
                    .setDuration(500)
                    .setOnFinish(() -> {
                        addSelfPosition(0, -HEIGHT);
                        buttonHide.setButtonTexture(ColorPattern.BLACK.rectTexture(), ColorPattern.T_GRAY.borderTexture(1), Icons.DOWN);
                    }));
        }
    }

    public void loadResource(Resources resources, boolean merge) {
        tabContainer.clearAllWidgets();

        if (!merge && this.resources != null) {
            this.resources.dispose();
        }

        if (!merge || this.resources == null) {
            this.resources = resources;
            resources.load();
        } else {
            this.resources.merge(resources);
        }

        int offset = Minecraft.getInstance().font.width(LocalizationUtils.format("gtlib.gui.editor.group.resources")) + 8;
        for (Resource<?> resource : this.resources.resources.values()) {
            tabContainer.addTab(
                    new TabButton(offset, -15, Minecraft.getInstance().font.width(LocalizationUtils.format(resource.name())) + 8, 15).setTexture(
                            new TextTexture(resource.name()),
                            new GuiTextureGroup(new TextTexture(resource.name(), ColorPattern.T_GREEN.color), ColorPattern.T_GRAY.rectTexture())
                    ),
                    resource.createContainer(this)
            );
            offset += 52;
        }
    }

}
