package com.gregtechceu.gtlib.forge;

import com.gregtechceu.gtlib.GTLib;
import com.gregtechceu.gtlib.client.forge.ClientProxyImpl;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(GTLib.MOD_ID)
public class GTLibForge {
    public GTLibForge() {
        GTLib.init();
        DistExecutor.unsafeRunForDist(() -> ClientProxyImpl::new, () -> CommonProxyImpl::new);
    }
}
