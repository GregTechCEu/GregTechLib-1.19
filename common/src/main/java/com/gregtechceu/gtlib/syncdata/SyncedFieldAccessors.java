package com.gregtechceu.gtlib.syncdata;

import com.gregtechceu.gtlib.syncdata.accessor.*;
import net.minecraft.Util;

import java.util.function.BiFunction;

public class SyncedFieldAccessors {

    public static final IAccessor INT_ACCESSOR = new PrimitiveAccessor.IntAccessor();
    public static final IAccessor LONG_ACCESSOR = new PrimitiveAccessor.LongAccessor();
    public static final IAccessor FLOAT_ACCESSOR = new PrimitiveAccessor.FloatAccessor();
    public static final IAccessor DOUBLE_ACCESSOR = new PrimitiveAccessor.DoubleAccessor();
    public static final IAccessor BOOLEAN_ACCESSOR = new PrimitiveAccessor.BooleanAccessor();
    public static final IAccessor BYTE_ACCESSOR = new PrimitiveAccessor.ByteAccessor();
    public static final IAccessor SHORT_ACCESSOR = new PrimitiveAccessor.ShortAccessor();
    public static final IAccessor CHAR_ACCESSOR = new PrimitiveAccessor.CharAccessor();


    public static final IAccessor ENUM_ACCESSOR = new EnumAccessor();
    public static final IAccessor TAG_SERIALIZABLE_ACCESSOR = new ITagSerializableAccessor();
    public static final IAccessor MANAGED_ACCESSOR = new IManagedAccessor();

    public static final IAccessor BLOCK_STATE_ACCESSOR = new BlockStateAccessor();
    public static final IAccessor RECIPE_ACCESSOR = new RecipeAccessor();
    public static final IAccessor POSITION_ACCESSOR = new PositionAccessor();
    public static final IAccessor COMPONENT_ACCESSOR = new ComponentAccessor();
    public static final IAccessor SIZE_ACCESSOR = new SizeAccessor();
    public static final IAccessor GUI_TEXTURE_ACCESSOR = new IGuiTextureAccessor();
    public static final IAccessor RESOURCE_LOCATION_ACCESSOR = new ResourceLocationAccessor();


    private static final BiFunction<IAccessor, Class<?>, IAccessor> ARRAY_ACCESSOR_FACTORY = Util.memoize(ArrayAccessor::new);
    private static final BiFunction<IAccessor, Class<?>, IAccessor> COLLECTION_ACCESSOR_FACTORY = Util.memoize(CollectionAccessor::new);
    public static IAccessor collectionAccessor(IAccessor childAccessor, Class<?> child) {
        return COLLECTION_ACCESSOR_FACTORY.apply(childAccessor, child);
    }
    public static IAccessor arrayAccessor(IAccessor childAccessor, Class<?> child) {
        return ARRAY_ACCESSOR_FACTORY.apply(childAccessor, child);
    }
}
