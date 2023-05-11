package com.gregtechceu.gtlib.gui.editor.ui;

import com.gregtechceu.gtlib.gui.animation.Transform;
import com.gregtechceu.gtlib.gui.editor.ColorPattern;
import com.gregtechceu.gtlib.gui.editor.Icons;
import com.gregtechceu.gtlib.gui.texture.GuiTextureGroup;
import com.gregtechceu.gtlib.gui.texture.ResourceTexture;
import com.gregtechceu.gtlib.gui.widget.*;
import com.gregtechceu.gtlib.utils.Size;
import com.gregtechceu.gtlib.utils.interpolate.Eases;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KilaBash
 * @date 2022/12/2
 * @implNote ResourcePanel
 */
public class ToolPanel extends WidgetGroup {
    public static final int WIDTH = 100;

    @Getter
    protected final Editor editor;
    protected final List<Widget> toolBoxes = new ArrayList<>();
    protected ButtonWidget buttonHide;
    protected TabContainer tabContainer;
    protected ImageWidget tabsBackground;

    @Getter
    protected boolean isShow;

    public ToolPanel(Editor editor) {
        super(-100, 30, WIDTH, Math.max(100, editor.getSize().getHeight() - ResourcePanel.HEIGHT - 30));
        setClientSideWidget();
        this.editor = editor;
    }

    @Override
    public void initWidget() {
        Size size = getSize();
        this.setBackground(ColorPattern.BLACK.rectTexture());

        addWidget(new LabelWidget(3, 3, "gtlib.gui.editor.group.tool_box"));
        addWidget(tabsBackground = new ImageWidget(WIDTH, 15, 20, 0, ColorPattern.BLACK.rectTexture().setRightRadius(8)));

        addWidget(tabContainer = new TabContainer(0, 15, WIDTH, size.height - 15));
        tabContainer.setBackground(ColorPattern.T_GRAY.borderTexture(-1));

        addWidget(buttonHide = new ButtonWidget(WIDTH - 13, 3, 10, 10, new GuiTextureGroup(
                ColorPattern.BLACK.rectTexture(),
                ColorPattern.T_GRAY.borderTexture(1),
                Icons.RIGHT
        ), cd -> {
            if (isShow()) {
                hide();
            } else {
                show();
            }
        }).setHoverBorderTexture(1, -1));

        super.initWidget();
    }

    @Override
    public void clearAllWidgets() {
        toolBoxes.clear();
        tabContainer.clearAllWidgets();
    }

    public void addNewToolBox(String name, ResourceTexture texture, WidgetGroup toolBox) {
        toolBox.setSize(new Size(WIDTH, getSize().height - 15));
        tabContainer.addTab((TabButton) new TabButton(WIDTH + 4, 4 + toolBoxes.size() * 20, 12, 12) {
            @Override
            @Environment(EnvType.CLIENT)
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if(isMouseOverElement(mouseX, mouseY)) {
                    show();
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }
        }.setTexture(texture, texture.copy().setColor(ColorPattern.T_GREEN.color)).setHoverTooltips(name), toolBox);
        toolBoxes.add(toolBox);
        tabsBackground.setSize(new Size(20, toolBoxes.size() * 20));
    }

    public void hide() {
        if (isShow() && !inAnimate()) {
            isShow = !isShow;
            animation(new Transform()
                    .setOffset(-WIDTH, 0)
                    .setEase(Eases.EaseQuadOut)
                    .setDuration(500)
                    .setOnFinish(() -> {
                        addSelfPosition(-WIDTH, 0);
                        buttonHide.setButtonTexture(ColorPattern.BLACK.rectTexture(), ColorPattern.T_GRAY.borderTexture(1), Icons.RIGHT);
                    }));
        }
    }

    public void show() {
        if (!isShow() && !inAnimate()) {
            isShow = !isShow;
            animation(new Transform()
                    .setOffset(WIDTH, 0)
                    .setEase(Eases.EaseQuadOut)
                    .setDuration(500)
                    .setOnFinish(() -> {
                        addSelfPosition(WIDTH, 0);
                        buttonHide.setButtonTexture(ColorPattern.BLACK.rectTexture(), ColorPattern.T_GRAY.borderTexture(1), Icons.LEFT);
                    }));
        }
    }
}
