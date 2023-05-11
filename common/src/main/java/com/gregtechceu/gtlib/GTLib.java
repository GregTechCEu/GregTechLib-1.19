package com.gregtechceu.gtlib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gregtechceu.gtlib.json.factory.FluidStackTypeAdapter;
import com.gregtechceu.gtlib.json.IGuiTextureTypeAdapter;
import com.gregtechceu.gtlib.json.ItemStackTypeAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Random;

public class GTLib {
    public static final String MOD_ID = "gtlib";
    public static final String NAME = "GregTechLib";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
    public static final String MODID_JEI = "jei";
    public static final String MODID_RUBIDIUM = "rubidium";
    public static final String MODID_REI = "roughlyenoughitems";
    public static final String MODID_EMI = "emi";
    public static final Random random = new Random();
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(IGuiTextureTypeAdapter.INSTANCE)
            .registerTypeAdapterFactory(FluidStackTypeAdapter.INSTANCE)
            .registerTypeAdapter(ItemStack.class, ItemStackTypeAdapter.INSTANCE)
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .create();
    public static File location;

    public static void init() {
        LOGGER.info("{} is initializing on platform: {}", NAME, Platform.platformName());
        location = new File(Platform.getGamePath().toFile(), "gtlib");
        if (location.mkdir()) {
            LOGGER.info("create gtlib config folder");
        }
    }

    public static ResourceLocation location(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static boolean isClient() {
        return Platform.isClient();
    }

    public static boolean isRemote() {
        if (isClient()) {
            return Minecraft.getInstance().isSameThread();
        }
        return false;
    }

    public static boolean isModLoaded(String mod) {
        return Platform.isModLoaded(mod);
    }

    public static boolean isJeiLoaded() {
        return !isReiLoaded() && isModLoaded(MODID_JEI);
    }

    public static boolean isReiLoaded() {
        return isModLoaded(MODID_REI);
    }

    public static boolean isEmiLoaded() {
        return isModLoaded(MODID_EMI);
    }
}
