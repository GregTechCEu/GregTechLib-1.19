package com.gregtechceu.gtlib.syncdata.blockentity;

import com.gregtechceu.gtlib.networking.GTLibNetworking;
import com.gregtechceu.gtlib.networking.s2c.SPacketManagedPayload;
import com.gregtechceu.gtlib.syncdata.annotation.DescSynced;
import com.gregtechceu.gtlib.syncdata.annotation.LazyManaged;
import com.gregtechceu.gtlib.syncdata.managed.IRef;
import net.minecraft.nbt.CompoundTag;

import java.util.Objects;

/**
 * A block entity that can be automatically synced with the client.
 *
 * @see DescSynced
 * @see LazyManaged
 */
public interface IAutoSyncBlockEntity extends IManagedBlockEntity {

    /**
     * do a sync now. if the block entity is tickable then this would be handled automatically, I think.
     *
     * @param force if true, all fields will be synced, otherwise only the ones that have changed will be synced
     */
    default void syncNow(boolean force) {
        var level = Objects.requireNonNull(getSelf().getLevel());
        if (level.isClientSide) {
            throw new IllegalStateException("Cannot sync from client");
        }
        for (IRef field : this.getNonLazyFields()) {
            field.update();
        }
        var packet = SPacketManagedPayload.of(this, force);
        GTLibNetworking.NETWORK.sendToTrackingChunk(packet, level.getChunkAt(this.getCurrentPos()));
    }


    default void defaultServerTick() {
        for (IRef field : getNonLazyFields()) {
            field.update();
        }
        if (getRootStorage().hasDirtyFields()) {
            var packet = SPacketManagedPayload.of(this, false);
            GTLibNetworking.NETWORK.sendToTrackingChunk(packet, Objects.requireNonNull(this.getSelf().getLevel()).getChunkAt(this.getCurrentPos()));
        }
    }

    /**
     * write custom data to the packet
     */
    default void writeCustomSyncData(CompoundTag tag) {
    }

    /**
     * read custom data from the packet
     */
    default void readCustomSyncData(CompoundTag tag) {
    }


    /**
     * sync tag name
     */
    default String getSyncTag() {
        return "sync";
    }
}
