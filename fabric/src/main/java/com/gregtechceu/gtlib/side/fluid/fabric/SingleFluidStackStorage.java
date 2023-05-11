/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gregtechceu.gtlib.side.fluid.fabric;

import com.gregtechceu.gtlib.side.fluid.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

/**
 * An item variant storage backed by an {@link ItemStack}.
 * Implementors should at least override {@link #getStack} and {@link #setStack},
 * and probably {@link #onFinalCommit} as well for {@code markDirty()} and similar calls.
 *
 * <p>{@link #canInsert} and {@link #canExtract} can be used for more precise control over which items may be inserted or extracted.
 * If one of these two functions is overridden to always return false, implementors may also wish to override
 * {@link #supportsInsertion} and/or {@link #supportsExtraction}.
 * {@link #getCapacity(FluidVariant)} can be overridden to change the maximum capacity depending on the item variant.
 *
 * <p><b>Experimental feature</b>, we reserve the right to remove or change it without further notice.
 * The transfer API is a complex addition, and we want to be able to correct possible design mistakes.
 */
@ApiStatus.Experimental
public abstract class SingleFluidStackStorage extends SnapshotParticipant<FluidStack> implements SingleSlotStorage<FluidVariant> {
	/**
	 * Return the stack of this storage. It will be modified directly sometimes to avoid needless copies.
	 * However, any mutation of the stack will directly be followed by a call to {@link #setStack}.
	 * This means that either returning the backing stack directly or a copy is safe.
	 *
	 * @return The current stack.
	 */
	protected abstract FluidStack getStack();

	/**
	 * Set the stack of this storage.
	 */
	protected abstract void setStack(FluidStack stack);

	/**
	 * Return {@code true} if the passed non-blank item variant can be inserted, {@code false} otherwise.
	 */
	protected boolean canInsert(FluidVariant fluidVariant) {
		return true;
	}

	/**
	 * Return {@code true} if the passed non-blank item variant can be extracted, {@code false} otherwise.
	 */
	protected boolean canExtract(FluidVariant fluidVariant) {
		return true;
	}

	/**
	 * Return the maximum capacity of this storage for the passed item variant.
	 * If the passed item variant is blank, an estimate should be returned.
	 *
	 * <p>If the capacity should be limited by the max count of the item, this function must take it into account.
	 * For example, a storage with a maximum count of 4, or less for items that have a smaller max count,
	 * should override this to return {@code Math.min(fluidVariant.getItem().getMaxCount(), 4);}.
	 *
	 * @return The maximum capacity of this storage for the passed item variant.
	 */
	protected abstract long getCapacity(FluidVariant fluidVariant);

	@Override
	public boolean isResourceBlank() {
		return getStack().isEmpty();
	}

	@Override
	public FluidVariant getResource() {
		return FluidHelperImpl.toFluidVariant(getStack());
	}

	@Override
	public long getAmount() {
		return getStack().getAmount();
	}

	@Override
	public long getCapacity() {
		return getCapacity(getResource());
	}

	@Override
	public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount);

		var currentStack = getStack();

		if (((insertedVariant.isOf(currentStack.getFluid()) && insertedVariant.nbtMatches(currentStack.getTag())) || currentStack.isEmpty()) && canInsert(insertedVariant)) {
			int insertedAmount = (int) Math.min(maxAmount, getCapacity(insertedVariant) - currentStack.getAmount());

			if (insertedAmount > 0) {
				updateSnapshots(transaction);
				currentStack = getStack();

				if (currentStack.isEmpty()) {
					currentStack = FluidStack.create(insertedVariant.getFluid(), insertedAmount, insertedVariant.getNbt());
				} else {
					currentStack.grow(insertedAmount);
				}

				setStack(currentStack);

				return insertedAmount;
			}
		}

		return 0;
	}

	@Override
	public long extract(FluidVariant variant, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(variant, maxAmount);

		var currentStack = getStack();

		if ((variant.isOf(currentStack.getFluid()) && variant.nbtMatches(currentStack.getTag())) && canExtract(variant)) {
			int extracted = (int) Math.min(currentStack.getAmount(), maxAmount);

			if (extracted > 0) {
				this.updateSnapshots(transaction);
				currentStack = getStack();
				currentStack.shrink(extracted);
				setStack(currentStack);

				return extracted;
			}
		}

		return 0;
	}

	@Override
	protected FluidStack createSnapshot() {
		var original = getStack();
		setStack(original.copy());
		return original;
	}

	@Override
	protected void readSnapshot(FluidStack snapshot) {
		setStack(snapshot);
	}
}
