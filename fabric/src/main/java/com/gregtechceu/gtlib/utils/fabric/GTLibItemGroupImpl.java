package com.gregtechceu.gtlib.utils.fabric;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class GTLibItemGroupImpl {
    public static int expandArrayAndGetId() {
        ResourceLocation error = new ResourceLocation("if_you_see_this", "something_went_wrong");
        return FabricItemGroupBuilder.build(error, Items.AIR::getDefaultInstance).getId();
    }
}
