package com.gregtechceu.gtlib.syncdata.managed;

import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * @author KilaBash
 * @date 2023/2/21
 * @implNote IArrayRef
 */
public interface IArrayRef extends IRef {
    default void setChanged(int index) {
        setChanged(true);
    }

    IntSet getChanged();

}
