package com.gregtechceu.gtlib.syncdata.managed;

import com.gregtechceu.gtlib.syncdata.SyncUtils;

class SimpleObjectRef extends ManagedRef {
    private Object oldValue;

    SimpleObjectRef(IManagedVar<?> field) {
        super(field);
        oldValue = getField().value();
    }

    @Override
    public void update() {
        Object newValue = getField().value();
        if ((oldValue == null && newValue != null) || (oldValue != null && newValue == null) || (oldValue != null && SyncUtils.isChanged(oldValue, newValue))) {
            oldValue = SyncUtils.copyWhenNecessary(newValue);
            setChanged(true);
        }
    }
}
