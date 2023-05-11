package com.gregtechceu.gtlib.json;

import com.google.gson.JsonObject;
import com.gregtechceu.gtlib.gui.texture.IGuiTexture;
import com.gregtechceu.gtlib.gui.texture.ItemStackTexture;
import com.gregtechceu.gtlib.gui.texture.ResourceTexture;
import com.gregtechceu.gtlib.gui.texture.ShaderTexture;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

/**
 * @author KilaBash
 * @date 2022/10/20
 * @implNote IGuiTextureJsonUtils
 */
public class SimpleIGuiTextureJsonUtils {

    public static JsonObject toJson(IGuiTexture texture) {
        JsonObject jsonObject = new JsonObject();
        if (texture instanceof ResourceTexture resourceTexture) {
            jsonObject.addProperty("type", "resource");
            jsonObject.addProperty("res", resourceTexture.imageLocation.toString());
        } else if (texture instanceof ItemStackTexture itemStackTexture && itemStackTexture.itemStack.length > 0) {
            jsonObject.addProperty("type", "item");
            jsonObject.addProperty("res", Registry.ITEM.getKey(itemStackTexture.itemStack[0].getItem()).toString());
        } else if (texture instanceof ShaderTexture shaderTexture && shaderTexture.location != null) {
            jsonObject.addProperty("type", "shader");
            jsonObject.addProperty("res", shaderTexture.location.toString());
        }
        return jsonObject;
    }

    public static IGuiTexture fromJson(JsonObject jsonObject) {
        return switch (jsonObject.get("type").getAsString()) {
            case "resource" -> new ResourceTexture(jsonObject.get("res").getAsString());
            case "item" -> new ItemStackTexture(Registry.ITEM.get(new ResourceLocation(jsonObject.get("res").getAsString())));
            case "shader" -> ShaderTexture.createShader(new ResourceLocation(jsonObject.get("res").getAsString()));
            default -> IGuiTexture.EMPTY;
        };
    }
}
