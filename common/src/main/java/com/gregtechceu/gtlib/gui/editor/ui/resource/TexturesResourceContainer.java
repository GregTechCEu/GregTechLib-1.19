package com.gregtechceu.gtlib.gui.editor.ui.resource;

import com.gregtechceu.gtlib.gui.editor.Icons;
import com.gregtechceu.gtlib.gui.editor.annotation.RegisterUI;
import com.gregtechceu.gtlib.gui.editor.configurator.ConfiguratorGroup;
import com.gregtechceu.gtlib.gui.editor.configurator.IConfigurable;
import com.gregtechceu.gtlib.gui.editor.configurator.SelectorConfigurator;
import com.gregtechceu.gtlib.gui.editor.data.resource.Resource;
import com.gregtechceu.gtlib.gui.editor.runtime.UIDetector;
import com.gregtechceu.gtlib.gui.editor.ui.ConfigPanel;
import com.gregtechceu.gtlib.gui.editor.ui.ResourcePanel;
import com.gregtechceu.gtlib.gui.texture.IGuiTexture;
import com.gregtechceu.gtlib.gui.texture.UIResourceTexture;
import com.gregtechceu.gtlib.gui.util.TreeBuilder;
import com.gregtechceu.gtlib.gui.widget.ImageWidget;

/**
 * @author KilaBash
 * @date 2022/12/5
 * @implNote TexturesResourceContainer
 */
public class TexturesResourceContainer extends ResourceContainer<IGuiTexture, ImageWidget> {
    public TexturesResourceContainer(Resource<IGuiTexture> resource, ResourcePanel panel) {
        super(resource, panel);
        setWidgetSupplier(k -> new ImageWidget(0, 0, 30, 30, getResource().getResource(k)));
        setDragging(key -> new UIResourceTexture(resource, key), o -> o);
        setOnEdit(key -> openTextureConfigurator(key, getResource().getResource(key)));
        setOnRemove(key -> !key.equals("empty"));
    }

    private void openTextureConfigurator(String key, IGuiTexture current) {
        if (key.equals("empty")) return;
        getPanel().getEditor().getConfigPanel().openConfigurator(ConfigPanel.Tab.RESOURCE, new IConfigurable() {
            @Override
            public void buildConfigurator(ConfiguratorGroup father) {
                UIDetector.Wrapper<RegisterUI, IGuiTexture> defaultWrapper = null;
                for (var wrapper : UIDetector.REGISTER_TEXTURES) {
                    if (wrapper.clazz() == current.getClass()) {
                        defaultWrapper = wrapper;
                    }
                }

                UIDetector.Wrapper<RegisterUI, IGuiTexture> finalDefaultWrapper = defaultWrapper;
                SelectorConfigurator<UIDetector.Wrapper<RegisterUI, IGuiTexture>> selectorConfigurator = new SelectorConfigurator<>(
                        "gtlib.gui.editor.name.texture_type",
                        () -> finalDefaultWrapper,
                        wrapper -> {
                            if (wrapper != finalDefaultWrapper) {
                                var newTexture = wrapper.creator().get();
                                getResource().addResource(key, newTexture);
                                getWidgets().get(key).setImage(newTexture);
                                openTextureConfigurator(key, newTexture);
                            }
                        },
                        finalDefaultWrapper,
                        false,
                        UIDetector.REGISTER_TEXTURES,
                        w -> "gtlib.gui.editor.register.texture." + w.annotation().name()
                );
                selectorConfigurator.setTips("gtlib.gui.editor.tips.texture_type");
                father.addConfigurators(selectorConfigurator);
                current.buildConfigurator(father);
            }

        });
    }

    @Override
    protected TreeBuilder.Menu getMenu() {
        return TreeBuilder.Menu.start()
                .leaf(Icons.EDIT_FILE, "gtlib.gui.editor.menu.edit", this::editResource)
                .leaf("gtlib.gui.editor.menu.rename", this::renameResource)
                .crossLine()
                .leaf(Icons.COPY, "gtlib.gui.editor.menu.copy", this::copy)
                .leaf(Icons.PASTE, "gtlib.gui.editor.menu.paste", this::paste)
                .branch(Icons.ADD_FILE, "gtlib.gui.editor.menu.add_resource", menu -> {
                    for (UIDetector.Wrapper<RegisterUI, IGuiTexture> wrapper : UIDetector.REGISTER_TEXTURES) {
                        IGuiTexture icon = wrapper.creator().get();
                        String name = "gtlib.gui.editor.register.texture." + wrapper.annotation().name();
                        menu.leaf(icon, name, () -> {
                            resource.addResource(genNewFileName(), wrapper.creator().get());
                            reBuild();
                        });
                    }
                })
                .leaf(Icons.REMOVE_FILE, "gtlib.gui.editor.menu.remove", this::removeSelectedResource);
    }
}
