package com.gregtechceu.gtlib.networking;

import com.gregtechceu.gtlib.GTLib;
import com.gregtechceu.gtlib.networking.both.PacketRPCMethodPayload;
import com.gregtechceu.gtlib.networking.c2s.CPacketUIClientAction;
import com.gregtechceu.gtlib.networking.s2c.SPacketManagedPayload;
import com.gregtechceu.gtlib.networking.s2c.SPacketUIOpen;
import com.gregtechceu.gtlib.networking.s2c.SPacketUIWidgetUpdate;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.resources.ResourceLocation;

/**
 * Author: KilaBash
 * Date: 2022/04/27
 * Description:
 */
public class GTLibNetworking {

    public static final INetworking NETWORK = createNetworking(new ResourceLocation(GTLib.MOD_ID, "networking"), "0.0.1");

    @ExpectPlatform
    public static INetworking createNetworking(ResourceLocation networking, String version) {
        throw new AssertionError();
    }

    public static void init() {
        NETWORK.registerS2C(SPacketUIOpen.class);
        NETWORK.registerS2C(SPacketUIWidgetUpdate.class);
        NETWORK.registerS2C(SPacketManagedPayload.class);

        NETWORK.registerC2S(CPacketUIClientAction.class);

        NETWORK.registerBoth(PacketRPCMethodPayload.class);
    }

}
