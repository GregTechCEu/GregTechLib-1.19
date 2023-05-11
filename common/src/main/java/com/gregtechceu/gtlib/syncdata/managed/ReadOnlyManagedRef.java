package com.gregtechceu.gtlib.syncdata.managed;

import net.minecraft.nbt.CompoundTag;

/**
 * @author KilaBash
 * @date 2023/2/19
 * @implNote ReadOnlyManagedRef
 */
public class ReadOnlyManagedRef extends ManagedRef {

    private boolean wasNull;
    private CompoundTag lastUid;

    ReadOnlyManagedRef(ReadOnlyManagedField field) {
        super(field);
        var current = getField().value();
        wasNull = current == null;
        if (current != null) {
            lastUid = getReadOnlyField().serializeUid(current);
        }
    }

    public ReadOnlyManagedField getReadOnlyField() {
        return ((ReadOnlyManagedField)field);
    }

    @Override
    public void update() {
        Object newValue = getField().value();
        if ((wasNull && newValue != null) || (!wasNull && newValue == null)) {
            setChanged(true);
        }
        wasNull = newValue == null;
        if (newValue != null) {
            var newUid = getReadOnlyField().serializeUid(newValue);
            if (!newUid.equals(lastUid) || getReadOnlyField().isDirty(newValue)) {
                setChanged(true);
            }
            lastUid = newUid;
        }
    }
}
