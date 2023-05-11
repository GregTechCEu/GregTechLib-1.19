package com.gregtechceu.gtlib.jei;

import com.gregtechceu.gtlib.core.mixins.jei.RecipeSlotAccessor;
import com.gregtechceu.gtlib.gui.ingredient.IRecipeIngredientSlot;
import com.gregtechceu.gtlib.gui.widget.Widget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.library.gui.ingredients.RecipeSlot;
import mezz.jei.library.ingredients.TypedIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RecipeSlotWrapper extends RecipeSlot {

    private final Widget widget;
    private final RecipeSlot wrapperSlot;
    private ImmutableRect2i area;

    public RecipeSlotWrapper(
            Widget widget,
            RecipeSlot wrapperSlot,
            int xPos,
            int yPos
    ) {

        super(((RecipeSlotAccessor) wrapperSlot).getIngredientManager(), wrapperSlot.getRole(), 0, 0, 0);
        this.widget = widget;
        this.wrapperSlot = wrapperSlot;
        this.area = new ImmutableRect2i(xPos, yPos, widget.getSize().width, widget.getSize().height);
        ((RecipeSlotAccessor) this).setArea(this.area);
        ((RecipeSlotAccessor) wrapperSlot).setArea(this.area);
        if (widget instanceof IRecipeIngredientSlot slot) {
            slot.clearTooltipCallback();
        }
    }

    @Override
    public @Unmodifiable Stream<ITypedIngredient<?>> getAllIngredients() {
        return wrapperSlot.getAllIngredients();
    }

    @Override
    public boolean isEmpty() {
        return wrapperSlot.isEmpty();
    }

    @Override
    public <T> Stream<T> getIngredients(IIngredientType<T> ingredientType) {
        return wrapperSlot.getIngredients(ingredientType);
    }

    @Override
    public Optional<ITypedIngredient<?>> getDisplayedIngredient() {
        return this.getDisplayIngredient();
    }

    @Override
    public <T> Optional<T> getDisplayedIngredient(IIngredientType<T> ingredientType) {
        return wrapperSlot.getDisplayedIngredient(ingredientType);
    }

    @Override
    public Optional<String> getSlotName() {
        return wrapperSlot.getSlotName();
    }

    @Override
    public RecipeIngredientRole getRole() {
        return wrapperSlot.getRole();
    }

    @Override
    public void drawHighlight(PoseStack poseStack, int color) {
        int x = this.area.getX();
        int y = this.area.getY();
        int width = this.area.getWidth();
        int height = this.area.getHeight();

        RenderSystem.disableDepthTest();
        GuiComponent.fill(poseStack, x, y, x + width, y + height, color);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    @Override
    public void setBackground(IDrawable background) {
        wrapperSlot.setBackground(background);
    }

    @Override
    public void setOverlay(IDrawable overlay) {
        wrapperSlot.setOverlay(overlay);
    }

    @Override
    public void set(List<Optional<ITypedIngredient<?>>> ingredients, Set<Integer> focusMatches, IIngredientVisibility ingredientVisibility) {
        wrapperSlot.set(ingredients, focusMatches, ingredientVisibility);
    }

    @Override
    public List<Component> getTooltip() {
        return wrapperSlot.getTooltip();
    }

    @Override
    public void addTooltipCallback(IRecipeSlotTooltipCallback tooltipCallback) {
        wrapperSlot.addTooltipCallback(tooltipCallback);
        if (widget instanceof IRecipeIngredientSlot) {
            ((IRecipeIngredientSlot) widget).addTooltipCallback(tooltips -> {
                List<Component> additional = new ArrayList<>();
                tooltipCallback.onTooltip(this, additional);
                tooltips.addAll(additional);
            });
        }
    }

    @Override
    public <T> void addRenderOverride(IIngredientType<T> ingredientType, IIngredientRenderer<T> ingredientRenderer) {
        wrapperSlot.addRenderOverride(ingredientType, ingredientRenderer);
    }

    @Override
    public void draw(PoseStack poseStack) {
        wrapperSlot.draw(poseStack);
    }


    @Override
    public void drawHoverOverlays(PoseStack poseStack) {
        wrapperSlot.drawHoverOverlays(poseStack);
    }

    @Override
    public Rect2i getRect() {
        return this.area.toMutable();
    }

    @Override
    public void setSlotName(String slotName) {
        wrapperSlot.setSlotName(slotName);
    }

    public void onPositionUpdate(RecipeLayoutWrapper<?> layoutWrapper) {
        int posY = widget.getPosition().y - layoutWrapper.getWrapper().getTop();
        int height = widget.getSize().height;
        if (posY < 0) {
            height += posY;
            posY = 0;
        }

        this.area = new ImmutableRect2i(
                widget.getPosition().x - layoutWrapper.getWrapper().getLeft(),
                posY,
                widget.getSize().width,
                height
        );
        ((RecipeSlotAccessor) this).setArea(this.area);
        ((RecipeSlotAccessor) wrapperSlot).setArea(this.area);
    }

    private Optional<ITypedIngredient<?>> getDisplayIngredient() {
        if (widget instanceof IRecipeIngredientSlot slot && slot.getJEIIngredient() != null) {
            return TypedIngredient.createAndFilterInvalid(((RecipeSlotAccessor) this).getIngredientManager(), slot.getJEIIngredient());
        }
        return Optional.empty();
    }

}
