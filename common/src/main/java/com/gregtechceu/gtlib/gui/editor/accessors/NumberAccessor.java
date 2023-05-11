package com.gregtechceu.gtlib.gui.editor.accessors;

import com.gregtechceu.gtlib.gui.editor.annotation.ConfigAccessor;
import com.gregtechceu.gtlib.gui.editor.annotation.DefaultValue;
import com.gregtechceu.gtlib.gui.editor.annotation.NumberColor;
import com.gregtechceu.gtlib.gui.editor.annotation.NumberRange;
import com.gregtechceu.gtlib.gui.editor.configurator.ColorConfigurator;
import com.gregtechceu.gtlib.gui.editor.configurator.Configurator;
import com.gregtechceu.gtlib.gui.editor.configurator.NumberConfigurator;
import com.gregtechceu.gtlib.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2022/12/1
 * @implNote NumberAccessor
 */
@ConfigAccessor
public class NumberAccessor extends TypesAccessor<Number> {

    public NumberAccessor() {
        super(int.class, float.class, byte.class, double.class, Integer.class, Float.class, Byte.class, Double.class);
    }

    @Override
    public Number defaultValue(Field field, Class<?> type) {
        Number number = 0;
        if (field.isAnnotationPresent(DefaultValue.class)) {
            number = field.getAnnotation(DefaultValue.class).numberValue()[0];
        }
        if (field.isAnnotationPresent(NumberRange.class)) {
            number = field.getAnnotation(NumberRange.class).range()[0];
        }
        if (type == int.class || type == Integer.class) {
            return number.intValue();
        } else if (type == byte.class || type == Byte.class) {
            return number.byteValue();
        } else if (type == long.class || type == Long.class) {
            return number.longValue();
        } else if (type == float.class || type == Float.class) {
            return number.floatValue();
        } else if (type == double.class || type == Double.class) {
            return number.doubleValue();
        }
        return number;
    }

    @Override
    public Configurator create(String name, Supplier<Number> supplier, Consumer<Number> consumer, boolean forceUpdate, Field field) {
        if (field.isAnnotationPresent(NumberColor.class)) {
            return new ColorConfigurator(name, supplier, consumer, defaultValue(field, ReflectionUtils.getRawType(field.getGenericType())), forceUpdate);
        }
        NumberConfigurator configurator = new NumberConfigurator(name, supplier, consumer, defaultValue(field, ReflectionUtils.getRawType(field.getGenericType())), forceUpdate);
        if (field.isAnnotationPresent(NumberRange.class)) {
            NumberRange range = field.getAnnotation(NumberRange.class);
            configurator = configurator.setRange(range.range()[0], range.range()[1]).setWheel(range.wheel());
        }
        return configurator;
    }
}
