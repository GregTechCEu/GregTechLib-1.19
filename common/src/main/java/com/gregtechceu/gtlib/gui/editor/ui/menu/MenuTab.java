package com.gregtechceu.gtlib.gui.editor.ui.menu;

import com.gregtechceu.gtlib.gui.editor.ColorPattern;
import com.gregtechceu.gtlib.gui.editor.IRegisterUI;
import com.gregtechceu.gtlib.gui.editor.ui.Editor;
import com.gregtechceu.gtlib.gui.texture.TextTexture;
import com.gregtechceu.gtlib.gui.util.TreeBuilder;
import com.gregtechceu.gtlib.gui.widget.ButtonWidget;
import com.gregtechceu.gtlib.gui.widget.Widget;
import com.gregtechceu.gtlib.utils.LocalizationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * @author KilaBash
 * @date 2022/12/17
 * @implNote MenuTab
 */
public abstract class MenuTab implements IRegisterUI {
    private final static Map<String, List<BiConsumer<MenuTab, TreeBuilder.Menu>>> HOOKS = new LinkedHashMap<>();

    protected Editor editor;

    protected MenuTab() {
        this.editor = Editor.INSTANCE;
        if (this.editor == null) {
            throw new RuntimeException("editor is null while creating a menu tab %s".formatted(name()));
        }
    }

    public static void registerMenuHook(String menuName, BiConsumer<MenuTab, TreeBuilder.Menu> consumer) {
        HOOKS.computeIfAbsent(menuName, n -> new ArrayList<>()).add(consumer);
    }

    public TreeBuilder.Menu appendMenu(TreeBuilder.Menu menu) {
        for (var hook : HOOKS.getOrDefault(name(), Collections.emptyList())) {
            hook.accept(this, menu);
        }
        return menu;
    }

    @Environment(EnvType.CLIENT)
    public Widget createTabWidget() {
        int width = Minecraft.getInstance().font.width(LocalizationUtils.format(getTranslateKey()));
        var button = new ButtonWidget(0, 0, width + 6, 16, new TextTexture(getTranslateKey()), null)
                .setHoverTexture(ColorPattern.T_WHITE.rectTexture(), new TextTexture(getTranslateKey()));
        button.setOnPressCallback(cd -> {
            var pos = button.getPosition();
            editor.openMenu(pos.x, pos.y + 14, appendMenu(createMenu()));
        });
        return button.setClientSideWidget();
    }

    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }

    public void deserializeNBT(CompoundTag nbt) {

    }

    abstract protected TreeBuilder.Menu createMenu();
}
