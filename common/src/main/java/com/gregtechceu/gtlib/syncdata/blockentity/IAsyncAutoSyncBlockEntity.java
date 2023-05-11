package com.gregtechceu.gtlib.syncdata.blockentity;

import com.gregtechceu.gtlib.Platform;
import com.gregtechceu.gtlib.async.AsyncThreadData;
import com.gregtechceu.gtlib.async.IAsyncLogic;
import com.gregtechceu.gtlib.networking.GTLibNetworking;
import com.gregtechceu.gtlib.networking.s2c.SPacketManagedPayload;
import com.gregtechceu.gtlib.syncdata.managed.IRef;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;

/**
 * @author KilaBash
 * @date 2022/9/7
 * @implNote IAsyncAutoSyncBlockEntity
 */
public interface IAsyncAutoSyncBlockEntity extends IAutoSyncBlockEntity, IAsyncLogic {

    default boolean useAsyncThread() {
        return true;
    }

    default void onValid() {
        if (useAsyncThread() && getSelf().getLevel() instanceof ServerLevel serverLevel) {
            AsyncThreadData.getOrCreate(serverLevel).addAsyncLogic(this);
        }
    }

    default void onInValid() {
        if (getSelf().getLevel() instanceof ServerLevel serverLevel) {
            AsyncThreadData.getOrCreate(serverLevel).removeAsyncLogic(this);
        }
    }

    @Override
    default void asyncTick(long periodID) {
        if (Platform.getMinecraftServer() == null) return;
        if (useAsyncThread() && !getSelf().isRemoved()) {
            for (IRef field : getNonLazyFields()) {
                field.update();
            }
            if (getRootStorage().hasDirtyFields()) {
                Platform.getMinecraftServer().execute(() -> {
                    var packet = SPacketManagedPayload.of(this, false);
                    GTLibNetworking.NETWORK.sendToTrackingChunk(packet, Objects.requireNonNull(this.getSelf().getLevel()).getChunkAt(this.getCurrentPos()));
                });
            }
        }
    }
}
