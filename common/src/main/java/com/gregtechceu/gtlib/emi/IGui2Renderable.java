package com.gregtechceu.gtlib.emi;

import com.gregtechceu.gtlib.gui.texture.IGuiTexture;
import dev.emi.emi.api.render.EmiRenderable;

/**
 * @author KilaBash
 * @date: 2022/04/30
 * @implNote IGui2Renderable
 */
public interface IGui2Renderable {
    static EmiRenderable toDrawable(IGuiTexture guiTexture, int width, int height) {
        return (matrices, x, y, delta) -> {
            if (guiTexture == null) return;
            guiTexture.draw(matrices, 0, 0, x, y, width, height);
        };
    }
}
