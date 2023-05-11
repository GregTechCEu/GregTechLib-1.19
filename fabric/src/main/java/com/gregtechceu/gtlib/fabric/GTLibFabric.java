package com.gregtechceu.gtlib.fabric;

import com.gregtechceu.gtlib.CommonProxy;
import com.gregtechceu.gtlib.GTLib;
import com.gregtechceu.gtlib.Platform;
import com.gregtechceu.gtlib.ServerCommands;
import com.gregtechceu.gtlib.syncdata.TypedPayloadRegistries;
import com.gregtechceu.gtlib.test.TestBlock;
import com.gregtechceu.gtlib.test.TestBlockEntity;
import com.gregtechceu.gtlib.test.TestItem;
import com.gregtechceu.gtlib.test.fabric.TestBlockEntityImpl;
import com.gregtechceu.gtlib.utils.fabric.ReflectionUtilsImpl;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;

public class GTLibFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        GTLib.init();
        if (Platform.isDevEnv()) {
            Registry.register(Registry.BLOCK, GTLib.location("test"), TestBlock.BLOCK);
            Registry.register(Registry.ITEM, GTLib.location("test"), TestItem.ITEM);
            TestBlockEntityImpl.TYPE = Registry.register(
                    Registry.BLOCK_ENTITY_TYPE,
                    GTLib.location("test"),
                    FabricBlockEntityTypeBuilder.create(TestBlockEntity::new, TestBlock.BLOCK).build()
            );
        }
        // load entry points
        for (IGTLibPlugin gtlibPugin : FabricLoader.getInstance().getEntrypoints("gtlib_pugin", IGTLibPlugin.class)) {
            gtlibPugin.onLoad();
        }
        // hook server
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            PlatformImpl.SERVER = server;
        });
        // register server commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ServerCommands.createServerCommands().forEach(dispatcher::register));
        // init common features
        CommonProxy.init();
        // execute annotation searching
        ReflectionUtilsImpl.execute();
        // register payload
        TypedPayloadRegistries.postInit();
    }

}
