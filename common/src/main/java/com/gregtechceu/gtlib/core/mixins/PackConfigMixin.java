package com.gregtechceu.gtlib.core.mixins;

import com.gregtechceu.gtlib.GTLib;
import com.gregtechceu.gtlib.utils.CustomResourcePack;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(WorldLoader.PackConfig.class)
public abstract class PackConfigMixin {

    @ModifyVariable(method = "createResourceManager", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/MultiPackResourceManager;<init>(Lnet/minecraft/server/packs/PackType;Ljava/util/List;)V"), index = 2)
    private List<PackResources> injectCreateReload(List<PackResources> resourcePacks) {
        var mutableList = new ArrayList<>(resourcePacks);
        mutableList.add(new CustomResourcePack(GTLib.location, GTLib.MOD_ID, PackType.SERVER_DATA));
        return mutableList;
    }

}
