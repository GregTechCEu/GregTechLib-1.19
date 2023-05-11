package com.gregtechceu.gtlib.gui.factory;

import com.gregtechceu.gtlib.gui.modular.IUIHolder;
import com.gregtechceu.gtlib.gui.modular.ModularUI;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class BlockEntityUIFactory extends UIFactory<BlockEntity>{
    public static final BlockEntityUIFactory INSTANCE  = new BlockEntityUIFactory();

    private BlockEntityUIFactory() {
        super();
    }

    @Override
    protected ModularUI createUITemplate(BlockEntity holder, Player entityPlayer) {
        if (holder instanceof IUIHolder) {
            return ((IUIHolder) holder).createUI(entityPlayer);
        }
        return null;
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected BlockEntity readHolderFromSyncData(FriendlyByteBuf syncData) {
        Level world = Minecraft.getInstance().level;
        return world == null ? null : world.getBlockEntity(syncData.readBlockPos());
    }

    @Override
    protected void writeHolderToSyncData(FriendlyByteBuf syncData, BlockEntity holder) {
        syncData.writeBlockPos(holder.getBlockPos());
    }
}
