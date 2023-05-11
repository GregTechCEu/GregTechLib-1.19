package com.gregtechceu.gtlib.test.forge;

import com.gregtechceu.gtlib.test.TestBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;

/**
 * @author KilaBash
 * @date 2023/3/24
 * @implNote TestBlockEntityImpl
 */
public class TestBlockEntityImpl {
    public static RegistryObject<BlockEntityType<TestBlockEntity>> TYPE;

    public static BlockEntityType<?> TYPE() {
        return TYPE.get();
    }
}
