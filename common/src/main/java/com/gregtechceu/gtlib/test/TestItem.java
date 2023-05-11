package com.gregtechceu.gtlib.test;

import com.gregtechceu.gtlib.client.renderer.IItemRendererProvider;
import com.gregtechceu.gtlib.client.renderer.IRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * @author KilaBash
 * @date 2022/05/24
 * @implNote TestItem
 */
public class TestItem extends BlockItem implements IItemRendererProvider {

    public static final TestItem ITEM = new TestItem();

    private TestItem() {
        super(TestBlock.BLOCK, new Properties().tab(CreativeModeTab.TAB_REDSTONE));
    }

    @Override
    public IRenderer getRenderer(ItemStack stack) {
        return TestBlock.BLOCK.getRenderer(TestBlock.BLOCK.defaultBlockState());
    }
}
