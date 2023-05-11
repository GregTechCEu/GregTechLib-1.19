package com.gregtechceu.gtlib;

import com.gregtechceu.gtlib.gui.editor.runtime.UIDetector;
import com.gregtechceu.gtlib.gui.factory.BlockEntityUIFactory;
import com.gregtechceu.gtlib.gui.factory.HeldItemUIFactory;
import com.gregtechceu.gtlib.gui.factory.UIEditorFactory;
import com.gregtechceu.gtlib.gui.factory.UIFactory;
import com.gregtechceu.gtlib.networking.GTLibNetworking;
import com.gregtechceu.gtlib.syncdata.TypedPayloadRegistries;


public class CommonProxy {
    public static void init() {
        GTLibNetworking.init();
        UIFactory.register(BlockEntityUIFactory.INSTANCE);
        UIFactory.register(HeldItemUIFactory.INSTANCE);
        UIFactory.register(UIEditorFactory.INSTANCE);
        UIDetector.init();
        TypedPayloadRegistries.init();
    }
}
