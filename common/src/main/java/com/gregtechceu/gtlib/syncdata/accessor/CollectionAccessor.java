package com.gregtechceu.gtlib.syncdata.accessor;


import com.gregtechceu.gtlib.syncdata.AccessorOp;
import com.gregtechceu.gtlib.syncdata.IAccessor;
import com.gregtechceu.gtlib.syncdata.TypedPayloadRegistries;
import com.gregtechceu.gtlib.syncdata.managed.ManagedHolder;
import com.gregtechceu.gtlib.syncdata.payload.ArrayPayload;
import com.gregtechceu.gtlib.syncdata.payload.ITypedPayload;

import java.util.Collection;

public class CollectionAccessor extends ReadonlyAccessor implements IArrayLikeAccessor {

    @Override
    public byte getDefaultType() {
        return TypedPayloadRegistries.getId(ArrayPayload.class);
    }

    private final IAccessor childAccessor;
    private final Class<?> childType;

    public CollectionAccessor(IAccessor childAccessor, Class<?> childType) {
        this.childAccessor = childAccessor;
        this.childType = childType;
    }

    @Override
    public boolean hasPredicate() {
        return true;
    }

    @Override
    public boolean test(Class<?> type) {
        return Collection.class.isAssignableFrom(type);
    }

    @Override
    public ITypedPayload<?> readFromReadonlyField(AccessorOp op, Object obj) {
        if (!(obj instanceof Collection<?> collection)) {
            throw new IllegalArgumentException("Field %s is not Collection".formatted(obj));
        }

        var iter = collection.iterator();

        var size = collection.size();
        var result = new ITypedPayload[size];
        if (!childAccessor.isManaged()) {
            throw new IllegalArgumentException("Child accessor %s is not managed".formatted(childAccessor));
        }


        for (int i = 0; i < size; i++) {
            var element = iter.next();
            var holder = ManagedHolder.of(element);
            var payload = childAccessor.readManagedField(op, holder);
            result[i] = payload;
        }

        return ArrayPayload.of(result);
    }

    @Override
    public void writeToReadonlyField(AccessorOp op, Object obj, ITypedPayload<?> payload) {
        if (!(obj instanceof Collection<?>)) {
            throw new IllegalArgumentException("Field %s is not Collection".formatted(obj));
        }

        if (!(payload instanceof ArrayPayload arrayPayload)) {
            throw new IllegalArgumentException("Payload %s is not ArrayPayload".formatted(payload));
        }
        var collection = (Collection<Object>) obj;

        var array = arrayPayload.getPayload();
        if (!childAccessor.isManaged()) {
            throw new IllegalArgumentException("Child accessor %s is not managed".formatted(childAccessor));
        }
        collection.clear();
        for (ITypedPayload<?> element : array) {
            var holder = ManagedHolder.ofType(childType);
            childAccessor.writeManagedField(op, holder, element);
            collection.add(holder.value());
        }
    }

    @Override
    public IAccessor getChildAccessor() {
        return childAccessor;
    }
}
