package com.gregtechceu.gtlib.gui.editor.configurator;

import com.gregtechceu.gtlib.gui.editor.ColorPattern;
import com.gregtechceu.gtlib.gui.editor.Icons;
import com.gregtechceu.gtlib.gui.editor.ui.ConfigPanel;
import com.gregtechceu.gtlib.gui.texture.IGuiTexture;
import com.gregtechceu.gtlib.gui.util.ClickData;
import com.gregtechceu.gtlib.gui.util.DrawerHelper;
import com.gregtechceu.gtlib.gui.widget.ButtonWidget;
import com.gregtechceu.gtlib.utils.Position;
import com.gregtechceu.gtlib.utils.Size;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KilaBash
 * @date 2022/12/1
 * @implNote ConfiguratorGroup
 */
public class ConfiguratorGroup extends Configurator {
    @Setter
    protected boolean canCollapse = true;
    protected boolean isCollapse;
    @Getter
    protected List<Configurator> configurators = new ArrayList<>();

    public ConfiguratorGroup(String name) {
        this(name, true);
    }

    public ConfiguratorGroup(String name, boolean isCollapse) {
        super(name);
        this.isCollapse = isCollapse;
    }

    @Override
    public void setConfigPanel(ConfigPanel configPanel, ConfigPanel.Tab tab) {
        super.setConfigPanel(configPanel, tab);
        for (Configurator configurator : configurators) {
            configurator.setConfigPanel(configPanel, tab);
        }
    }

    protected void clickName(ClickData clickData) {
        if (!canCollapse) return;
        isCollapse = !isCollapse;
        for (Configurator configurator : configurators) {
            configurator.setActive(!isCollapse);
            configurator.setVisible(!isCollapse);
        }
        computeLayout();
    }

    public void addConfigurators(Configurator... configurators) {
        for (Configurator configurator : configurators) {
            configurator.setConfigPanel(configPanel, tab);
            configurator.setActive(!isCollapse);
            configurator.setVisible(!isCollapse);
            this.configurators.add(configurator);
            addWidget(configurator);
        }
        if (isInit()) {
            for (Configurator configurator : configurators) {
                configurator.init(Math.max(0, width - 5));
            }
            computeLayout();
        }
    }

    @Override
    public void computeHeight() {
        int height = 15;
        if (!isCollapse) {
            for (Configurator configurator : configurators) {
                configurator.computeHeight();
                configurator.setSelfPosition(new Position(5, height));
                height += configurator.getSize().height;
            }
        }
        setSize(new Size(getSize().width, height));
    }

    @Override
    public void init(int width) {
        super.init(width);
        this.addWidget(new ButtonWidget(0, 0, leftWidth + 9, 15, IGuiTexture.EMPTY, this::clickName));
        for (Configurator configurator : configurators) {
            configurator.init(Math.max(0, width - 5));
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void drawInBackground(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        Position pos = getPosition();
        Size size = getSize();
        if (isCollapse) {
            Icons.RIGHT.setColor(-1).draw(poseStack, mouseX, mouseY, pos.x + leftWidth, pos.y + 3, 9, 9);
        } else {
            Icons.DOWN.setColor(-1).draw(poseStack, mouseX, mouseY, pos.x + leftWidth, pos.y + 3, 9, 9);
            if (configurators.size() > 0) {
                DrawerHelper.drawSolidRect(poseStack, pos.x + 2, pos.y + 17, 1, size.height - 19, ColorPattern.T_WHITE.color);
            }
        }
        super.drawInBackground(poseStack, mouseX, mouseY, partialTicks);
    }
}
