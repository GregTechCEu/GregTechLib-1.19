package com.gregtechceu.gtlib.gui.modular;


import net.minecraft.world.entity.player.Player;

public interface IUIHolder {
    IUIHolder EMPTY = new IUIHolder() {
        @Override
        public ModularUI createUI(Player entityPlayer) {
            return null;
        }

        @Override
        public boolean isInvalid() {
            return false;
        }

        @Override
        public boolean isRemote() {
            return true;
        }

        @Override
        public void markAsDirty() {

        }
    };

    ModularUI createUI(Player entityPlayer);

    boolean isInvalid();

    boolean isRemote();

    void markAsDirty();

}
