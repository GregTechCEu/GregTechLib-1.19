package com.gregtechceu.gtlib.networking;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

/**
 * @author KilaBash
 * @date 2023/2/9
 * @implNote IHandlerContext
 */
public interface IHandlerContext {
    Object getContext();
    boolean isClient();
    @Nullable Player getPlayer();
    @Nullable MinecraftServer getServer();
    Level getLevel();

}
