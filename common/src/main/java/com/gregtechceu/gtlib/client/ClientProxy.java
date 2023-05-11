package com.gregtechceu.gtlib.client;

import com.gregtechceu.gtlib.client.shader.Shaders;
import com.gregtechceu.gtlib.gui.util.DrawerHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ClientProxy {

    /**
     * should be called when Minecraft is prepared.
     */
    public static void init() {
        Shaders.init();
        DrawerHelper.init();
    }

}
