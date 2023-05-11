package com.gregtechceu.gtlib.forge;

import com.gregtechceu.gtlib.CommonProxy;
import com.gregtechceu.gtlib.GTLib;
import com.gregtechceu.gtlib.Platform;
import com.gregtechceu.gtlib.ServerCommands;
import com.gregtechceu.gtlib.syncdata.TypedPayloadRegistries;
import com.gregtechceu.gtlib.test.TestBlock;
import com.gregtechceu.gtlib.test.TestBlockEntity;
import com.gregtechceu.gtlib.test.TestItem;
import com.gregtechceu.gtlib.test.forge.TestBlockEntityImpl;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class CommonProxyImpl {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, GTLib.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, GTLib.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, GTLib.MOD_ID);

    public CommonProxyImpl() {

        if (Platform.isDevEnv()) {
            BLOCKS.register("test", () -> TestBlock.BLOCK);
            ITEMS.register("test", () -> TestItem.ITEM);
            TestBlockEntityImpl.TYPE = BLOCK_ENTITY_TYPES.register("test", () -> BlockEntityType.Builder.of(TestBlockEntity::new, TestBlock.BLOCK).build(null));
        }

        // used for forge events (ClientProxy + CommonProxy)
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.register(this);
        // register server commands
        MinecraftForge.EVENT_BUS.addListener(this::registerCommand);
        // init common features
        CommonProxy.init();
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITY_TYPES.register(eventBus);
    }

    @SubscribeEvent
    public void loadComplete(FMLLoadCompleteEvent e) {
        e.enqueueWork(TypedPayloadRegistries::postInit);
    }

    public void registerCommand(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        ServerCommands.createServerCommands().forEach(dispatcher::register);
    }
}
