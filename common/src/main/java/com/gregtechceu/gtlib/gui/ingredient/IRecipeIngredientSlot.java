package com.gregtechceu.gtlib.gui.ingredient;

import com.gregtechceu.gtlib.gui.widget.Widget;
import com.gregtechceu.gtlib.jei.IngredientIO;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public interface IRecipeIngredientSlot extends IIngredientSlot{

    default Widget self() {
        return (Widget) this;
    }

    @Nullable
    @Override
    default Object getIngredientOverMouse(double mouseX, double mouseY) {
        if (self().isMouseOverElement(mouseX, mouseY)) {
            return getJEIIngredient();
        }
        return null;
    }

    @Nullable
    Object getJEIIngredient();

    default IngredientIO getIngredientIO(){
        return IngredientIO.RENDER_ONLY;
    }

    /**
     * support {@link mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback}
     */
    default void addTooltipCallback(Consumer<List<Component>> callback) {

    }

    default void clearTooltipCallback(){

    }


}
