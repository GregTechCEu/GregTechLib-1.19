package com.gregtechceu.gtlib.syncdata.managed;

/**
 * @author KilaBash
 * @date 2023/2/21
 * @implNote IManagedArrayVar
 */
public interface IManagedArrayVar<T> extends IManagedVar<T> {
    T value(int index);

    void set(int index, T value);

    Class<T> getChildrenType();

}
