package com.gregtechceu.gtlib.gui.editor.accessors;

import com.gregtechceu.gtlib.gui.editor.annotation.ConfigAccessor;
import com.gregtechceu.gtlib.gui.editor.annotation.DefaultValue;
import com.gregtechceu.gtlib.gui.editor.configurator.Configurator;
import com.gregtechceu.gtlib.gui.editor.configurator.StringConfigurator;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2022/12/1
 * @implNote NumberAccessor
 */
@ConfigAccessor
public class StringAccessor extends TypesAccessor<String> {

    public StringAccessor() {
        super(String.class);
    }

    @Override
    public String defaultValue(Field field, Class<?> type) {
        if (field.isAnnotationPresent(DefaultValue.class)) {
            return field.getAnnotation(DefaultValue.class).stringValue()[0];
        }
        return "";
    }

    @Override
    public Configurator create(String name, Supplier<String> supplier, Consumer<String> consumer, boolean forceUpdate, Field field) {
        return new StringConfigurator(name, supplier, consumer, defaultValue(field, String.class), forceUpdate);
    }
}
