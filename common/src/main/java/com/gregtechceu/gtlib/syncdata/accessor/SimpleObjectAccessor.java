package com.gregtechceu.gtlib.syncdata.accessor;

import com.gregtechceu.gtlib.syncdata.AccessorOp;
import com.gregtechceu.gtlib.syncdata.managed.IManagedVar;
import com.gregtechceu.gtlib.syncdata.payload.ITypedPayload;
import com.gregtechceu.gtlib.syncdata.payload.ObjectTypedPayload;
import com.gregtechceu.gtlib.syncdata.payload.PrimitiveTypedPayload;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;


public abstract class SimpleObjectAccessor extends ManagedAccessor {

    public static <T> SimpleObjectAccessor create(@NotNull Class<T> type, boolean inherited, Supplier<? extends ObjectTypedPayload<T>> payloadSupplier) {
        if(inherited) {
            return new SimpleObjectAccessor(type) {
                @Override
                public ObjectTypedPayload<?> createEmpty() {
                    return payloadSupplier.get();
                }

                @Override
                public boolean hasPredicate() {
                    return true;
                }

                @Override
                public boolean test(Class<?> test) {
                    return type.isAssignableFrom(test);
                }
            };
        }

        return new SimpleObjectAccessor(type) {
            @Override
            public ObjectTypedPayload<?> createEmpty() {
                return payloadSupplier.get();
            }
        };
    }
    private final Class<?>[] operandTypes;

    protected SimpleObjectAccessor(Class<?>... operandTypes) {
        this.operandTypes = operandTypes;
    }

    @Override
    public Class<?>[] operandTypes() {
        return operandTypes;
    }

    public abstract ObjectTypedPayload<?> createEmpty();

    @Override
    public ITypedPayload<?> readManagedField(AccessorOp op, IManagedVar<?> field) {
        var value = field.value();
        if (value != null) {
            //noinspection unchecked
            return ((ObjectTypedPayload)createEmpty()).setPayload(value);
        }
        return PrimitiveTypedPayload.ofNull();
    }

    @Override
    public void writeManagedField(AccessorOp op, IManagedVar<?> field, ITypedPayload<?> payload) {
        if (payload instanceof ObjectTypedPayload<?> object) {
            //noinspection unchecked
            ((IManagedVar<Object>) field).set(object.getPayload());
        }
        if(payload instanceof PrimitiveTypedPayload<?> primitive && primitive.isNull()) {
            field.set(null);
        }
    }

}
