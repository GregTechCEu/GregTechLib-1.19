package com.gregtechceu.gtlib.gui.editor.data.resource;

import com.gregtechceu.gtlib.gui.editor.annotation.RegisterUI;
import com.gregtechceu.gtlib.gui.editor.ui.ResourcePanel;
import com.gregtechceu.gtlib.gui.editor.ui.resource.EntriesResourceContainer;
import com.gregtechceu.gtlib.gui.editor.ui.resource.ResourceContainer;
import com.gregtechceu.gtlib.utils.LocalizationUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import javax.annotation.Nullable;

import static com.gregtechceu.gtlib.gui.editor.data.resource.EntriesResource.RESOURCE_NAME;


/**
 * @author KilaBash
 * @date 2022/12/3
 * @implNote TextureResource
 */
@RegisterUI(name = RESOURCE_NAME, group = "resource")
public class EntriesResource extends Resource<String> {

    public final static String RESOURCE_NAME = "gtlib.gui.editor.group.entries";

    @Override
    public String name() {
        return RESOURCE_NAME;
    }

    @Override
    public void buildDefault() {
        data.put("gtlib.author", "Hello KilaBash!");
    }

    @Override
    public void onLoad() {
        LocalizationUtils.setResource(this);
    }

    @Override
    public void unLoad() {
        LocalizationUtils.clearResource();
    }

    @Override
    public ResourceContainer<String, ?> createContainer(ResourcePanel panel) {
        return new EntriesResourceContainer(this, panel);
    }

    @Nullable
    @Override
    public Tag serialize(String value) {
        return StringTag.valueOf(value);
    }

    @Override
    public String deserialize(Tag nbt) {
        return nbt instanceof StringTag stringTag ? stringTag.getAsString() : "missing value";
    }
}
