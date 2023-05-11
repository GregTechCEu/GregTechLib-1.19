package com.gregtechceu.gtlib.networking.both;

import com.gregtechceu.gtlib.networking.IPacket;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * a packet that contains a BlockPos
 */
@NoArgsConstructor
public abstract class PacketIntLocation implements IPacket {
    protected BlockPos pos;

    public PacketIntLocation(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
    }
}
