package com.gregtechceu.gtlib.jei;

import com.gregtechceu.gtlib.core.mixins.jei.RecipeLayoutAccessor;
import com.gregtechceu.gtlib.core.mixins.jei.RecipeSlotsAccessor;
import com.gregtechceu.gtlib.gui.widget.Widget;
import com.gregtechceu.gtlib.utils.Position;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.library.gui.ingredients.RecipeSlot;
import mezz.jei.library.gui.ingredients.RecipeSlotsView;
import mezz.jei.library.gui.recipes.RecipeLayout;
import mezz.jei.library.gui.recipes.RecipeLayoutBuilder;
import mezz.jei.library.gui.recipes.ShapelessIcon;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * To reduce workload and allow for customization, we wrapped and expanded {@link RecipeLayout}  to fit our needs.
 */
public class RecipeLayoutWrapper<R extends ModularWrapper<?>> extends RecipeLayout<R> {

    private static final int RECIPE_BORDER_PADDING = 4;

    private final RecipeLayoutAccessor accessor = (RecipeLayoutAccessor) this;

    /**
     * GTLib wraps the recipe inside ModularWrapper so that we can control the rendering of the recipe ourselves.
     */
    private final ModularWrapper<?> wrapper;


    public static <T extends ModularWrapper<?>> RecipeLayout<T> createWrapper(
            IRecipeCategory<T> recipeCategory,
            T recipe,
            IFocusGroup focuses,
            IIngredientManager ingredientManager,
            IIngredientVisibility ingredientVisibility,
            IModIdHelper modIdHelper,
            Textures textures) {
        RecipeLayoutWrapper<T> wrapper = new RecipeLayoutWrapper<>(recipeCategory, recipe, ingredientManager, modIdHelper, textures);
        if (wrapper.setRecipeLayout(recipeCategory, recipe, focuses, ingredientVisibility)) {
            return wrapper;
        }
        return null;
    }

    @Override
    public void setPosition(int posX, int posY) {
        super.setPosition(posX, posY);
        var recipe = getWrapper();
        recipe.setRecipeLayout(posX, posY);
        List<RecipeSlot> recipeSlots = new ArrayList<>();
        List<Widget> allWidgets = recipe.modularUI.getFlatWidgetCollection();
        for (var slot : this.getRecipeSlots().getSlots()) {
            var rect = slot.getRect();
            Widget widget = allWidgets.get(rect.getX());
            Position position = widget.getPosition();
            recipeSlots.add(new RecipeSlotWrapper(widget, slot, position.x - posX, position.y - posY));
        }
        String uid = recipe.getUid();
        if (uid != null) this.addOutputTooltips(recipeSlots, uid);
        this.setRecipeSlots(recipeSlots);
    }

    private boolean setRecipeLayout(
            IRecipeCategory<R> recipeCategory,
            R recipe,
            IFocusGroup focuses,
            IIngredientVisibility ingredientVisibility
    ) {
        RecipeLayoutBuilder builder = new RecipeLayoutBuilder(accessor.getIngredientManager(), accessor.getIngredientCycleOffset());
        try {
            recipeCategory.setRecipe(builder, recipe, focuses);
            if (builder.isUsed()) {
                builder.setRecipeLayout(this, focuses, ingredientVisibility);
                return true;
            }
        } catch (RuntimeException | LinkageError e) {
            accessor.getLogger().error("Error caught from Recipe Category: {}", recipeCategory.getRecipeType().getUid(), e);
        }
        return false;
    }

    private void setRecipeSlots(List<RecipeSlot> recipeSlots) {
        ((RecipeSlotsAccessor) this.getRecipeSlots()).setSlots(recipeSlots);
        ((RecipeSlotsAccessor) this.getRecipeSlots()).setView(new RecipeSlotsView(recipeSlots));
    }

    public RecipeLayoutWrapper(
            IRecipeCategory<R> recipeCategory,
            R recipe,
            IIngredientManager ingredientManager,
            IModIdHelper modIdHelper,
            Textures textures
    ) {
        super(recipeCategory, recipe, ingredientManager, modIdHelper, textures);
        this.wrapper = recipe;
    }

    public ModularWrapper<?> getWrapper() {
        return wrapper;
    }

