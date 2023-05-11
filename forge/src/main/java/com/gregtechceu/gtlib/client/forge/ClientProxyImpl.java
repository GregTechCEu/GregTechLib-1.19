package com.gregtechceu.gtlib.client.forge;

import com.gregtechceu.gtlib.GTLib;
import com.gregtechceu.gtlib.Platform;
import com.gregtechceu.gtlib.client.ClientProxy;
import com.gregtechceu.gtlib.client.model.forge.GTLibRendererModel;
import com.gregtechceu.gtlib.client.renderer.IRenderer;
import com.gregtechceu.gtlib.client.shader.Shaders;
import com.gregtechceu.gtlib.forge.CommonProxyImpl;
import com.gregtechceu.gtlib.forge.jei.JEIClientEventHandler;
import com.gregtechceu.gtlib.test.TestBlock;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.function.Consumer;


@OnlyIn(Dist.CLIENT)
public class ClientProxyImpl extends CommonProxyImpl {

    public ClientProxyImpl() {
        super();
        // init
        if (GTLib.isJeiLoaded()) {
            MinecraftForge.EVENT_BUS.register(JEIClientEventHandler.class);
        }
    }

    @SubscribeEvent
    public void clientSetup(final FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            ClientProxy.init();
            if (Platform.isDevEnv()) {
                ItemBlockRenderTypes.setRenderLayer(TestBlock.BLOCK, RenderType.cutoutMipped());
            }
        });
    }

    @SubscribeEvent
    public void modelRegistry(final ModelEvent.RegisterGeometryLoaders e) {
        e.register("renderer", GTLibRendererModel.Loader.INSTANCE);
    }

    @SubscribeEvent
    public void shaderRegistry(RegisterShadersEvent event) {
        for (Pair<ShaderInstance, Consumer<ShaderInstance>> pair : Shaders.registerShaders(event.getResourceManager())) {
            event.registerShader(pair.getFirst(), pair.getSecond());
        }
    }

    @SubscribeEvent
    public void registerTextures(TextureStitchEvent.Pre event) {
        for (IRenderer renderer : IRenderer.EVENT_REGISTERS) {
            renderer.onPrepareTextureAtlas(event.getAtlas().location(), event::addSprite);
        }
    }

    @SubscribeEvent
    public void registerTextures(ModelEvent.RegisterAdditional event) {
        for (IRenderer renderer : IRenderer.EVENT_REGISTERS) {
            renderer.onAdditionalModel(event::register);
        }
    }
}
