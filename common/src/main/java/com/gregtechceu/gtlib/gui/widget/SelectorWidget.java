package com.gregtechceu.gtlib.gui.widget;

import com.gregtechceu.gtlib.gui.editor.ColorPattern;
import com.gregtechceu.gtlib.gui.editor.annotation.*;
import com.gregtechceu.gtlib.gui.editor.configurator.ConfiguratorGroup;
import com.gregtechceu.gtlib.gui.editor.configurator.IConfigurableWidget;
import com.gregtechceu.gtlib.gui.modular.ModularUIGuiContainer;
import com.gregtechceu.gtlib.gui.texture.ColorRectTexture;
import com.gregtechceu.gtlib.gui.texture.IGuiTexture;
import com.gregtechceu.gtlib.gui.texture.TextTexture;
import com.gregtechceu.gtlib.utils.Position;
import com.gregtechceu.gtlib.utils.Size;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@RegisterUI(name = "selector", group = "widget.basic")
public class SelectorWidget extends WidgetGroup {
    protected List<SelectableWidgetGroup> selectables;
    @Configurable
    protected List<String> candidates;
    @Configurable
    protected String currentValue;
    @Configurable
    @NumberRange(range = {1, 20})
    protected int maxCount = 5;
    @Configurable
    @NumberColor
    protected int fontColor = -1;
    @Configurable
    protected boolean showUp;
    protected boolean isShow;
    protected IGuiTexture popUpTexture = new ColorRectTexture(0xAA000000);
    private Supplier<String> supplier;
    private Consumer<String> onChanged;
    public final TextTexture textTexture;
    protected final DraggableScrollableWidgetGroup popUp;
    protected final ButtonWidget button;

    public SelectorWidget() {
        this(0, 0, 60, 15, new ArrayList<>(List.of("A", "B", "C", "D", "E", "F", "G")), -1);
        setButtonBackground(ColorPattern.T_GRAY.rectTexture());
        setValue("D");
    }

    public SelectorWidget(int x, int y, int width, int height, List<String> candidates, int fontColor) {
        super(new Position(x, y), new Size(width, height));
        this.button = new ButtonWidget(0,0, width, height, textTexture = new TextTexture("", fontColor).setWidth(width).setType(TextTexture.TextType.ROLL), d -> {
            if (d.isRemote) setShow(!isShow);
        });
        this.candidates = candidates;
        this.selectables = new ArrayList<>();
        this.addWidget(button);
        this.addWidget(popUp = new DraggableScrollableWidgetGroup(0, height, width, 15));
        popUp.setBackground(popUpTexture);
        popUp.setVisible(false);
        popUp.setActive(false);
        currentValue = "";
        computeLayout();
    }

    protected void computeLayout() {
        int height = Math.min(maxCount, candidates.size()) * 15;
        popUp.clearAllWidgets();
        selectables.clear();
        popUp.setSize(new Size(getSize().width, height));
        popUp.setSelfPosition(showUp ? new Position(0, -height) : new Position(0, getSize().height));
        if (candidates.size() > maxCount) {
            popUp.setYScrollBarWidth(4).setYBarStyle(null, new ColorRectTexture(-1));
        }
        int y = 0;
        int width = candidates.size() > maxCount ? getSize().width -4 : getSize().width;
        for (String candidate : candidates) {
            SelectableWidgetGroup select = new SelectableWidgetGroup(0, y, width, 15);
            select.addWidget(new ImageWidget(0, 0, width, 15, new TextTexture(candidate, fontColor).setWidth(width).setType(TextTexture.TextType.ROLL)));
            select.setSelectedTexture(-1, -1);
            select.setOnSelected(s -> {
                setValue(candidate);
                if (onChanged != null) {
                    onChanged.accept(candidate);
                }
                writeClientAction(2, buffer -> buffer.writeUtf(candidate));
                setShow(false);
            });
            popUp.addWidget(select);
            selectables.add(select);
            y += 15;
        }
        popUp.setScrollYOffset(0);
    }

    @ConfigSetter(field = "maxCount")
    public SelectorWidget setMaxCount(int maxCount) {
        this.maxCount = maxCount;
        computeLayout();
        return this;
    }

    @ConfigSetter(field = "showUp")
    public SelectorWidget setIsUp(boolean isUp) {
        this.showUp = isUp;
        computeLayout();
        return this;
    }

