package com.gregtechceu.gtlib.gui.widget;

import com.gregtechceu.gtlib.gui.editor.annotation.RegisterUI;
import com.gregtechceu.gtlib.gui.editor.configurator.Configurator;
import com.gregtechceu.gtlib.gui.editor.configurator.ConfiguratorGroup;
import com.gregtechceu.gtlib.gui.editor.configurator.GuiTextureConfigurator;
import com.gregtechceu.gtlib.gui.editor.configurator.IConfigurableWidget;
import com.gregtechceu.gtlib.gui.texture.IGuiTexture;
import com.gregtechceu.gtlib.gui.texture.ProgressTexture;
import com.gregtechceu.gtlib.gui.texture.ResourceTexture;
import com.gregtechceu.gtlib.gui.texture.UIResourceTexture;
import com.gregtechceu.gtlib.utils.Position;
import com.gregtechceu.gtlib.utils.Size;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

@RegisterUI(name = "progress", group = "widget.basic")
public class ProgressWidget extends Widget implements IConfigurableWidget {
    public final static DoubleSupplier JEIProgress = () -> Math.abs(System.currentTimeMillis() % 2000) / 2000.;

    public DoubleSupplier progressSupplier;
    private Function<Double, String> dynamicHoverTips;

    private double lastProgressValue;

    public ProgressWidget() {
        this(JEIProgress, 0, 0, 40, 40, new ProgressTexture());
    }

    public ProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height, ResourceTexture fullImage) {
        super(new Position(x, y), new Size(width, height));
        this.progressSupplier = progressSupplier;
        this.backgroundTexture = new ProgressTexture(fullImage.getSubTexture(0.0, 0.0, 1.0, 0.5), fullImage.getSubTexture(0.0, 0.5, 1.0, 0.5));
        this.lastProgressValue = -1;
    }

    public ProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height, ProgressTexture progressBar) {
        super(new Position(x, y), new Size(width, height));
        this.progressSupplier = progressSupplier;
        this.backgroundTexture = progressBar;
        this.lastProgressValue = -1;
    }

    public ProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
        this.progressSupplier = progressSupplier;
    }

    public ProgressWidget setProgressBar(IGuiTexture emptyBarArea, IGuiTexture filledBarArea) {
        this.backgroundTexture = new ProgressTexture(emptyBarArea, filledBarArea);
        return this;
    }

    public ProgressWidget setProgressBar(ProgressTexture progressBar) {
        this.backgroundTexture = progressBar;
        return this;
    }

    public ProgressWidget setDynamicHoverTips(Function<Double, String> hoverTips) {
        this.dynamicHoverTips = hoverTips;
        return this;
    }

    public ProgressWidget setFillDirection(ProgressTexture.FillDirection fillDirection) {
        if (this.backgroundTexture instanceof ProgressTexture progressTexture) {
            progressTexture.setFillDirection(fillDirection);
        }
        return this;
    }

    public ProgressWidget setProgressSupplier(DoubleSupplier progressSupplier) {
        this.progressSupplier = progressSupplier;
        return this;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void drawInForeground(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        if ((tooltipTexts.size() > 0 || dynamicHoverTips != null) && isMouseOverElement(mouseX, mouseY) && getHoverElement(mouseX, mouseY) == this && gui != null && gui.getModularUIGui() != null) {
            var tips = new ArrayList<>(tooltipTexts);
            if (dynamicHoverTips != null) {
                tips.add(Component.translatable(dynamicHoverTips.apply(lastProgressValue)));
            }
            gui.getModularUIGui().setHoverTooltip(tips, ItemStack.EMPTY, null, null);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void drawInBackground(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (progressSupplier == JEIProgress || isClientSideWidget) {
            lastProgressValue = progressSupplier.getAsDouble();
        }
        if (backgroundTexture instanceof ProgressTexture progressTexture) {
            progressTexture.setProgress(lastProgressValue);
        }
        super.drawInBackground(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void initWidget() {
        super.initWidget();
        this.lastProgressValue = progressSupplier.getAsDouble();
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        buffer.writeDouble(lastProgressValue);
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        lastProgressValue = buffer.readDouble();
    }

    @Override
    public void detectAndSendChanges() {
        double actualValue = progressSupplier.getAsDouble();
        if (actualValue - lastProgressValue != 0) {
            this.lastProgressValue = actualValue;
            writeUpdateInfo(0, buffer -> buffer.writeDouble(actualValue));
        }
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 0) {
            this.lastProgressValue = buffer.readDouble();
        }
    }

    @Override
    public void buildConfigurator(ConfiguratorGroup father) {
        IConfigurableWidget.super.buildConfigurator(father);
        if (father.getConfigurators().get(0)instanceof ConfiguratorGroup group) {
            for (Configurator configurator : group.getConfigurators()) {
                if (configurator instanceof GuiTextureConfigurator guiConfigurator&& configurator.getName().equals("gtlib.gui.editor.name.background")) {
                    guiConfigurator.setOnUpdate(t -> {
                        if (t instanceof ProgressTexture || (t instanceof UIResourceTexture uiResourceTexture && uiResourceTexture.getTexture() instanceof ProgressTexture)) {
                            this.backgroundTexture = t;
                        }
                    });
                    guiConfigurator.setAvailable(t -> t instanceof ProgressTexture || (t instanceof UIResourceTexture uiResourceTexture && uiResourceTexture.getTexture() instanceof ProgressTexture));
                    guiConfigurator.setTips("gtlib.gui.editor.tips.progress_texture");
                }
            }
        }
    }

    @Override
    public boolean canDragIn(Object dragging) {
        if (dragging instanceof IGuiTexture) {
            return dragging instanceof ProgressTexture || (dragging instanceof UIResourceTexture uiResourceTexture && uiResourceTexture.getTexture() instanceof ProgressTexture);
        }
        return IConfigurableWidget.super.canDragIn(dragging);
    }

    @Override
    public boolean handleDragging(Object dragging) {
        if (dragging instanceof IGuiTexture) {
            if (dragging instanceof ProgressTexture || (dragging instanceof UIResourceTexture uiResourceTexture && uiResourceTexture.getTexture() instanceof ProgressTexture) ) {
                this.backgroundTexture = (IGuiTexture) dragging;
                return true;
            }
            return false;
        } else return IConfigurableWidget.super.handleDragging(dragging);
    }

}
