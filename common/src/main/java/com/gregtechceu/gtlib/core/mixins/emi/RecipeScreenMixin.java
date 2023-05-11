package com.gregtechceu.gtlib.core.mixins.emi;

import com.gregtechceu.gtlib.emi.ModularWrapperWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.screen.RecipeScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/4/3
 * @implNote WidgetGroupMixin
 */
@Mixin(RecipeScreen.class)
public abstract class RecipeScreenMixin {

    @Shadow(remap = false) private List currentPage;

    @Inject(method = "mouseReleased", at = @At(value = "HEAD"), cancellable = true)
    private void initMouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        for (var widgetGroup : currentPage) {
            if (widgetGroup instanceof WidgetGroupAccessor accessor) {
                for (Widget widget : accessor.getWidgets()) {
                    if (widget instanceof ModularWrapperWidget wrapperWidget) {
                        int ox = (int) (mouseX - accessor.getPositionX());
                        int oy = (int) (mouseY - accessor.getPositionY());
                        if (wrapperWidget.mouseReleased(mouseX, mouseY, button)) {
                            cir.setReturnValue(true);
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "mouseDragged", at = @At(value = "HEAD"), cancellable = true)
    private void initMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        for (var widgetGroup : currentPage) {
            if (widgetGroup instanceof WidgetGroupAccessor accessor) {
                for (Widget widget : accessor.getWidgets()) {
                    if (widget instanceof ModularWrapperWidget wrapperWidget) {
                        int ox = (int) (mouseX - accessor.getPositionX());
                        int oy = (int) (mouseY - accessor.getPositionY());
                        if (wrapperWidget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                            cir.setReturnValue(true);
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "mouseScrolled", at = @At(value = "HEAD"), cancellable = true)
    private void initMouseScrolled(double mouseX, double mouseY, double amount, CallbackInfoReturnable<Boolean> cir) {
        for (var widgetGroup : currentPage) {
            if (widgetGroup instanceof WidgetGroupAccessor accessor) {
                for (Widget widget : accessor.getWidgets()) {
                    if (widget instanceof ModularWrapperWidget wrapperWidget) {
                        int ox = (int) (mouseX - accessor.getPositionX());
                        int oy = (int) (mouseY - accessor.getPositionY());
                        if (wrapperWidget.mouseScrolled(mouseX, mouseY, amount)) {
                            cir.setReturnValue(true);
                        }
                    }
                }
            }
        }
    }

}