    @ConfigSetter(field = "fontColor")
    public SelectorWidget setFontColor(int fontColor) {
        this.fontColor = fontColor;
        computeLayout();
        return this;
    }

    @ConfigSetter(field = "currentValue")
    public SelectorWidget setValue(String value) {
        if (!value.equals(currentValue)) {
            currentValue = value;
            int index = candidates.indexOf(value);
            textTexture.updateText(value);
            for (int i = 0; i < selectables.size(); i++) {
                selectables.get(i).isSelected = index == i;
            }
        }
        return this;
    }

    @ConfigSetter(field = "candidates")
    public void setCandidates(List<String> candidates) {
        this.candidates = candidates;
        computeLayout();
    }

    public SelectorWidget setButtonBackground(IGuiTexture... guiTexture) {
        super.setBackground(guiTexture);
        return this;
    }

    @ConfigSetter(field = "popUpTexture")
    public SelectorWidget setBackground(IGuiTexture background) {
        popUpTexture = background;
        popUp.setBackground(background);
        return this;
    }

    @Override
    public void setSize(Size size) {
        super.setSize(size);
        button.setSize(size);
        computeLayout();
    }

    @Environment(EnvType.CLIENT)
    public void setShow(boolean isShow) {
        if (isShow) {
            setFocus(true);
        }
        this.isShow = isShow;
        popUp.setVisible(isShow);
        popUp.setActive(isShow);
    }

    public String getValue() {
        return currentValue;
    }

    public SelectorWidget setOnChanged(Consumer<String> onChanged) {
        this.onChanged = onChanged;
        return this;
    }

    public SelectorWidget setSupplier(Supplier<String> supplier) {
        this.supplier = supplier;
        return this;
    }

    @Override
    public boolean isMouseOverElement(double mouseX, double mouseY) {
        return super.isMouseOverElement(mouseX, mouseY) || (isShow && popUp.isMouseOverElement(mouseX, mouseY));
    }

    @Override
    public @Nullable Widget getHoverElement(double mouseX, double mouseY) {
        return isMouseOverElement(mouseX, mouseY) ? this : null;
    }

    @Override
    public void onFocusChanged(@Nullable Widget lastFocus, Widget focus) {
        if (lastFocus != null && !lastFocus.isParent(this) && focus != this) {
            setShow(false);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (isClientSideWidget && supplier != null) {
            setValue(supplier.get());
        }
        if (gui != null) {
            ModularUIGuiContainer container = gui.getModularUIGui();
            if (container != null && container.lastFocus != null && container.lastFocus.isParent(this)) {
                setFocus(true);
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (!isClientSideWidget && supplier != null) {
            var last = currentValue;
            setValue(supplier.get());
            if (!last.equals( currentValue)) {
                writeUpdateInfo(3, buffer -> buffer.writeUtf(currentValue));
            }
        }
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        if (supplier != null) {
            setValue(supplier.get());
        }
        buffer.writeUtf(currentValue);
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        setValue(buffer.readUtf());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void drawInForeground(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        boolean lastVisible = popUp.isVisible();
        popUp.setVisible(false);
        super.drawInForeground(matrixStack, mouseX, mouseY, partialTicks);
        popUp.setVisible(lastVisible);

        if(isShow) {
            matrixStack.translate(0, 0, 200);
            popUp.drawInBackground(matrixStack, mouseX, mouseY, partialTicks);
            popUp.drawInForeground(matrixStack, mouseX, mouseY, partialTicks);
            matrixStack.translate(0, 0, -200);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void drawInBackground(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        boolean lastVisible = popUp.isVisible();
        popUp.setVisible(false);
        super.drawInBackground(matrixStack, mouseX, mouseY, partialTicks);
        popUp.setVisible(lastVisible);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!super.mouseClicked(mouseX, mouseY, button)) {
            setFocus(false);
            return false;
        }
        return true;
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        if (id == 2) {
            setValue(buffer.readUtf());
            if (onChanged != null) {
               onChanged.accept(getValue()); 
            }
        }
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 3) {
            setValue(buffer.readUtf());
        }
    }

    @Override
    public void addWidgetsConfigurator(ConfiguratorGroup father) {

    }

    @Override
    public boolean canWidgetAccepted(IConfigurableWidget widget) {
        return false;
    }
}