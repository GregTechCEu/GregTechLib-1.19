package com.gregtechceu.gtlib.syncdata.field;

import com.gregtechceu.gtlib.syncdata.AccessorOp;
import com.gregtechceu.gtlib.syncdata.IAccessor;
import com.gregtechceu.gtlib.syncdata.TypedPayloadRegistries;
import com.gregtechceu.gtlib.syncdata.accessor.IArrayLikeAccessor;
import com.gregtechceu.gtlib.syncdata.managed.*;
import com.gregtechceu.gtlib.syncdata.payload.ITypedPayload;
import lombok.Getter;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class ManagedKey {
    @Getter
    private final String name;
    @Getter
    private final boolean isDestSync;
    @Getter
    private final boolean isPersist;
    @Getter
    private final boolean isDrop;
    @Nullable
    @Getter
    private String persistentKey;
    @Getter
    private final boolean isLazy;
    @Getter
    private final Type contentType;
    @Getter
    private final Field rawField;
    @Getter
    private boolean isReadOnlyManaged;
    @Getter
    @Nullable
    private Method onDirtyMethod, serializeMethod, deserializeMethod;

    public void setPersistentKey(@Nullable String persistentKey) {
        this.persistentKey = persistentKey;
    }

    public void setRedOnlyManaged(Method onDirtyMethod, Method serializeMethod, Method deserializeMethod) {
        this.isReadOnlyManaged = true;
        this.onDirtyMethod = onDirtyMethod;
        this.serializeMethod = serializeMethod;
        this.deserializeMethod = deserializeMethod;
    }

    public ManagedKey(String name, boolean isDestSync, boolean isPersist, boolean isDrop, boolean isLazy, Type contentType, Field rawField) {
        this.name = name;
        this.isDestSync = isDestSync;
        this.isPersist = isPersist;
        this.isDrop = isDrop;
        this.isLazy = isLazy;
        this.contentType = contentType;
        this.rawField = rawField;
    }

    private IAccessor accessor;

    public IAccessor getAccessor() {
        if (accessor == null) {
            accessor = TypedPayloadRegistries.findByType(contentType);
        }
        return accessor;
    }

    public ITypedPayload<?> readSyncedField(IRef field, boolean force) {
        return getAccessor().readField(force ? AccessorOp.FORCE_SYNCED : AccessorOp.SYNCED, field);
    }

    public void writeSyncedField(IRef field, ITypedPayload<?> payload) {
        getAccessor().writeField(AccessorOp.SYNCED, field, payload);
    }

    public Tag readPersistedField(IRef field) {
        return getAccessor().readField(AccessorOp.PERSISTED, field).serializeNBT();
    }

    public void writePersistedField(IRef field, @NotNull Tag nbt) {
        var payloadType = getAccessor().getDefaultType();
        var payload = TypedPayloadRegistries.create(payloadType);
        payload.deserializeNBT(nbt);
        getAccessor().writeField(AccessorOp.PERSISTED, field, payload);
    }

    public IRef createRef(Object instance) {
        try {

            var accessor = getAccessor();

            if(accessor instanceof IArrayLikeAccessor arrayLikeAccessor) {

                if(accessor.isManaged() || arrayLikeAccessor.getChildAccessor().isManaged()) {
                    return new ManagedArrayLikeRef(ManagedField.of(rawField, instance), isLazy).setKey(this);
                }
//                else if (isReadOnlyManaged()) {
//                    return new ReadOnlyManagedArrayLikeRef(ReadOnlyManagedField.of(rawField, instance, onChangedMethod, serializeMethod, deserializeMethod), isLazy).setKey(this);
//                }
                try {
                    rawField.setAccessible(true);
                    return new ReadonlyArrayRef(isLazy, rawField.get(instance)).setKey(this);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            if (accessor.isManaged()) {
                return ManagedRef.create(ManagedField.of(rawField, instance), isLazy).setKey(this);
            } else if (isReadOnlyManaged()) {
                return ManagedRef.create(ReadOnlyManagedField.of(rawField, instance, onDirtyMethod, serializeMethod, deserializeMethod), isLazy).setKey(this);
            }
            try {
                rawField.setAccessible(true);
                return new ReadonlyRef(isLazy, rawField.get(instance)).setKey(this);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }catch (Exception e) {
            throw new IllegalStateException("Failed to create ref of " + this.name + " with type:" + this.rawField.getType().getCanonicalName(), e);
        }
    }
}
