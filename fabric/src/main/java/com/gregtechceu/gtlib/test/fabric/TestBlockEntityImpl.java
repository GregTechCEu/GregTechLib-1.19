package com.gregtechceu.gtlib.test.fabric;

import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * @author KilaBash
 * @date 2023/3/24
 * @implNote TestBlockEntityImpl
 */
public class TestBlockEntityImpl {
    public static BlockEntityType<?> TYPE;

    public static BlockEntityType<?> TYPE() {
        return TYPE;
    }
}
