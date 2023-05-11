package com.gregtechceu.gtlib.gui.editor.configurator;

import com.gregtechceu.gtlib.gui.widget.Widget;
import com.gregtechceu.gtlib.utils.Position;
import com.gregtechceu.gtlib.utils.Size;

/**
 * @author KilaBash
 * @date 2022/12/3
 * @implNote WrapperConfigurator
 */
public class WrapperConfigurator extends Configurator {
    public final Widget inner;

    public WrapperConfigurator(String name, Widget widget) {
        super(name);
        this.inner = widget;
    }

    @Override
    public void computeHeight() {
        super.computeHeight();
        setSize(new Size(getSize().width, inner.getSize().height + 19));
    }

    @Override
    public void init(int width) {
        super.init(width);
        Size size = inner.getSize();
        inner.setSelfPosition(new Position((width - size.width) / 2, 17));
        addWidget(inner);
    }
}
