package com.gregtechceu.gtlib.client.model.custommodel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gregtechceu.gtlib.GTLib;
import com.gregtechceu.gtlib.client.model.ModelFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class GTLibMetadataSection {
    public static final String SECTION_NAME = GTLib.MOD_ID;
    private static final Map<ResourceLocation, GTLibMetadataSection> METADATA_CACHE = new HashMap<>();

    public final boolean emissive;
    public final ResourceLocation connection;

    public GTLibMetadataSection(boolean emissive, ResourceLocation connection) {
        this.emissive = emissive;
        this.connection = connection;
    }

    @Nullable
    public static GTLibMetadataSection getMetadata(ResourceLocation res) {
        if (METADATA_CACHE.containsKey(res)) {
            return METADATA_CACHE.get(res);
        }
        GTLibMetadataSection ret = null;
        var resourceOptional = Minecraft.getInstance().getResourceManager().getResource(res);
        if (resourceOptional.isPresent()) {
            var resource = resourceOptional.get();
            try {
                ret = resource.metadata().getSection(Serializer.INSTANCE).get();
            } catch (Exception ignored) {
            }
        }
        METADATA_CACHE.put(res, ret);
        return ret;
    }

    @Nullable
    public static GTLibMetadataSection getMetadata(TextureAtlasSprite sprite) {
        return getMetadata(spriteToAbsolute(sprite.getName()));
    }

    public static boolean isEmissive(TextureAtlasSprite sprite) {
        GTLibMetadataSection ret = getMetadata(spriteToAbsolute(sprite.getName()));
        return ret != null && ret.emissive;
    }

    @Nullable
    public static TextureAtlasSprite getConnection(TextureAtlasSprite sprite) {
        GTLibMetadataSection ret = getMetadata(spriteToAbsolute(sprite.getName()));
        return (ret == null || ret.connection == null) ? null : ModelFactory.getBlockSprite(ret.connection);
    }

    public static ResourceLocation spriteToAbsolute(ResourceLocation sprite) {
        if (!sprite.getPath().startsWith("textures/")) {
            sprite = new ResourceLocation(sprite.getNamespace(), "textures/" + sprite.getPath());
        }
        if (!sprite.getPath().endsWith(".png")) {
            sprite = new ResourceLocation(sprite.getNamespace(), sprite.getPath() + ".png");
        }
        return sprite;
    }

    public static class Serializer implements MetadataSectionSerializer<GTLibMetadataSection> {
        static Serializer INSTANCE = new Serializer();

        @Override
        @Nonnull
        public String getMetadataSectionName() {
            return SECTION_NAME;
        }

        @Override
        @Nonnull
        public GTLibMetadataSection fromJson(@Nonnull JsonObject json) {
            boolean emissive = false;
            ResourceLocation connection = null;
            if (json.isJsonObject()) {
                JsonObject obj = json.getAsJsonObject();
                if (obj.has("emissive")) {
                    JsonElement element = obj.get("emissive");
                    if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
                        emissive = element.getAsBoolean();
                    }
                }
                if (obj.has("connection")) {
                    JsonElement element = obj.get("connection");
                    if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                        connection = new ResourceLocation(element.getAsString());
                    }
                }
            }
            return new GTLibMetadataSection(emissive, connection);
        }
    }
}
