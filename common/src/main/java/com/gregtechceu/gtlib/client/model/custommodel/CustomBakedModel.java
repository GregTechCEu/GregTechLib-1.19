package com.gregtechceu.gtlib.client.model.custommodel;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.gregtechceu.gtlib.client.bakedpipeline.Quad;
import com.gregtechceu.gtlib.client.bakedpipeline.Submap;
import com.gregtechceu.gtlib.client.model.ModelFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Used to baked the model with emissive effect. or multi-layer
 *
 * Making the top layer emissive.
 */
@Environment(EnvType.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CustomBakedModel implements BakedModel {
    private final BakedModel parent;
    private final Table<Direction, Connections, List<BakedQuad>> sideCache;
    private final List<BakedQuad> noSideCache;

    public CustomBakedModel(BakedModel parent) {
        this.parent = parent;
        this.sideCache = Tables.newCustomTable(new EnumMap<>(Direction.class), HashMap::new);
        this.noSideCache = new ArrayList<>();
    }

    @Override
    @Nonnull
    @Deprecated
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return parent.getQuads(state, side, rand);
    }

    @Nonnull
    public List<BakedQuad> getCustomQuads(BlockAndTintGetter level, BlockPos pos, @Nonnull BlockState state, @Nullable Direction side, RandomSource rand) {
        var connections = Connections.checkConnections(level, pos, state, side);
        if (side == null) {
            if (noSideCache.isEmpty()) {
                noSideCache.addAll(buildCustomQuads(connections, parent.getQuads(state, null, rand)));
            }
            return noSideCache;
        }
        if (!sideCache.contains(side, connections)) {
            synchronized (sideCache) {
                sideCache.put(side, connections, buildCustomQuads(connections, parent.getQuads(state, side, rand)));
            }
        }
        return Objects.requireNonNull(sideCache.get(side, connections));
    }

    public static List<BakedQuad> reBakeCustomQuads(List<BakedQuad>quads, BlockAndTintGetter level, BlockPos pos, @Nonnull BlockState state, @Nullable Direction side) {
        return buildCustomQuads(Connections.checkConnections(level, pos, state, side), quads);
    }

    public static List<BakedQuad> buildCustomQuads(Connections connections, List<BakedQuad> base) {
        List<BakedQuad> result = new LinkedList<>();
        for (BakedQuad quad : base) {
            var section = GTLibMetadataSection.getMetadata(quad.getSprite());
            List<Quad> quads = bakeConnectionQuads(quad, connections, section == null ? null :
                    section.connection == null ? null :
                            ModelFactory.getBlockSprite(section.connection));
            if (section != null) {
                if (section.emissive) {
                    quads = quads.stream().map(q -> q.setLight(15, 15)).toList();
                }
            }
            for (Quad q : quads) {
                result.add(q.rebake());
            }
        }
        return result;
    }

    public static List<Quad> bakeConnectionQuads(BakedQuad bakedQuad, Connections connections, @Nullable TextureAtlasSprite texture) {
        if (!connections.isEmpty()) {
            var quads = Quad.from(bakedQuad).derotate().subdivide(4);
            if (texture != null) {
                if (connections.contains(Connection.UP) && connections.contains(Connection.LEFT) && connections.contains(Connection.UP_LEFT)) {
                    quads[2] = quads[2] == null ? null : quads[2].grow().transformUVs(texture, Submap.uvs[0]);
                } else if (connections.contains(Connection.UP) && !connections.contains(Connection.LEFT)) {
                    quads[2] = quads[2] == null ? null : quads[2].grow().transformUVs(texture, Submap.uvs[2]);
                } else if (!connections.contains(Connection.UP) && connections.contains(Connection.LEFT)) {
                    quads[2] = quads[2] == null ? null : quads[2].grow().transformUVs(texture, Submap.uvs[8]);
                } else if (connections.contains(Connection.UP) && connections.contains(Connection.LEFT) && !connections.contains(Connection.UP_LEFT)) {
                    quads[2] = quads[2] == null ? null : quads[2].grow().transformUVs(texture, Submap.uvs[10]);
                } else {
                    quads[2] = quads[2] == null ? null : quads[2].grow().transformUVs(bakedQuad.getSprite(), Submap.uvs[16]);
                }
                if (connections.contains(Connection.UP) && connections.contains(Connection.RIGHT) && connections.contains(Connection.UP_RIGHT)) {
                    quads[3] = quads[3] == null ? null : quads[3].grow().transformUVs(texture, Submap.uvs[1]);
                } else if (connections.contains(Connection.UP) && !connections.contains(Connection.RIGHT)) {
                    quads[3] = quads[3] == null ? null : quads[3].grow().transformUVs(texture, Submap.uvs[3]);
                } else if (!connections.contains(Connection.UP) && connections.contains(Connection.RIGHT)) {
                    quads[3] = quads[3] == null ? null : quads[3].grow().transformUVs(texture, Submap.uvs[9]);
                } else if (connections.contains(Connection.UP) && connections.contains(Connection.RIGHT) && !connections.contains(Connection.UP_RIGHT)) {
                    quads[3] = quads[3] == null ? null : quads[3].grow().transformUVs(texture, Submap.uvs[11]);
                } else {
                    quads[3] = quads[3] == null ? null : quads[3].grow().transformUVs(bakedQuad.getSprite(), Submap.uvs[17]);
                }
                if (connections.contains(Connection.DOWN) && connections.contains(Connection.RIGHT) && connections.contains(Connection.DOWN_RIGHT)) {
                    quads[0] = quads[0] == null ? null : quads[0].grow().transformUVs(texture, Submap.uvs[5]);
                } else if (connections.contains(Connection.DOWN) && !connections.contains(Connection.RIGHT)) {
                    quads[0] = quads[0] == null ? null : quads[0].grow().transformUVs(texture, Submap.uvs[7]);
                } else if (!connections.contains(Connection.DOWN) && connections.contains(Connection.RIGHT)) {
                    quads[0] = quads[0] == null ? null : quads[0].grow().transformUVs(texture, Submap.uvs[13]);
                } else if (connections.contains(Connection.DOWN) && connections.contains(Connection.RIGHT) && !connections.contains(Connection.DOWN_RIGHT)) {
                    quads[0] = quads[0] == null ? null : quads[0].grow().transformUVs(texture, Submap.uvs[15]);
                } else {
                    quads[0] = quads[0] == null ? null : quads[0].grow().transformUVs(bakedQuad.getSprite(), Submap.uvs[19]);
                }
                if (connections.contains(Connection.DOWN) && connections.contains(Connection.LEFT) && connections.contains(Connection.DOWN_LEFT)) {
                    quads[1] = quads[1] == null ? null : quads[1].grow().transformUVs(texture, Submap.uvs[4]);
                } else if (connections.contains(Connection.DOWN) && !connections.contains(Connection.LEFT)) {
                    quads[1] = quads[1] == null ? null : quads[1].grow().transformUVs(texture, Submap.uvs[6]);
                } else if (!connections.contains(Connection.DOWN) && connections.contains(Connection.LEFT)) {
                    quads[1] = quads[1] == null ? null : quads[1].grow().transformUVs(texture, Submap.uvs[12]);
                } else if (connections.contains(Connection.DOWN) && connections.contains(Connection.LEFT) && !connections.contains(Connection.DOWN_LEFT)) {
                    quads[1] = quads[1] == null ? null : quads[1].grow().transformUVs(texture, Submap.uvs[14]);
                } else {
                    quads[1] = quads[1] == null ? null : quads[1].grow().transformUVs(bakedQuad.getSprite(), Submap.uvs[18]);
                }
            }
            return Arrays.stream(quads).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return List.of(Quad.from(bakedQuad));
    }

    @Override
    public boolean useAmbientOcclusion() {
        return parent.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return parent.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return parent.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return parent.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return parent.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return parent.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return parent.getOverrides();
    }
}
