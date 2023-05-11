package com.gregtechceu.gtlib.gui.factory;

import com.gregtechceu.gtlib.GTLib;
import com.gregtechceu.gtlib.gui.editor.ui.Editor;
import com.gregtechceu.gtlib.gui.modular.IUIHolder;
import com.gregtechceu.gtlib.gui.modular.ModularUI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class UIEditorFactory extends UIFactory<UIEditorFactory> implements IUIHolder {

	public static final UIEditorFactory INSTANCE = new UIEditorFactory();

	private UIEditorFactory(){

	}

	@Override
	protected ModularUI createUITemplate(UIEditorFactory holder, Player entityPlayer) {
		return createUI(entityPlayer);
	}

	@Override
	protected UIEditorFactory readHolderFromSyncData(FriendlyByteBuf syncData) {
		return this;
	}

	@Override
	protected void writeHolderToSyncData(FriendlyByteBuf syncData, UIEditorFactory holder) {

	}

	@Override
	public ModularUI createUI(Player entityPlayer) {
		return new ModularUI(this, entityPlayer)
				.widget(new Editor(GTLib.location));
	}

	@Override
	public boolean isInvalid() {
		return false;
	}

	@Override
	public boolean isRemote() {
		return GTLib.isRemote();
	}

	@Override
	public void markAsDirty() {

	}
}
