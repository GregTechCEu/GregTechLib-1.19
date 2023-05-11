package com.gregtechceu.gtlib.gui.editor.ui.tool;

import com.gregtechceu.gtlib.gui.editor.ColorPattern;
import com.gregtechceu.gtlib.gui.editor.Icons;
import com.gregtechceu.gtlib.gui.editor.annotation.RegisterUI;
import com.gregtechceu.gtlib.gui.editor.configurator.IConfigurableWidget;
import com.gregtechceu.gtlib.gui.editor.runtime.UIDetector;
import com.gregtechceu.gtlib.gui.editor.ui.ToolPanel;
import com.gregtechceu.gtlib.gui.texture.ResourceTexture;
import com.gregtechceu.gtlib.gui.texture.WidgetTexture;
import com.gregtechceu.gtlib.gui.widget.DraggableScrollableWidgetGroup;
import com.gregtechceu.gtlib.gui.widget.ImageWidget;
import com.gregtechceu.gtlib.gui.widget.LabelWidget;
import com.gregtechceu.gtlib.gui.widget.SelectableWidgetGroup;
import com.gregtechceu.gtlib.utils.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2022/12/10
 * @implNote WidgetToolBox
 */
public class WidgetToolBox extends DraggableScrollableWidgetGroup {
    public static class Default {
        public static List<Default> TABS = new ArrayList<>();
        public static final Default BASIC = registerTab("widget.basic", Icons.WIDGET_BASIC);
        public static final Default GROUP = registerTab("widget.group", Icons.WIDGET_GROUP);
        public static final Default CONTAINER = registerTab("widget.container", Icons.WIDGET_CONTAINER);
        public static final Default CUSTOM = registerTab("widget.custom", Icons.WIDGET_CUSTOM);

        public final String groupName;
        public final ResourceTexture icon;

        private Default(String groupName, ResourceTexture icon) {
            this.groupName = groupName;
            this.icon = icon;
            TABS.add(this);
        }

        public WidgetToolBox createToolBox() {
            return new WidgetToolBox(groupName);
        }

        public static Default registerTab(String groupName, ResourceTexture icon) {
            return new Default(groupName, icon);
        }
    }

    public WidgetToolBox(String groupName) {
        super(0, 0, ToolPanel.WIDTH, 100);
        int yOffset = 3;
        setYScrollBarWidth(4).setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(2).transform(-0.5f, 0));
        for (UIDetector.Wrapper<RegisterUI, IConfigurableWidget> wrapper : UIDetector.REGISTER_WIDGETS) {
            String group = wrapper.annotation().group().isEmpty() ? "widget.basic" : wrapper.annotation().group();
            if (group.equals(groupName)) {
                var widget = wrapper.creator().get();
                widget.widget().setSelfPosition(new Position(0, 0));
                SelectableWidgetGroup selectableWidgetGroup = new SelectableWidgetGroup(0, yOffset, ToolPanel.WIDTH - 2, 50 + 14);
                selectableWidgetGroup.addWidget(new ImageWidget((ToolPanel.WIDTH - 2 - 45) / 2, 17, 45, 30, new WidgetTexture(widget.widget())));
                selectableWidgetGroup.addWidget(new LabelWidget(3, 3, widget.getTranslateKey()));
                selectableWidgetGroup.setSelectedTexture(ColorPattern.T_GRAY.rectTexture());
                selectableWidgetGroup.setDraggingProvider(() -> new IWidgetPanelDragging() {
                    final IConfigurableWidget configurableWidget = wrapper.creator().get();
                    @Override
                    public IConfigurableWidget get() {
                        return configurableWidget;
                    }
                }, (w, p) -> new WidgetTexture(w.get().widget()).setDragging(true));
                addWidget(selectableWidgetGroup);
                yOffset += 50 + 14 + 3;
            }
        }
    }

    public interface IWidgetPanelDragging extends Supplier<IConfigurableWidget> {
    }
}
