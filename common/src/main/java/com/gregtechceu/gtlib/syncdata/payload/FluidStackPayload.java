package com.gregtechceu.gtlib.syncdata.payload;

import com.gregtechceu.gtlib.side.fluid.FluidStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

public class FluidStackPayload extends ObjectTypedPayload<FluidStack> {

    @Override
    public void writePayload(FriendlyByteBuf buf) {
        payload.writeToBuf(buf);
    }

    @Override
    public void readPayload(FriendlyByteBuf buf) {
        payload = FluidStack.readFromBuf(buf);
    }

    @Override
    public Tag serializeNBT() {
        return payload.saveToTag(new CompoundTag());
    }

    @Override
    public void deserializeNBT(Tag tag) {
        payload = FluidStack.loadFromTag((CompoundTag) tag);
    }
}

