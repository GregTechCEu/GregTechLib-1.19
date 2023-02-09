package com.lowdragmc.lowdraglib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lowdragmc.lowdraglib.json.FluidStackTypeAdapter;
import com.lowdragmc.lowdraglib.json.IGuiTextureTypeAdapter;
import com.lowdragmc.lowdraglib.json.ItemStackTypeAdapter;
import com.simibubi.create.Create;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Random;

public class LDLib {
    public static final String MOD_ID = "ldlib";
    public static final String NAME = "LowDragLib";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
    public static final String MODID_JEI = "jei";
    public static final String MODID_RUBIDIUM = "rubidium";
    public static final String MODID_REI = "roughlyenoughitems";
    public static final Random random = new Random();
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(IGuiTextureTypeAdapter.INSTANCE)
            .registerTypeAdapter(ItemStack.class, ItemStackTypeAdapter.INSTANCE)
            .registerTypeAdapter(FluidStack.class, FluidStackTypeAdapter.INSTANCE)
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .create();
    public static File location;

    public static void init() {
        LOGGER.info("{} initializing! Create version: {} on platform: {}", NAME, Create.VERSION, Platform.platformName());
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
        if (!isReiLoaded() && isModLoaded(MODID_JEI)) return true;
        try {
            Class.forName("mezz.jei.core.config.GiveMode");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    public static boolean isReiLoaded() {
        if (isModLoaded(MODID_REI)) return true;
        try {
            Class.forName("me.shedaniel.rei.api.common.entry.EntryStack");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }
}