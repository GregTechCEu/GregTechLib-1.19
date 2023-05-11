package com.gregtechceu.gtlib.syncdata;

/**
 * @author KilaBash
 * @date 2023/2/17
 * @implNote FieldUpdateListener
 */
@FunctionalInterface
public
interface IFieldUpdateListener<T> {
    void onFieldChanged(String changedField, T newValue, T oldValue);
}
