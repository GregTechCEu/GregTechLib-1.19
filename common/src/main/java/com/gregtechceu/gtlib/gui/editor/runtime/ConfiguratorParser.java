package com.gregtechceu.gtlib.gui.editor.runtime;

import com.gregtechceu.gtlib.gui.editor.accessors.IConfiguratorAccessor;
import com.gregtechceu.gtlib.gui.editor.annotation.ConfigSetter;
import com.gregtechceu.gtlib.gui.editor.annotation.Configurable;
import com.gregtechceu.gtlib.gui.editor.configurator.Configurator;
import com.gregtechceu.gtlib.gui.editor.configurator.ConfiguratorGroup;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * @author KilaBash
 * @date 2022/12/1
 * @implNote ConfiguratorParser
 */
public class ConfiguratorParser {

    public static void createConfigurators(ConfiguratorGroup father, Map<String, Method> setters, Class<?> clazz, Object object) {
        if (clazz == Object.class || clazz == null) return;

        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(ConfigSetter.class)) {
                ConfigSetter configSetter = method.getAnnotation(ConfigSetter.class);
                String name = configSetter.field();
                if (!setters.containsKey(name)) {
                    setters.put(name, method);
                }
            }
        }

        createConfigurators(father, setters, clazz.getSuperclass(), object);
        if (clazz.isAnnotationPresent(Configurable.class)) {
            Configurable configurable = clazz.getAnnotation(Configurable.class);
            String name = configurable.name().isEmpty() ? clazz.getSimpleName() : configurable.name();
            ConfiguratorGroup newGroup = new ConfiguratorGroup(name, configurable.collapse());
            newGroup.setCanCollapse(configurable.canCollapse());
            father.addConfigurators(newGroup);
            father = newGroup;
        }

        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (field.isAnnotationPresent(Configurable.class)) {
                // sub configurable
                var rawClass = field.getDeclaringClass();
                if (rawClass.isAnnotationPresent(Configurable.class) && rawClass.getAnnotation(Configurable.class).subConfigurable()) {
                    try {
                        var value = field.get(object);
                        if (value != null) {
                            createConfigurators(father, new HashMap<>(), field.getDeclaringClass(), value);
                            continue;
                        }
                    } catch (IllegalAccessException ignored) {}
                }
                Configurable configurable = field.getAnnotation(Configurable.class);
                IConfiguratorAccessor accessor = ConfiguratorAccessors.findByType(field.getGenericType());
                field.setAccessible(true);
                String name = configurable.name().isEmpty() ? field.getName() : configurable.name();
                Method setter = setters.get(field.getName());

                Configurator configurator = accessor.create(name, () -> {
                    try {
                        return field.get(object);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }, value -> {
                    try {
                        if (setter == null) {
                            field.set(object, value);
                        } else {
                            setter.invoke(object, value);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, configurable.forceUpdate(), field);

                configurator.setTips(configurable.tips());
                father.addConfigurators(configurator);
            }
        }
    }

}
