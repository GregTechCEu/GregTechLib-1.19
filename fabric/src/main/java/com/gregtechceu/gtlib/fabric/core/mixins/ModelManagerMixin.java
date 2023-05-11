package com.gregtechceu.gtlib.fabric.core.mixins;

import com.gregtechceu.gtlib.client.model.fabric.ModelFactoryImpl;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author KilaBash
 * @date 2023/2/8
 * @implNote ModelManagerMixin
 */
@Mixin(ModelManager.class)
public abstract class ModelManagerMixin {

    @Inject(method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Lnet/minecraft/client/resources/model/ModelBakery;", at = @At(value = "RETURN"))
    private <T extends BlockEntity> void injectPrepare(ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfoReturnable<ModelBakery> cir) {
        ModelFactoryImpl.BAKERY = cir.getReturnValue();
    }

}
