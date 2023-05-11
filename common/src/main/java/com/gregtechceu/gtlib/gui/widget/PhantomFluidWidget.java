package com.gregtechceu.gtlib.gui.widget;

import com.google.common.collect.Lists;
import com.gregtechceu.gtlib.GTLib;
import com.gregtechceu.gtlib.gui.editor.annotation.ConfigSetter;
import com.gregtechceu.gtlib.gui.editor.annotation.RegisterUI;
import com.gregtechceu.gtlib.gui.editor.configurator.IConfigurableWidget;
import com.gregtechceu.gtlib.gui.ingredient.IGhostIngredientTarget;
import com.gregtechceu.gtlib.gui.ingredient.Target;
import com.gregtechceu.gtlib.misc.ItemStackTransfer;
import com.gregtechceu.gtlib.side.fluid.FluidTransferHelper;
import com.gregtechceu.gtlib.side.fluid.FluidStack;
import com.gregtechceu.gtlib.side.fluid.IFluidStorage;
import dev.emi.emi.api.stack.FluidEmiStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nullable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@RegisterUI(name = "phantom_fluid_slot", group = "widget.container")
public class PhantomFluidWidget extends TankWidget implements IGhostIngredientTarget, IConfigurableWidget {

    private Consumer<FluidStack> fluidStackUpdater;

    public PhantomFluidWidget() {
        super();
        this.allowClickFilled = false;
        this.allowClickDrained = false;
    }

    public PhantomFluidWidget(IFluidStorage fluidTank, int x, int y) {
        super(fluidTank, x, y, false, false);
    }

    public PhantomFluidWidget(@Nullable IFluidStorage fluidTank, int x, int y, int width, int height) {
        super(fluidTank, x, y, width, height, false, false);
    }

    public PhantomFluidWidget setIFluidStackUpdater(Consumer<FluidStack> fluidStackUpdater) {
        this.fluidStackUpdater = fluidStackUpdater;
        return this;
    }

    @ConfigSetter(field = "allowClickFilled")
    public PhantomFluidWidget setAllowClickFilled(boolean v) {
        // you cant modify it
        return this;
    }

    @ConfigSetter(field = "allowClickDrained")
    public PhantomFluidWidget setAllowClickDrained(boolean v) {
        // you cant modify it
        return this;
    }

    public static FluidStack drainFrom(Object ingredient) {
         if (ingredient instanceof Ingredient ingred) {
            var items = ingred.getItems();
            if (items.length > 0) {
                ingredient = items[0];
            }
        }
        if (ingredient instanceof ItemStack itemStack) {
            var handler = FluidTransferHelper.getFluidTransfer(new ItemStackTransfer(itemStack), 0);
            if (handler != null) {
                return handler.drain(Long.MAX_VALUE, true);
            }
        }
        return null;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public List<Target> getPhantomTargets(Object ingredient) {
        if (GTLib.isReiLoaded() && ingredient instanceof dev.architectury.fluid.FluidStack fluidStack) {
            ingredient = FluidStack.create(fluidStack.getFluid(), fluidTank.getCapacity(), fluidStack.getTag());
        }
        if (GTLib.isEmiLoaded() && ingredient instanceof FluidEmiStack fluidEmiStack) {
            var fluid = fluidEmiStack.getKeyOfType(Fluid.class);
            ingredient = fluid == null ? FluidStack.empty() : FluidStack.create(fluid, fluidTank.getCapacity(), fluidEmiStack.getNbt());
        }
        if (!(ingredient instanceof FluidStack) && drainFrom(ingredient) == null) {
            return Collections.emptyList();
        }

        Rect2i rectangle = toRectangleBox();
        return Lists.newArrayList(new Target() {
            @Nonnull
            @Override
            public Rect2i getArea() {
                return rectangle;
            }

            @Override
            public void accept(@Nonnull Object ingredient) {
                if (fluidTank == null) return;
                FluidStack ingredientStack;
                if (GTLib.isReiLoaded() && ingredient instanceof dev.architectury.fluid.FluidStack fluidStack) {
                    ingredient = FluidStack.create(fluidStack.getFluid(), fluidTank.getCapacity(), fluidStack.getTag());
                }
                if (GTLib.isEmiLoaded() && ingredient instanceof FluidEmiStack fluidEmiStack) {
                    var fluid = fluidEmiStack.getKeyOfType(Fluid.class);
                    ingredient = fluid == null ? FluidStack.empty() : FluidStack.create(fluid, fluidTank.getCapacity(), fluidEmiStack.getNbt());
                }
                if (ingredient instanceof FluidStack)
                    ingredientStack = (FluidStack) ingredient;
                else
                    ingredientStack = drainFrom(ingredient);

                if (ingredientStack != null) {
                    CompoundTag tagCompound = ingredientStack.saveToTag(new CompoundTag());
                    writeClientAction(2, buffer -> buffer.writeNbt(tagCompound));
                }

                if (isClientSideWidget) {
                    fluidTank.drain(fluidTank.getCapacity(), false);
                    if (ingredientStack != null) {
                        fluidTank.fill(ingredientStack.copy(), false);
                    }
                    if (fluidStackUpdater != null) {
                        fluidStackUpdater.accept(ingredientStack);
                    }
                }
            }
        });
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        if (id == 1) {
            handlePhantomClick();
        } else if (id == 2) {
            FluidStack fluidStack;
            fluidStack = FluidStack.loadFromTag(buffer.readNbt());
            if (fluidTank == null) return;
            fluidTank.drain(fluidTank.getCapacity(), false);
            if (fluidStack != null) {
                fluidTank.fill(fluidStack.copy(), false);
            }
            if (fluidStackUpdater != null) {
                fluidStackUpdater.accept(fluidStack);
            }
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            if (isClientSideWidget) {
                handlePhantomClick();
            } else {
                writeClientAction(1, buffer -> { });
            }
            return true;
        }
        return false;
    }

    private void handlePhantomClick() {
        if (fluidTank == null) return;
        ItemStack itemStack = gui.getModularUIContainer().getCarried().copy();
        if (!itemStack.isEmpty()) {
            itemStack.setCount(1);
            var handler = FluidTransferHelper.getFluidTransfer(gui.entityPlayer, gui.getModularUIContainer());
            if (handler != null) {
                FluidStack resultFluid = handler.drain(Integer.MAX_VALUE, true);
                fluidTank.drain(fluidTank.getCapacity(), false);
                fluidTank.fill(resultFluid.copy(), false);
                if (fluidStackUpdater != null) {
                    fluidStackUpdater.accept(resultFluid);
                }
            }
        } else {
            fluidTank.drain(fluidTank.getCapacity(), false);
            if (fluidStackUpdater != null) {
                fluidStackUpdater.accept(null);
            }
        }
    }

}
