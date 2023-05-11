package com.gregtechceu.gtlib.gui.modular;

import com.google.common.base.Preconditions;
import com.gregtechceu.gtlib.GTLib;
import com.gregtechceu.gtlib.gui.texture.IGuiTexture;
import com.gregtechceu.gtlib.gui.widget.SlotWidget;
import com.gregtechceu.gtlib.gui.widget.Widget;
import com.gregtechceu.gtlib.gui.widget.WidgetGroup;
import com.gregtechceu.gtlib.utils.Position;
import com.gregtechceu.gtlib.utils.Size;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import javax.annotation.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Author: KilaBash
 * Date: 2022/04/23
 * Description: ModularUI.
 */
public final class ModularUI {

    private final HashMap<Slot, SlotWidget> slotMap = new LinkedHashMap<>();
    public final WidgetGroup mainGroup;
    private int screenWidth, screenHeight;
    private int width, height;
    @Getter
    private boolean fullScreen;
    @Environment(EnvType.CLIENT)
    private ModularUIGuiContainer guiContainer;
    private ModularUIContainer container;
    private final List<Runnable> uiCloseCallback;
    private long tickCount;

    /**
     * UIHolder of this modular UI
     */
    public final IUIHolder holder;
    public final Player entityPlayer;

    public ModularUI(int width, int height, IUIHolder holder, Player entityPlayer) {
        this(new WidgetGroup(Position.ORIGIN, new Size(width, height)), holder, entityPlayer);
    }

    public ModularUI(WidgetGroup mainGroup, IUIHolder holder, Player entityPlayer) {
        this.mainGroup = mainGroup;
        mainGroup.setSelfPosition(Position.ORIGIN);
        this.width = mainGroup.getSize().width;
        this.height = mainGroup.getSize().height;
        this.holder = holder;
        this.entityPlayer = entityPlayer;
        this.uiCloseCallback = new ArrayList<>();
    }

    public ModularUI(IUIHolder holder, Player entityPlayer) {
        this(0, 0, holder, entityPlayer);
        fullScreen = true;
    }

    public void setFullScreen() {
        this.fullScreen = true;
        setSize(getScreenWidth(), getScreenHeight());
    }

    public HashMap<Slot, SlotWidget> getSlotMap() {
        return slotMap;
    }

    @Nullable
    public Widget getFirstWidgetById(String regex) {
        return mainGroup.getFirstWidgetById(Pattern.compile(regex));
    }

    public List<Widget> getWidgetsById(String regex) {
        return mainGroup.getWidgetsById(Pattern.compile(regex));
    }

    public ModularUIContainer getModularUIContainer() {
        return container;
    }

    //WARNING! WIDGET CHANGES SHOULD BE *STRICTLY* SYNCHRONIZED BETWEEN SERVER AND CLIENT,
    //OTHERWISE ID MISMATCH CAN HAPPEN BETWEEN ASSIGNED SLOTS!
    public void addNativeSlot(Slot slotHandle, SlotWidget slotWidget) {
        if (this.slotMap.containsKey(slotHandle)) {
            GTLib.LOGGER.error("duplicated slot {}, {}", slotHandle, slotWidget);
        }
        this.slotMap.put(slotHandle, slotWidget);
        if (container != null) {
            container.addSlot(slotHandle);
        }
    }

    //WARNING! WIDGET CHANGES SHOULD BE *STRICTLY* SYNCHRONIZED BETWEEN SERVER AND CLIENT,
    //OTHERWISE ID MISMATCH CAN HAPPEN BETWEEN ASSIGNED SLOTS!
    public void removeNativeSlot(Slot slotHandle) {
        if (this.slotMap.containsKey(slotHandle)) {
            this.slotMap.remove(slotHandle);
            if (container != null) {
                container.removeSlot(slotHandle);
            }
        }
    }

    public void setModularUIContainer(ModularUIContainer container) {
        this.container = container;
        for (Slot slot : slotMap.keySet()) {
            this.container.addSlot(slot);
        }
    }


    public void registerCloseListener(Runnable runnable) {
        uiCloseCallback.add(runnable);
    }

    public void triggerCloseListeners() {
        uiCloseCallback.forEach(Runnable::run);
    }

    @Environment(EnvType.CLIENT)
    public ModularUIGuiContainer getModularUIGui() {
        return guiContainer;
    }

    @Environment(EnvType.CLIENT)
    public void setModularUIGui(ModularUIGuiContainer modularUIGuiContainer) {
        this.guiContainer = modularUIGuiContainer;
    }

    public List<Widget> getFlatVisibleWidgetCollection() {
        List<Widget> widgetList = new ArrayList<>();

        for (Widget widget : mainGroup.widgets) {
            if (!widget.isVisible()) continue;
            widgetList.add(widget);

            if (widget instanceof WidgetGroup)
                widgetList.addAll(((WidgetGroup) widget).getContainedWidgets(false));
        }

        return widgetList;
    }


    public long getTickCount() {
        return tickCount;
    }

    void addTick() {
        this.tickCount += 1;
    }
    
    public List<Widget> getFlatWidgetCollection() {
        List<Widget> widgetList = new ArrayList<>();
        for (Widget widget : mainGroup.widgets) {
            widgetList.add(widget);
            if (widget instanceof WidgetGroup) {
                widgetList.addAll(((WidgetGroup) widget).getContainedWidgets(true));
            }
        }
        return widgetList;
    }

    @Environment(EnvType.CLIENT)
    public void setSize(int width, int height) {
        if (this.width != width || this.height != height) {
            this.width = width;
            this.height = height;
            mainGroup.setSize(new Size(width, height));
            getModularUIGui().init();
        }
    }

    @Environment(EnvType.CLIENT)
    public void updateScreenSize(int screenWidth, int screenHeight) {
        if (fullScreen && (screenWidth != width || screenHeight != height)) {
            width = screenWidth;
            height = screenHeight;
            getModularUIGui().init();
            return;
        }
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        Position displayOffset = new Position(getGuiLeft(), getGuiTop());
        mainGroup.setParentPosition(displayOffset);
        mainGroup.onScreenSizeUpdate(screenWidth, screenHeight);
    }

    public void initWidgets() {
        mainGroup.setGui(this);
        mainGroup.initWidget();
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getGuiLeft() {
        return (getScreenWidth() - getWidth()) / 2;
    }

    public int getGuiTop() {
        return (getScreenHeight() - getHeight()) / 2;
    }

    public Rectangle toScreenCoords(Rectangle widgetRect) {
        return new Rectangle(getGuiLeft() + widgetRect.x, getGuiTop() + widgetRect.y, widgetRect.width, widgetRect.height);
    }

    public ModularUI widget(Widget widget) {
        Preconditions.checkNotNull(widget);
        mainGroup.addWidget(widget);
        return this;
    }

    public ModularUI background(IGuiTexture... textures) {
        mainGroup.setBackground(textures);
        return this;
    }
}
