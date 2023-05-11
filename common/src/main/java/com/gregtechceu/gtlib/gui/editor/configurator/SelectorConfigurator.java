package com.gregtechceu.gtlib.gui.editor.configurator;

import com.gregtechceu.gtlib.gui.editor.ColorPattern;
import com.gregtechceu.gtlib.gui.widget.SelectorWidget;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2022/12/3
 * @implNote BooleanConfigurator
 */
public class SelectorConfigurator<T> extends ValueConfigurator<T>{

    protected List<T> candidates;
    protected Function<T, String> mapping;
    protected Map<String, T> nameMap;
    protected SelectorWidget selector;

    @Setter
    protected int max;

    public SelectorConfigurator(String name, Supplier<T> supplier, Consumer<T> onUpdate, @Nonnull T defaultValue, boolean forceUpdate, List<T> candidates, Function<T, String> mapping) {
        super(name, supplier, onUpdate, defaultValue, forceUpdate);
        this.candidates = candidates;
        this.mapping = mapping;
        this.nameMap = new HashMap<>();
        for (T candidate : candidates) {
            nameMap.put(mapping.apply(candidate), candidate);
        }
    }

    @Override
    protected void onValueUpdate(T newValue) {
        if (newValue == null) newValue = defaultValue;
        if (newValue.equals(value)) return;
        super.onValueUpdate(newValue);
        selector.setValue(mapping.apply(newValue));
    }

    @Override
    public void init(int width) {
        super.init(width);
        addWidget(selector = new SelectorWidget(leftWidth, 2, width - leftWidth - 3 - rightWidth, 10, nameMap.keySet().stream().toList(), -1)
                .setOnChanged(s -> {
                    value = nameMap.get(s);
                    updateValue();
                })
                .setButtonBackground(ColorPattern.T_GRAY.rectTexture().setRadius(5))
                .setBackground(ColorPattern.BLACK.rectTexture())
                .setValue(mapping.apply(value))
        );
    }

}