    /**
     * Rewrite the rendering of the recipe to use the GTLib wrapped rendering method.
     */
    @Override
    public void drawRecipe(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        IRecipeCategory<R> recipeCategory = getRecipeCategory();
        IDrawable background = recipeCategory.getBackground();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int posX = accessor.getPosX();
        int posY = accessor.getPosY();
        final int recipeMouseX = mouseX - posX;
        final int recipeMouseY = mouseY - posY;

        poseStack.pushPose();
        {
            poseStack.translate(posX, posY, 0);

            IDrawable categoryBackground = recipeCategory.getBackground();
            int width = categoryBackground.getWidth() + (2 * RECIPE_BORDER_PADDING);
            int height = categoryBackground.getHeight() + (2 * RECIPE_BORDER_PADDING);
            accessor.getRecipeBorder().draw(poseStack, -RECIPE_BORDER_PADDING, -RECIPE_BORDER_PADDING, width, height);
            background.draw(poseStack);

            // defensive push/pop to protect against recipe categories changing the last pose
            poseStack.pushPose();
            {
                wrapper.draw(poseStack, recipeMouseX, recipeMouseY, Minecraft.getInstance().getDeltaFrameTime());
                //drawExtras and drawInfo often render text which messes with the color, this clears it
                RenderSystem.setShaderColor(1, 1, 1, 1);
            }
            poseStack.popPose();

            //We have any shapeless recipes?
            ShapelessIcon shapelessIcon = accessor.getShapelessIcon();
            if (shapelessIcon != null) {
                shapelessIcon.draw(poseStack);
            }

        }
        poseStack.popPose();
//
//        if (getRecipeTransferButton() != null) {
//            Minecraft minecraft = Minecraft.getInstance();
//            float partialTicks = minecraft.getFrameTime();
//            getRecipeTransferButton().render(poseStack, mouseX, mouseY, partialTicks);
//        }
        RenderSystem.disableBlend();
    }

    @Override
    public void drawOverlays(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        //:P
    }

    /**
     * Sync slots position.
     */
    public void onPositionUpdate() {
        this.getRecipeSlots().getSlots()
                .stream()
                .filter(RecipeSlotWrapper.class::isInstance)
                .map(RecipeSlotWrapper.class::cast)
                .forEach(slotWrapper -> slotWrapper.onPositionUpdate(this));
    }

    private void addOutputTooltips(List<RecipeSlot> recipeSlots, String uid) {
        List<RecipeSlot> outputSlots = recipeSlots.stream()
                .filter(r -> r.getRole() == RecipeIngredientRole.OUTPUT)
                .toList();
        if (outputSlots.isEmpty()) return;

        for (RecipeSlot outputSlot : outputSlots) {
            outputSlot.addTooltipCallback(new RegisterNameTooltipCallback(uid, accessor.getModIdHelper()));
        }
    }

    private static class RegisterNameTooltipCallback implements IRecipeSlotTooltipCallback {

        private final ResourceLocation uid;
        private final IModIdHelper modIdHelper;

        private RegisterNameTooltipCallback(String uid, IModIdHelper modIdHelper) {
            this.uid = new ResourceLocation(uid);
            this.modIdHelper = modIdHelper;
        }

        @Override
        public void onTooltip(@NotNull IRecipeSlotView recipeSlotView, @NotNull List<Component> tooltip) {
            if (recipeSlotView.getRole() != RecipeIngredientRole.OUTPUT) return;
            if (modIdHelper.isDisplayingModNameEnabled()) {
                String modName = modIdHelper.getFormattedModNameForModId(uid.getNamespace());
                var recipeBy = Component.translatable("jei.tooltip.recipe.by", modName);
                tooltip.add(recipeBy.withStyle(ChatFormatting.GRAY));
            }

            Minecraft minecraft = Minecraft.getInstance();
            boolean showAdvanced = minecraft.options.advancedItemTooltips || Screen.hasShiftDown();
            if (showAdvanced) {
                String recipeUid = ResourceLocation.DEFAULT_NAMESPACE.equals(uid.getNamespace()) ? uid.getPath() : uid.toString();
                var recipeId = Component.translatable("jei.tooltip.recipe.id", recipeUid);
                tooltip.add(recipeId.withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

}
