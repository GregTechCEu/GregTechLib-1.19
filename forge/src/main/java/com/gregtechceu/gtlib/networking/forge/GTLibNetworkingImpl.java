package com.gregtechceu.gtlib.networking.forge;

import com.gregtechceu.gtlib.networking.INetworking;
import net.minecraft.resources.ResourceLocation;

public class GTLibNetworkingImpl {

    public static INetworking createNetworking(ResourceLocation networking, String version) {
        return new Networking(networking, version);
    }

}
