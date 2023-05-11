package com.gregtechceu.gtlib.core.mixins;

import com.gregtechceu.gtlib.networking.s2c.SPacketManagedPayload;
import com.gregtechceu.gtlib.syncdata.blockentity.IAsyncAutoSyncBlockEntity;
import com.gregtechceu.gtlib.syncdata.blockentity.IAutoPersistBlockEntity;
import com.gregtechceu.gtlib.syncdata.blockentity.IAutoSyncBlockEntity;
import com.gregtechceu.gtlib.syncdata.blockentity.IManagedBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author KilaBash
 * @date 2022/11/27
 * @implNote BlockEntityMixin
 */
@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {

    @Inject(method = "getUpdateTag", at = @At(value = "RETURN"))
    private void injectGetUpdateTag(CallbackInfoReturnable<CompoundTag> cir) {
        if (this instanceof IAutoSyncBlockEntity autoSyncBlockEntity) {
            var tag = cir.getReturnValue();
            tag.put(autoSyncBlockEntity.getSyncTag(), SPacketManagedPayload.of(autoSyncBlockEntity, true).serializeNBT());
        }
    }

    @Inject(method = "saveAdditional", at = @At(value = "RETURN"))
    private void injectSaveAdditional(CompoundTag pTag, CallbackInfo ci) {
        if (this instanceof IAutoPersistBlockEntity autoPersistBlockEntity) {
            autoPersistBlockEntity.saveManagedPersistentData(pTag, false);
        }
    }

    @Inject(method = "load", at = @At(value = "RETURN"))
    private void injectLoad(CompoundTag pTag, CallbackInfo ci) {
        if (this instanceof IAutoSyncBlockEntity autoSyncBlockEntity && pTag.get(autoSyncBlockEntity.getSyncTag()) instanceof CompoundTag tag) {
            new SPacketManagedPayload(tag).processPacket(autoSyncBlockEntity);
        } else if (this instanceof IAutoPersistBlockEntity autoPersistBlockEntity) {
            autoPersistBlockEntity.loadManagedPersistentData(pTag);
        }
    }

    @Inject(method = "setRemoved", at = @At(value = "RETURN"))
    private void injectSetRemoved(CallbackInfo ci) {
        if (this instanceof IAsyncAutoSyncBlockEntity autoSyncBlockEntity) {
            autoSyncBlockEntity.onInValid();
        }
    }

    @Inject(method = "clearRemoved", at = @At(value = "RETURN"))
    private void injectClearRemoved(CallbackInfo ci) {
        if (this instanceof IManagedBlockEntity managed) {
            managed.getRootStorage().init();
            if (managed instanceof IAsyncAutoSyncBlockEntity autoSyncBlockEntity) {
                autoSyncBlockEntity.onValid();
            }
        }
    }

}
