package com.gregtechceu.gtlib.core.mixins;

import com.gregtechceu.gtlib.utils.LocalizationUtils;
import net.minecraft.client.resources.language.ClientLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLanguage.class)
public abstract class LanguageMixin {

    @Inject(method = "getOrDefault", at = @At(value = "HEAD"), cancellable = true)
    private void injectGet(String pId, CallbackInfoReturnable<String> cir) {
        if (LocalizationUtils.RESOURCE != null && LocalizationUtils.RESOURCE.hasResource(pId)) {
            cir.setReturnValue(LocalizationUtils.RESOURCE.getResource(pId));
        }
    }

}
