package com.gregtechceu.gtlib.core.mixins;

import com.gregtechceu.gtlib.GTLib;
import com.gregtechceu.gtlib.utils.CustomResourcePack;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ReloadableResourceManager.class)
public abstract class ReloadableResourceManagerMixin {

    @ModifyVariable(method = "createReload", at = @At("HEAD"), index = 4, argsOnly = true)
    private List<PackResources> injectCreateReload(List<PackResources> resourcePacks) {
        var mutableList = new ArrayList<>(resourcePacks);
        mutableList.add(new CustomResourcePack(GTLib.location, GTLib.MOD_ID, PackType.CLIENT_RESOURCES));
        return mutableList;
    }

}
