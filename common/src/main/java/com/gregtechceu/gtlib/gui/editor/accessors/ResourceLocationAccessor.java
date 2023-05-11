package com.gregtechceu.gtlib.gui.editor.accessors;

import com.gregtechceu.gtlib.GTLib;
import com.gregtechceu.gtlib.gui.editor.annotation.ConfigAccessor;
import com.gregtechceu.gtlib.gui.editor.annotation.DefaultValue;
import com.gregtechceu.gtlib.gui.editor.configurator.Configurator;
import com.gregtechceu.gtlib.gui.editor.configurator.StringConfigurator;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2022/12/3
 * @implNote ResourceLocationAccessor
 */
@ConfigAccessor
public class ResourceLocationAccessor extends TypesAccessor<ResourceLocation> {

    public ResourceLocationAccessor() {
        super(ResourceLocation.class);
    }

    @Override
    public ResourceLocation defaultValue(Field field, Class<?> type) {
        if (field.isAnnotationPresent(DefaultValue.class)) {
            return new ResourceLocation(field.getAnnotation(DefaultValue.class).stringValue()[0]);
        }
        return new ResourceLocation(GTLib.MOD_ID, "default");
    }

    @Override
    public Configurator create(String name, Supplier<ResourceLocation> supplier, Consumer<ResourceLocation> consumer, boolean forceUpdate, Field field) {
        var configurator = new StringConfigurator(name, () -> supplier.get().toString(), s -> consumer.accept(new ResourceLocation(s)), defaultValue(field, String.class).toString(), forceUpdate);
        configurator.setResourceLocation(true);
        return configurator;
    }
}
