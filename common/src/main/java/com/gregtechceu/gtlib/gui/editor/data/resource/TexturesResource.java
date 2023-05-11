package com.gregtechceu.gtlib.gui.editor.data.resource;

import com.gregtechceu.gtlib.gui.editor.annotation.RegisterUI;
import com.gregtechceu.gtlib.gui.editor.runtime.UIDetector;
import com.gregtechceu.gtlib.gui.editor.ui.ResourcePanel;
import com.gregtechceu.gtlib.gui.editor.ui.resource.ResourceContainer;
import com.gregtechceu.gtlib.gui.editor.ui.resource.TexturesResourceContainer;
import com.gregtechceu.gtlib.gui.texture.IGuiTexture;
import com.gregtechceu.gtlib.gui.texture.ResourceBorderTexture;
import com.gregtechceu.gtlib.gui.texture.ResourceTexture;
import com.gregtechceu.gtlib.gui.widget.ImageWidget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import static com.gregtechceu.gtlib.gui.editor.data.resource.TexturesResource.RESOURCE_NAME;
import static com.gregtechceu.gtlib.gui.widget.TabContainer.TABS_LEFT;

/**
 * @author KilaBash
 * @date 2022/12/3
 * @implNote TextureResource
 */
@RegisterUI(name = RESOURCE_NAME, group = "resource")
public class TexturesResource extends Resource<IGuiTexture> {

    public final static String RESOURCE_NAME = "gtlib.gui.editor.group.textures";

    public TexturesResource() {
        data.put("empty", IGuiTexture.EMPTY);
    }

    @Override
    public void buildDefault() {
        data.put("border background", ResourceBorderTexture.BORDERED_BACKGROUND);
        data.put("button", ResourceBorderTexture.BUTTON_COMMON);
        data.put("slot", new ResourceTexture("gtlib:textures/gui/slot.png"));
        data.put("fluid slot", new ResourceTexture("gtlib:textures/gui/fluid_slot.png"));
        data.put("tab", TABS_LEFT.getSubTexture(0, 0, 0.5f, 1f / 3));
        data.put("tab pressed", TABS_LEFT.getSubTexture(0.5f, 0, 0.5f, 1f / 3));
        for (var wrapper : UIDetector.REGISTER_TEXTURES) {
            data.put("gtlib.gui.editor.register.texture." + wrapper.annotation().name(), wrapper.creator().get());
        }
    }

    @Override
    public String name() {
        return RESOURCE_NAME;
    }

    @Override
    public ResourceContainer<IGuiTexture, ImageWidget> createContainer(ResourcePanel panel) {
        return new TexturesResourceContainer(this, panel);
    }

    @Override
    public Tag serialize(IGuiTexture value) {
        return IGuiTexture.serializeWrapper(value);
    }

    @Override
    public IGuiTexture deserialize(Tag nbt) {
        if (nbt instanceof CompoundTag tag) {
            return IGuiTexture.deserializeWrapper(tag);
        }
        return null;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        data.clear();
        data.put("empty", IGuiTexture.EMPTY);
        for (String key : nbt.getAllKeys()) {
            data.put(key, deserialize(nbt.get(key)));
        }
        for (IGuiTexture texture : data.values()) {
            texture.setUIResource(this);
        }
    }

}
