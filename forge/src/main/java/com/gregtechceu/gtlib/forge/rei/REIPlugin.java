package com.gregtechceu.gtlib.forge.rei;

import com.gregtechceu.gtlib.gui.modular.ModularUIGuiContainer;
import com.gregtechceu.gtlib.gui.modular.ModularUIReiHandlers;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.forge.REIPluginCommon;

/**
 * @author KilaBash
 * @date 2022/11/27
 * @implNote REIPlugin
 */
@REIPluginCommon
public class REIPlugin implements REIClientPlugin {

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerFocusedStack(ModularUIReiHandlers.FOCUSED_STACK_PROVIDER);
        registry.registerDraggableStackVisitor(ModularUIReiHandlers.DRAGGABLE_STACK_VISITOR);
    }

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(ModularUIGuiContainer.class, ModularUIReiHandlers.EXCLUSION_ZONES_PROVIDER);
    }
}
