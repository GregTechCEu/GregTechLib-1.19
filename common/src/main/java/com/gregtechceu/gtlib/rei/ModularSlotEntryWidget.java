package com.gregtechceu.gtlib.rei;

import com.gregtechceu.gtlib.jei.IngredientIO;
import com.gregtechceu.gtlib.gui.ingredient.IRecipeIngredientSlot;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;

public class ModularSlotEntryWidget extends EntryWidget {

    private final IRecipeIngredientSlot slot;

    public ModularSlotEntryWidget(IRecipeIngredientSlot slot) {
        super(new Rectangle(slot.self().getPosition().x, slot.self().getPosition().y, slot.self().getSize().width, slot.self().getSize().height));
        this.slot = slot;
        if (slot.getIngredientIO() == IngredientIO.INPUT) {
            markIsInput();
        } else if (slot.getIngredientIO() == IngredientIO.OUTPUT) {
            markIsOutput();
        } else {
            unmarkInputOrOutput();
        }
        var ingredient = slot.getJEIIngredient();
        if (ingredient instanceof EntryStack<?> entryStack) {
            entry(entryStack);
        } else if (ingredient instanceof EntryIngredient entryStacks) {
            entries(entryStacks);
        }
    }


    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {

    }

    @Override
    protected void drawBackground(PoseStack matrices, int mouseX, int mouseY, float delta) {

    }

    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return slot.self().isVisible() && slot.self().isMouseOverElement(mouseX, mouseY);
    }

    @Override
    public Rectangle getInnerBounds() {
        var bounds = getBounds();
        return new Rectangle(bounds.x + 1, bounds.y + 1, bounds.width - 2, bounds.height - 2);
    }

}
