package com.gregtechceu.gtlib.gui.factory;

import com.gregtechceu.gtlib.gui.modular.IUIHolder;
import com.gregtechceu.gtlib.gui.modular.ModularUI;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * @author KilaBash
 * @date 2022/8/26
 * @implNote ItemUIFactory
 */
public class HeldItemUIFactory extends UIFactory<HeldItemUIFactory.HeldItemHolder> {

    public static final HeldItemUIFactory INSTANCE = new HeldItemUIFactory();

    public final boolean openUI(ServerPlayer player, InteractionHand hand) {
        return openUI(new HeldItemHolder(player, hand), player);
    }

    @Override
    protected ModularUI createUITemplate(HeldItemHolder holder, Player entityPlayer) {
        return holder.createUI(entityPlayer);
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected HeldItemHolder readHolderFromSyncData(FriendlyByteBuf syncData) {
        Player player = Minecraft.getInstance().player;
        return player == null ? null :new HeldItemHolder(player, syncData.readEnum(InteractionHand.class));
    }

    @Override
    protected void writeHolderToSyncData(FriendlyByteBuf syncData, HeldItemHolder holder) {
        syncData.writeEnum(holder.hand);
    }

    public static class HeldItemHolder implements IUIHolder{
        Player player;
        InteractionHand hand;
        ItemStack held;

        public HeldItemHolder(Player player, InteractionHand hand) {
            this.player = player;
            this.hand = hand;
            held = player.getItemInHand(hand);
        }

        @Override
        public ModularUI createUI(Player entityPlayer) {
            if (held.getItem() instanceof IHeldItemUIHolder itemUIHolder) {
                return itemUIHolder.createUI(entityPlayer, this);
            }
            return null;
        }

        @Override
        public boolean isInvalid() {
            return !ItemStack.isSameItemSameTags(player.getItemInHand(hand), held);
        }

        @Override
        public boolean isRemote() {
            return player.level.isClientSide;
        }

        @Override
        public void markAsDirty() {

        }

        public Player getPlayer() {
            return player;
        }

        public InteractionHand getHand() {
            return hand;
        }

        public ItemStack getHeld() {
            return held;
        }
    }

    public interface IHeldItemUIHolder {

        ModularUI createUI(Player entityPlayer, HeldItemHolder holder);

    }
}
