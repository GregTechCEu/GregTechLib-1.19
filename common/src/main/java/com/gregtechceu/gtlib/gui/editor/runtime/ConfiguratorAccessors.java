package com.gregtechceu.gtlib.gui.editor.runtime;

import com.gregtechceu.gtlib.gui.editor.accessors.ArrayConfiguratorAccessor;
import com.gregtechceu.gtlib.gui.editor.accessors.CollectionConfiguratorAccessor;
import com.gregtechceu.gtlib.gui.editor.accessors.IConfiguratorAccessor;
import com.gregtechceu.gtlib.utils.ReflectionUtils;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author KilaBash
 * @date 2022/12/1
 * @implNote ConfiguratorAccessors
 */
public class ConfiguratorAccessors {
    private static final Map<Class<?>, IConfiguratorAccessor<?>> ACCESSOR_MAP = new ConcurrentHashMap<>();

    public static IConfiguratorAccessor<?> findByType(Type clazz) {

        if (clazz instanceof GenericArrayType array) {
            var componentType = array.getGenericComponentType();
            var childAccessor = findByType(componentType);
            var rawType = ReflectionUtils.getRawType(componentType);

            return new ArrayConfiguratorAccessor(rawType == null ? Object.class : rawType, childAccessor);
        }

        var rawType = ReflectionUtils.getRawType(clazz);

        if (rawType != null) {
            if (rawType.isArray()) {
                var componentType = rawType.getComponentType();
                var childAccessor = findByType(componentType);
                return new ArrayConfiguratorAccessor(componentType, childAccessor);
            }

            if (Collection.class.isAssignableFrom(rawType)) {
                var componentType = ((ParameterizedType) clazz).getActualTypeArguments()[0];
                var childAccessor = findByType(componentType);
                var rawComponentType = ReflectionUtils.getRawType(componentType);

                return new CollectionConfiguratorAccessor(rawType, rawComponentType == null ? Object.class : rawComponentType, childAccessor);
            }

            return findByClass(rawType);
        }
        return IConfiguratorAccessor.DEFAULT;
    }

    public static IConfiguratorAccessor<?> findByClass(Class<?> clazz) {
        return ACCESSOR_MAP.computeIfAbsent(clazz, c -> {
            for (IConfiguratorAccessor<?> accessor : UIDetector.CONFIGURATOR_ACCESSORS) {
                if (accessor.test(c)) {
                    return accessor;
                }
            }
            return IConfiguratorAccessor.DEFAULT;
        });
    }

}
