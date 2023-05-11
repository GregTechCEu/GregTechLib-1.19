package com.gregtechceu.gtlib.gui.editor.configurator;

import com.gregtechceu.gtlib.gui.editor.annotation.RegisterUI;
import com.gregtechceu.gtlib.gui.editor.data.Resources;
import com.gregtechceu.gtlib.gui.editor.data.resource.Resource;
import com.gregtechceu.gtlib.gui.editor.data.resource.TexturesResource;
import com.gregtechceu.gtlib.gui.editor.runtime.PersistedParser;
import com.gregtechceu.gtlib.gui.editor.runtime.UIDetector;
import com.gregtechceu.gtlib.gui.texture.ColorRectTexture;
import com.gregtechceu.gtlib.gui.texture.IGuiTexture;
import com.gregtechceu.gtlib.gui.texture.UIResourceTexture;
import com.gregtechceu.gtlib.gui.widget.Widget;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2022/12/6
 * @implNote IConfigurableWidget
 */
public interface IConfigurableWidget extends IConfigurable {

    Function<String, UIDetector.Wrapper<RegisterUI, IConfigurableWidget>> CACHE = Util.memoize(type -> {
        for (var wrapper : UIDetector.REGISTER_WIDGETS) {
            if (wrapper.annotation().name().equals(type)) {
                return wrapper;
            }
        }
        return null;
    });

    default Widget widget() {
        return (Widget) this;
    }


    default boolean canDragIn(Object dragging) {
        if (dragging instanceof IGuiTexture) {
            return true;
        } else if (dragging instanceof String) {
            return true;
        } else if (dragging instanceof IIdProvider) {
            return true;
        } else if (dragging instanceof Integer) {
            return true;
        }
        return false;
    }

    default boolean handleDragging(Object dragging) {
        if (dragging instanceof IGuiTexture guiTexture) {
            widget().setBackground(guiTexture);
            return true;
        } else if (dragging instanceof String string) {
            widget().setHoverTooltips(string);
            return true;
        } else if (dragging instanceof IIdProvider idProvider) {
            widget().setId(idProvider.get());
            return true;
        } else if (dragging instanceof Integer color) {
            widget().setBackground(new ColorRectTexture(color));
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    static CompoundTag serializeNBT(IConfigurableWidget widget, Resources resources, boolean isProject) {
        UIResourceTexture.setCurrentResource((Resource<IGuiTexture>) resources.resources.get(TexturesResource.RESOURCE_NAME), isProject);
        CompoundTag tag = widget.serializeInnerNBT();
        UIResourceTexture.clearCurrentResource();
        return tag;
    }

    @SuppressWarnings("unchecked")
    static void deserializeNBT(IConfigurableWidget widget, CompoundTag tag, Resources resources, boolean isProject) {
        UIResourceTexture.setCurrentResource((Resource<IGuiTexture>) resources.resources.get(TexturesResource.RESOURCE_NAME), isProject);
        widget.deserializeInnerNBT(tag);
        UIResourceTexture.clearCurrentResource();
    }

    default CompoundTag serializeInnerNBT() {
        CompoundTag tag = new CompoundTag();
        PersistedParser.serializeNBT(tag, getClass(), this);
        return tag;
    }

    default void deserializeInnerNBT(CompoundTag nbt) {
        PersistedParser.deserializeNBT(nbt, new HashMap<>(), getClass(), this);
    }

    default CompoundTag serializeWrapper() {
        var tag = new CompoundTag();
        tag.putString("type", name());
        tag.put("data", serializeInnerNBT());
        return tag;
    }

    @Nullable
    static IConfigurableWidget deserializeWrapper(CompoundTag tag) {
        String type = tag.getString("type");
        var wrapper = CACHE.apply(type);
        if (wrapper != null) {
            var child = wrapper.creator().get();
            child.deserializeInnerNBT(tag.getCompound("data"));
            return child;
        }
        return null;
    }

    // ******* setter ********//


    @FunctionalInterface
    interface IIdProvider extends Supplier<String> {

    }
}
