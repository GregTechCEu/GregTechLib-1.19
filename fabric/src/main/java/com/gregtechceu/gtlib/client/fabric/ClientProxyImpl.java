package com.gregtechceu.gtlib.client.fabric;

import com.gregtechceu.gtlib.Platform;
import com.gregtechceu.gtlib.client.ClientCommands;
import com.gregtechceu.gtlib.client.ClientProxy;
import com.gregtechceu.gtlib.client.model.fabric.GTLibRendererModel;
import com.gregtechceu.gtlib.client.renderer.IRenderer;
import com.gregtechceu.gtlib.test.TestBlock;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/8
 * @implNote ClientProxyImpl
 */
@Environment(EnvType.CLIENT)
public class ClientProxyImpl implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Minecraft start
        ClientLifecycleEvents.CLIENT_STARTED.register(minecraft -> ClientProxy.init());

        // register custom model loader
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> GTLibRendererModel.Loader.INSTANCE);

        // register client commands
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            List<LiteralArgumentBuilder<FabricClientCommandSource>> commands = ClientCommands.createClientCommands();
            commands.forEach(dispatcher::register);
        });

        ClientSpriteRegistryCallback.event(InventoryMenu.BLOCK_ATLAS).register((atlas, registry) -> IRenderer.EVENT_REGISTERS.forEach(renderer -> renderer.onPrepareTextureAtlas(InventoryMenu.BLOCK_ATLAS, registry::register)));

        // render types
        if (Platform.isDevEnv()) {
            BlockRenderLayerMap.INSTANCE.putBlock(TestBlock.BLOCK, RenderType.cutoutMipped());
        }
    }

}
