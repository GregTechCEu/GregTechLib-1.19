package com.gregtechceu.gtlib.gui.editor.accessors;

import com.gregtechceu.gtlib.gui.editor.annotation.ConfigAccessor;
import com.gregtechceu.gtlib.gui.editor.annotation.DefaultValue;
import com.gregtechceu.gtlib.gui.editor.configurator.Configurator;
import com.gregtechceu.gtlib.gui.editor.configurator.GuiTextureConfigurator;
import com.gregtechceu.gtlib.gui.texture.IGuiTexture;
import com.gregtechceu.gtlib.gui.texture.ResourceTexture;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2022/12/1
 * @implNote NumberAccessor
 */
@ConfigAccessor
public class GuiTextureAccessor extends TypesAccessor<IGuiTexture> {

    public GuiTextureAccessor() {
        super(IGuiTexture.class);
    }

    @Override
    public IGuiTexture defaultValue(Field field, Class<?> type) {
        if (field.isAnnotationPresent(DefaultValue.class)) {
            return new ResourceTexture(field.getAnnotation(DefaultValue.class).stringValue()[0]);
        }
        return IGuiTexture.EMPTY;
    }

    @Override
    public Configurator create(String name, Supplier<IGuiTexture> supplier, Consumer<IGuiTexture> consumer, boolean forceUpdate, Field field) {
        return new GuiTextureConfigurator(name, supplier, consumer, forceUpdate);
    }
}
