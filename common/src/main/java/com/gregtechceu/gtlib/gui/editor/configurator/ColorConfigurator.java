package com.gregtechceu.gtlib.gui.editor.configurator;

import com.gregtechceu.gtlib.gui.editor.ColorPattern;
import com.gregtechceu.gtlib.gui.editor.Icons;
import com.gregtechceu.gtlib.gui.editor.ui.Editor;
import com.gregtechceu.gtlib.gui.texture.ColorRectTexture;
import com.gregtechceu.gtlib.gui.texture.GuiTextureGroup;
import com.gregtechceu.gtlib.gui.texture.IGuiTexture;
import com.gregtechceu.gtlib.gui.util.TreeBuilder;
import com.gregtechceu.gtlib.gui.widget.ButtonWidget;
import com.gregtechceu.gtlib.gui.widget.DialogWidget;
import com.gregtechceu.gtlib.gui.widget.HsbColorWidget;
import com.gregtechceu.gtlib.gui.widget.ImageWidget;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2022/12/1
 * @implNote NumberConfigurator
 */
public class ColorConfigurator extends ValueConfigurator<Number> {
    protected ImageWidget image;
    @Setter
    protected boolean colorBackground;

    public ColorConfigurator(String name, Supplier<Number> supplier, Consumer<Number> onUpdate, @Nonnull Number defaultValue, boolean forceUpdate) {
        super(name, supplier, onUpdate, defaultValue, forceUpdate);
        if (value == null) {
            value = defaultValue;
        }
    }

    @Override
    protected void onValueUpdate(Number newValue) {
        if (newValue == null) newValue = defaultValue;
        if (newValue.equals(value)) return;
        super.onValueUpdate(newValue);
        image.setImage(getCommonColor());
    }

    private IGuiTexture getCommonColor() {
        return new ColorRectTexture(value.intValue()).setRadius(5).setRadius(5);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1 && Editor.INSTANCE != null && image.isMouseOverElement(mouseX, mouseY)) {
            var menu = TreeBuilder.Menu.start()
                    .leaf(Icons.COPY, "gtlib.gui.editor.menu.copy", () -> Editor.INSTANCE.setCopy("number", value));
            if ("number".equals(Editor.INSTANCE.getCopyType())) {
                menu.leaf(Icons.PASTE, "gtlib.gui.editor.menu.paste", () -> {
                    Editor.INSTANCE.ifCopiedPresent("number", c -> {
                        if (c instanceof Number number) {
                            onValueUpdate(number.intValue());
                            updateValue();
                        }
                    });
                });
            }
            Editor.INSTANCE.openMenu(mouseX, mouseY, menu);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void init(int width) {
        super.init(width);
        addWidget(image = new ImageWidget(leftWidth, 2, width - leftWidth - 3 - rightWidth, 10, getCommonColor()));
        image.setDraggingConsumer(
                o -> o instanceof Number,
                o -> image.setImage(ColorPattern.GREEN.rectTexture().setRadius(5)),
                o -> image.setImage(getCommonColor()),
                o -> {
                    if (o instanceof Number number) {
                        onValueUpdate(number.intValue());
                        updateValue();
                    }
                });
        addWidget(new ButtonWidget(leftWidth, 2, width - leftWidth - 3 - rightWidth, 10, null, cd -> {
            if (Editor.INSTANCE != null) {
                var position = image.getPosition();
                var dialog = Editor.INSTANCE.openDialog(new DialogWidget(position.x, position.y - 110, 110, 110));
                dialog.setClickClose(true);
                dialog.addWidget(new HsbColorWidget(5, 5, 100, 100)
                                .setOnChanged(newColor -> {
                                    value = newColor;
                                    updateValue();
                                    image.setImage(getCommonColor());
                                })
                                .setColorSupplier(() -> value.intValue())
                                .setColor(value.intValue()))
                        .setBackground(new GuiTextureGroup(ColorPattern.BLACK.rectTexture(), ColorPattern.T_WHITE.borderTexture(-1)));
            }
        }));
    }

}
