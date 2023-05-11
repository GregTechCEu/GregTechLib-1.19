package com.gregtechceu.gtlib.test;

import com.gregtechceu.gtlib.GTLib;
import com.gregtechceu.gtlib.gui.editor.ui.Editor;
import com.gregtechceu.gtlib.gui.factory.BlockEntityUIFactory;
import com.gregtechceu.gtlib.gui.modular.IUIHolder;
import com.gregtechceu.gtlib.gui.modular.ModularUI;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author KilaBash
 * @date 2022/05/24
 * @implNote TODO
 */
public class TestBlockEntity extends BlockEntity implements IUIHolder {

    public TestBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(TYPE(), pWorldPosition, pBlockState);
    }

    @ExpectPlatform
    public static BlockEntityType<?> TYPE() {
        throw new AssertionError();
    }

    public void use(Player player) {
        if (!getLevel().isClientSide) {
            BlockEntityUIFactory.INSTANCE.openUI(this, (ServerPlayer) player);
        }
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(this, entityPlayer).widget(new Editor(GTLib.location));
    }

    @Override
    public boolean isInvalid() {
        return false;
    }

    @Override
    public boolean isRemote() {
        return level.isClientSide;
    }

    @Override
    public void markAsDirty() {

    }
}
