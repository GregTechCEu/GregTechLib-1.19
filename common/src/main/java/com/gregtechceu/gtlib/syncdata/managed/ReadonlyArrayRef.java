package com.gregtechceu.gtlib.syncdata.managed;

import com.gregtechceu.gtlib.syncdata.IContentChangeAware;
import com.gregtechceu.gtlib.syncdata.IManaged;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.Collection;

public class ReadonlyArrayRef extends ReadonlyRef implements IArrayRef {
    private final IntSet dirty = new IntOpenHashSet();

    public ReadonlyArrayRef(boolean isLazy, Object value) {
        super(isLazy, value);
    }

    @Override
    protected void init() {
        var value = readRaw();
        if (value instanceof IContentChangeAware || value instanceof IManaged) {
            super.init();
            return;
        }
        if (isLazy()) {
            return;
        }
        var type = value.getClass();
        if (type.isArray()) {
            var componentType = type.getComponentType();
            if (IManaged.class.isAssignableFrom(componentType)) return;
            if (!IContentChangeAware.class.isAssignableFrom(componentType)) {
                throw new IllegalArgumentException("complex sync field must be an IContentChangeAware if not lazy!");
            }
            for (var handler : (IContentChangeAware[]) value) {
                replaceHandler(handler);
            }
            return;
        } else if (value instanceof Collection<?> collection) {
            for (var item : collection) {
                if (item instanceof IContentChangeAware handler) {
                    replaceHandler(handler);
                } else if (item instanceof IManaged) {

                } else {
                    throw new IllegalArgumentException("complex sync field must be an IContentChangeAware if not lazy!");
                }
            }

            return;
        }
        throw new IllegalArgumentException("Field must be an array or collection");
    }

    @Override
    public void update() {
        super.update();
        var value = readRaw();
        var type = value.getClass();
        if (type.isArray()) {
            var componentType = type.getComponentType();
            if (IManaged.class.isAssignableFrom(componentType)) {
                IManaged[] values = (IManaged[]) value;
                for (int i = 0; i < values.length; i++) {
                    var managed = values[i];
                    for (IRef field : managed.getSyncStorage().getNonLazyFields()) {
                        field.update();
                    }
                    if (managed.getSyncStorage().hasDirtyFields()) {
                        setChanged(i);
                    }
                }
            }
        } else if (value instanceof Collection<?> collection) {
            int i = 0;
            for (var item : collection) {
                if (item instanceof IManaged managed) {
                    for (IRef field : managed.getSyncStorage().getNonLazyFields()) {
                        field.update();
                    }
                    if (managed.getSyncStorage().hasDirtyFields()) {
                        setChanged(i);
                    }
                }
                i++;
            }
        }
    }

    @Override
    public void setChanged(boolean changed) {
        super.setChanged(changed);
        if (!changed) {
            dirty.clear();
        }
    }

    @Override
    public void setChanged(int index) {
        setChanged(true);
        dirty.add(index);
    }

    @Override
    public IntSet getChanged() {
        return dirty;
    }
}
