package com.gregtechceu.gtlib.syncdata.accessor;

import com.gregtechceu.gtlib.syncdata.AccessorOp;
import com.gregtechceu.gtlib.syncdata.IAccessor;
import com.gregtechceu.gtlib.syncdata.managed.IManagedVar;
import com.gregtechceu.gtlib.syncdata.managed.IRef;
import com.gregtechceu.gtlib.syncdata.managed.ManagedRef;
import com.gregtechceu.gtlib.syncdata.payload.ITypedPayload;
import com.gregtechceu.gtlib.syncdata.payload.PrimitiveTypedPayload;

public abstract class ManagedAccessor implements IAccessor {
    private byte defaultType = -1;

    @Override
    public byte getDefaultType() {
        return defaultType;
    }

    @Override
    public void setDefaultType(byte defaultType) {
        this.defaultType = defaultType;
    }

    @Override
    public boolean isManaged() {
        return true;
    }

    public abstract ITypedPayload<?> readManagedField(AccessorOp op, IManagedVar<?> field);

    public abstract void writeManagedField(AccessorOp op, IManagedVar<?> field, ITypedPayload<?> payload);

    @Override
    public ITypedPayload<?> readField(AccessorOp op, IRef field) {
        if (!(field instanceof ManagedRef syncedField)) {
            throw new IllegalArgumentException("Field %s is not a managed field".formatted(field));
        }
        var managedField = syncedField.getField();
        if (!managedField.isPrimitive() && managedField.value() == null) {
            return PrimitiveTypedPayload.ofNull();
        }
        return readManagedField(op, managedField);
    }


    @Override
    public void writeField(AccessorOp op, IRef field, ITypedPayload<?> payload) {
        if (!(field instanceof ManagedRef syncedField)) {
            throw new IllegalArgumentException("Field %s is not a managed field".formatted(field));
        }
        var managedField = syncedField.getField();
        writeManagedField(op, managedField, payload);
    }


}
