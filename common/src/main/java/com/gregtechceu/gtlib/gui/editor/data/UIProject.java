package com.gregtechceu.gtlib.gui.editor.data;

import com.gregtechceu.gtlib.gui.editor.ColorPattern;
import com.gregtechceu.gtlib.gui.editor.annotation.RegisterUI;
import com.gregtechceu.gtlib.gui.editor.configurator.IConfigurableWidget;
import com.gregtechceu.gtlib.gui.editor.ui.Editor;
import com.gregtechceu.gtlib.gui.editor.ui.MainPanel;
import com.gregtechceu.gtlib.gui.editor.ui.tool.WidgetToolBox;
import com.gregtechceu.gtlib.gui.texture.GuiTextureGroup;
import com.gregtechceu.gtlib.gui.texture.ResourceBorderTexture;
import com.gregtechceu.gtlib.gui.texture.TextTexture;
import com.gregtechceu.gtlib.gui.widget.TabButton;
import com.gregtechceu.gtlib.gui.widget.WidgetGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

import java.io.File;
import java.io.IOException;

/**
 * @author KilaBash
 * @date 2022/12/4
 * @implNote UIProject
 */
@RegisterUI(name = "ui", group = "project")
public class UIProject extends Project {

    public Resources resources;
    public WidgetGroup root;

    private UIProject() {

    }

    public UIProject(Resources resources, WidgetGroup root) {
        this.resources = resources;
        this.root = root;
    }

    public UIProject(CompoundTag tag) {
        deserializeNBT(tag);
    }

    public UIProject newEmptyProject() {
        return new UIProject(Resources.defaultResource(),
                (WidgetGroup) new WidgetGroup(30, 30, 200, 200).setBackground(ResourceBorderTexture.BORDERED_BACKGROUND));
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("resources", resources.serializeNBT());
        tag.put("root", IConfigurableWidget.serializeNBT(this.root, resources, true));
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        this.resources = loadResources(tag.getCompound("resources"));
        this.root = new WidgetGroup();
        IConfigurableWidget.deserializeNBT(this.root, tag.getCompound("root"), resources, true);
    }

    @Override
    public Resources getResources() {
        return resources;
    }

    @Override
    public void saveProject(File file) {
        try {
            NbtIo.write(serializeNBT(), file);
        } catch (IOException ignored) {
            // TODO
        }
    }

    public Project loadProject(File file) {
        try {
            var tag = NbtIo.read(file);
            if (tag != null) {
                return new UIProject(tag);
            }
        } catch (IOException ignored) {}
        return null;
    }

    @Override
    public void onLoad(Editor editor) {
        super.onLoad(editor);
        editor.getTabPages().addTab(new TabButton(50, 16, 60, 14).setTexture(
                new GuiTextureGroup(ColorPattern.T_GREEN.rectTexture().setBottomRadius(10).transform(0, 0.4f), new TextTexture("Main")),
                new GuiTextureGroup(ColorPattern.T_RED.rectTexture().setBottomRadius(10).transform(0, 0.4f), new TextTexture("Main"))
        ), new MainPanel(editor, root));

        for (WidgetToolBox.Default tab : WidgetToolBox.Default.TABS) {
            editor.getToolPanel().addNewToolBox("gtlib.gui.editor.group." + tab.groupName, tab.icon, tab.createToolBox());
        }

    }
}
