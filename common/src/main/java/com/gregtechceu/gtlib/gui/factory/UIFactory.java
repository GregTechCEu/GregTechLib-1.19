package com.gregtechceu.gtlib.gui.factory;

import com.gregtechceu.gtlib.Platform;
import com.gregtechceu.gtlib.core.mixins.accessor.ServerPlayerAccessor;
import com.gregtechceu.gtlib.gui.modular.ModularUI;
import com.gregtechceu.gtlib.gui.modular.ModularUIContainer;
import com.gregtechceu.gtlib.gui.modular.ModularUIGuiContainer;
import com.gregtechceu.gtlib.networking.GTLibNetworking;
import com.gregtechceu.gtlib.networking.s2c.SPacketUIOpen;
import com.gregtechceu.gtlib.side.ForgeEventHooks;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public abstract class UIFactory<T> {
    public final int uiFactoryId;
    public static final Int2ObjectMap<UIFactory<?>> FACTORIES = new Int2ObjectOpenHashMap<>();

    public UIFactory(){
        uiFactoryId = FACTORIES.size();
    }
    
    public static void register(UIFactory<?> factory) {
        FACTORIES.put(factory.uiFactoryId, factory);
    }

    public final boolean openUI(T holder, ServerPlayer player) {
        ModularUI uiTemplate = createUITemplate(holder, player);
        if (uiTemplate == null) return false;
        uiTemplate.initWidgets();

        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }
        ((ServerPlayerAccessor) player).callNextContainerCounter();
        int currentWindowId = ((ServerPlayerAccessor) player).getContainerCounter();

        FriendlyByteBuf serializedHolder = new FriendlyByteBuf(Unpooled.buffer());
        writeHolderToSyncData(serializedHolder, holder);
        ModularUIContainer container = new ModularUIContainer(uiTemplate, currentWindowId);

        //accumulate all initial updates of widgets in open packet
        uiTemplate.mainGroup.writeInitialData(serializedHolder);

        GTLibNetworking.NETWORK.sendToPlayer(new SPacketUIOpen(uiFactoryId, serializedHolder, currentWindowId), player);

        ((ServerPlayerAccessor) player).callInitMenu(container);
        player.containerMenu = container;

        //and fire forge event only in the end
        if (Platform.isForge()) {
            ForgeEventHooks.postPlayerContainerEvent(player, container);
        }
        return true;
    }

    @Environment(EnvType.CLIENT)
    public final void initClientUI(FriendlyByteBuf serializedHolder, int windowId) {
        T holder = readHolderFromSyncData(serializedHolder);
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer entityPlayer = minecraft.player;

        ModularUI uiTemplate = createUITemplate(holder, entityPlayer);
        if (uiTemplate == null) return;
        uiTemplate.initWidgets();
        ModularUIGuiContainer ModularUIGuiContainer = new ModularUIGuiContainer(uiTemplate, windowId);
        uiTemplate.mainGroup.readInitialData(serializedHolder);
        minecraft.setScreen(ModularUIGuiContainer);
        minecraft.player.containerMenu = ModularUIGuiContainer.getMenu();

    }

    protected abstract ModularUI createUITemplate(T holder, Player entityPlayer);

    @Environment(EnvType.CLIENT)
    protected abstract T readHolderFromSyncData(FriendlyByteBuf syncData);

    protected abstract void writeHolderToSyncData(FriendlyByteBuf syncData, T holder);

}
