package com.gregtechceu.gtlib.gui.editor.accessors;

import com.gregtechceu.gtlib.gui.editor.annotation.ConfigAccessor;
import com.gregtechceu.gtlib.gui.editor.annotation.DefaultValue;
import com.gregtechceu.gtlib.gui.editor.configurator.BooleanConfigurator;
import com.gregtechceu.gtlib.gui.editor.configurator.Configurator;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2022/12/1
 * @implNote NumberAccessor
 */
@ConfigAccessor
public class BooleanAccessor extends TypesAccessor<Boolean> {

    public BooleanAccessor() {
        super(Boolean.class, boolean.class);
    }

    @Override
    public Boolean defaultValue(Field field, Class<?> type) {
        if (field.isAnnotationPresent(DefaultValue.class)) {
            return field.getAnnotation(DefaultValue.class).booleanValue()[0];
        }
        return false;
    }

    @Override
    public Configurator create(String name, Supplier<Boolean> supplier, Consumer<Boolean> consumer, boolean forceUpdate, Field field) {
        return new BooleanConfigurator(name, supplier, consumer, defaultValue(field, boolean.class), forceUpdate);
    }
}
