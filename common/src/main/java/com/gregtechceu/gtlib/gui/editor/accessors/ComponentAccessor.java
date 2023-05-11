package com.gregtechceu.gtlib.gui.editor.accessors;


import com.gregtechceu.gtlib.gui.editor.annotation.ConfigAccessor;
import com.gregtechceu.gtlib.gui.editor.annotation.DefaultValue;
import com.gregtechceu.gtlib.gui.editor.configurator.Configurator;
import com.gregtechceu.gtlib.gui.editor.configurator.StringConfigurator;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2022/12/2
 * @implNote ComponentAccessor
 */
@ConfigAccessor
public class ComponentAccessor implements IConfiguratorAccessor<Component> {
    @Override
    public boolean test(Class<?> type) {
        return type == Component.class;
    }

    @Override
    public Component defaultValue(Field field, Class<?> type) {
        if (field.isAnnotationPresent(DefaultValue.class)) {
            return Component.nullToEmpty(field.getAnnotation(DefaultValue.class).stringValue()[0]);
        }
        return Component.empty();
    }

    @Override
    public Configurator create(String name, Supplier<Component> supplier, Consumer<Component> consumer, boolean forceUpdate, Field field) {
        return new StringConfigurator(name, () -> {
            Component component = supplier.get();
            return component.getString();
        }, s -> consumer.accept(Component.translatable(s)), "", forceUpdate);
    }
}
