package com.gregtechceu.gtlib.client.model.forge;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.gregtechceu.gtlib.client.model.custommodel.CustomBakedModel;
import com.gregtechceu.gtlib.client.renderer.IBlockRendererProvider;
import com.gregtechceu.gtlib.client.renderer.IRenderer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GTLibRendererModel implements IUnbakedGeometry<GTLibRendererModel> {
    public static final GTLibRendererModel INSTANCE = new GTLibRendererModel();

    private GTLibRendererModel() {}

    @Override
    public BakedModel bake(IGeometryBakingContext iGeometryBakingContext, ModelBakery arg, Function<Material, TextureAtlasSprite> function, ModelState arg2, ItemOverrides arg3, ResourceLocation arg4) {
        return new RendererBakedModel();
    }

    @Override
    public Collection<Material> getMaterials(IGeometryBakingContext iGeometryBakingContext, Function<ResourceLocation, UnbakedModel> function, Set<Pair<String, String>> set) {
        return Collections.emptyList();
    }

    public static final class RendererBakedModel implements BakedModel {

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
            return Collections.emptyList();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return false;
        }

        @Override
        public boolean isGui3d() {
            return true;
        }

        @Override
        public boolean usesBlockLight() {
            return false;
        }

        @Override
        public boolean isCustomRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(MissingTextureAtlasSprite.getLocation());
        }

        @Override
        public ItemOverrides getOverrides() {
            return ItemOverrides.EMPTY;
        }

        // forge

        public static final ModelProperty<IRenderer> IRENDERER = new ModelProperty<>();
        public static final ModelProperty<BlockAndTintGetter> WORLD = new ModelProperty<>();
        public static final ModelProperty<BlockPos> POS = new ModelProperty<>();
        //TODO Model Data
//        public static final ModelProperty<ModelData> MODEL_DATA = new ModelProperty<>();


        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
            IRenderer renderer = data.get(IRENDERER);
            BlockAndTintGetter world = data.get(WORLD);
            BlockPos pos = data.get(POS);
//            ModelData modelData = data.get(MODEL_DATA);
            if (renderer != null) {
                var quads = renderer.renderModel(world, pos, state, side, rand);
                if (renderer.reBakeCustomQuads() && state != null && world != null && pos != null) {
                    return CustomBakedModel.reBakeCustomQuads(quads, world, pos, state, side);
                }
                return quads;
            }
            return Collections.emptyList();
        }

        @Override
        public boolean useAmbientOcclusion(BlockState state) {
            if (state.getBlock() instanceof IBlockRendererProvider rendererProvider) {
                IRenderer renderer = rendererProvider.getRenderer(state);
                if (renderer != null) {
                    return renderer.useAO(state);
                }
            }
            return useAmbientOcclusion();
        }


        @Override
        public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
            if (state.getBlock() instanceof IBlockRendererProvider rendererProvider) {
                IRenderer renderer = rendererProvider.getRenderer(state);
                if (renderer != null) {
                    modelData = ModelData.builder()
                            .with(IRENDERER, renderer)
                            .with(WORLD, level)
                            .with(POS, pos)
//                            .with(MODEL_DATA, modelData)
                            .build();
                }
            }
            return modelData;
        }

        @Override
        public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
            IRenderer renderer = data.get(IRENDERER);
            if (renderer != null) {
                return renderer.getParticleTexture();
            }
            return BakedModel.super.getParticleIcon(data);
        }

    }

    public static final class Loader implements IGeometryLoader<GTLibRendererModel> {

        public static final Loader INSTANCE = new Loader();
        private Loader() {}

        @Override
        public GTLibRendererModel read(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return GTLibRendererModel.INSTANCE;
        }
    }
}
