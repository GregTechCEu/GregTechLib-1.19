package com.gregtechceu.gtlib.gui.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.network.FriendlyByteBuf;
import org.lwjgl.glfw.GLFW;

public class ClickData {
    public final int button;
    public final boolean isShiftClick;
    public final boolean isCtrlClick;
    public final boolean isRemote;

    private ClickData(int button, boolean isShiftClick, boolean isCtrlClick, boolean isRemote) {
        this.button = button;
        this.isShiftClick = isShiftClick;
        this.isCtrlClick = isCtrlClick;
        this.isRemote = isRemote;
    }

    @Environment(EnvType.CLIENT)
    public ClickData() {
        MouseHandler mouseHelper = Minecraft.getInstance().mouseHandler;
        long id = Minecraft.getInstance().getWindow().getWindow();
        this.button = mouseHelper.isLeftPressed() ? 0 : mouseHelper.isRightPressed() ? 1 : 2;
        this.isShiftClick = InputConstants.isKeyDown(id, GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(id, GLFW.GLFW_KEY_LEFT_SHIFT);
        this.isCtrlClick = InputConstants.isKeyDown(id, GLFW.GLFW_KEY_LEFT_CONTROL) || InputConstants.isKeyDown(id, GLFW.GLFW_KEY_RIGHT_CONTROL);
        this.isRemote = true;
    }

    @Environment(EnvType.CLIENT)
    public void writeToBuf(FriendlyByteBuf buf) {
        buf.writeVarInt(button);
        buf.writeBoolean(isShiftClick);
        buf.writeBoolean(isCtrlClick);
    }

    public static ClickData readFromBuf(FriendlyByteBuf buf) {
        int button = buf.readVarInt();
        boolean shiftClick = buf.readBoolean();
        boolean ctrlClick = buf.readBoolean();
        return new ClickData(button, shiftClick, ctrlClick, false);
    }
}
