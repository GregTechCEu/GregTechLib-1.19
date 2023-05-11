package com.gregtechceu.gtlib.syncdata.blockentity;

import com.gregtechceu.gtlib.networking.GTLibNetworking;
import com.gregtechceu.gtlib.networking.both.PacketRPCMethodPayload;
import com.gregtechceu.gtlib.syncdata.IManaged;
import com.gregtechceu.gtlib.syncdata.field.RPCMethodMeta;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.level.ServerPlayer;
import javax.annotation.Nullable;

public interface IRPCBlockEntity extends IManagedBlockEntity {

    /**
     * Get the RPC method
     */
    @Nullable
    default RPCMethodMeta getRPCMethod(IManaged managed, String methodName) {
        return managed.getFieldHolder().getRpcMethodMap().get(methodName);
    }

    default PacketRPCMethodPayload generateRpcPacket(IManaged managed, String methodName, Object... args) {
        return PacketRPCMethodPayload.of(managed, this, methodName,args);
    }

    @Environment(EnvType.CLIENT)
    default void rpcToServer(IManaged managed, String methodName, Object... args) {
        var packet = generateRpcPacket(managed, methodName, args);
        GTLibNetworking.NETWORK.sendToServer(packet);
    }


    default void rpcToPlayer(IManaged managed, ServerPlayer player, String methodName, Object... args) {
        var packet = generateRpcPacket(managed, methodName, args);
        GTLibNetworking.NETWORK.sendToPlayer(packet, player);
    }

    default void rpcToTracking(IManaged managed, ServerPlayer player, String methodName, Object... args) {
        var packet = generateRpcPacket(managed, methodName, args);
        GTLibNetworking.NETWORK.sendToTrackingChunk(packet, getSelf().getLevel().getChunkAt(getSelf().getBlockPos()));
    }

}
