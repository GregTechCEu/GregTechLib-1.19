package com.gregtechceu.gtlib.gui.editor.runtime;

import com.gregtechceu.gtlib.GTLib;
import com.gregtechceu.gtlib.gui.editor.accessors.IConfiguratorAccessor;
import com.gregtechceu.gtlib.gui.editor.annotation.ConfigAccessor;
import com.gregtechceu.gtlib.gui.editor.annotation.RegisterUI;
import com.gregtechceu.gtlib.gui.editor.configurator.IConfigurableWidget;
import com.gregtechceu.gtlib.gui.editor.data.Project;
import com.gregtechceu.gtlib.gui.editor.data.resource.Resource;
import com.gregtechceu.gtlib.gui.editor.ui.menu.MenuTab;
import com.gregtechceu.gtlib.gui.editor.ui.view.FloatViewWidget;
import com.gregtechceu.gtlib.gui.texture.IGuiTexture;
import com.gregtechceu.gtlib.utils.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2022/12/3
 * @implNote AnnotationDetector
 */
@SuppressWarnings("unchecked")
public class UIDetector {

    public record Wrapper<A extends Annotation, T>(A annotation, Class<? extends T> clazz, Supplier<T> creator) { }

    public static final List<IConfiguratorAccessor<?>> CONFIGURATOR_ACCESSORS = scanClasses(ConfigAccessor.class, IConfiguratorAccessor.class, UIDetector::checkNoArgsConstructor, UIDetector::createNoArgsInstance, (a, b) -> 0);
    public static final List<Wrapper<RegisterUI, IGuiTexture>> REGISTER_TEXTURES = scanClasses(RegisterUI.class, IGuiTexture.class, UIDetector::checkNoArgsConstructor, UIDetector::toUINoArgsBuilder, UIDetector::UIWrapperSorter);
    public static final List<Wrapper<RegisterUI, Resource>> REGISTER_RESOURCES = scanClasses(RegisterUI.class, Resource.class, UIDetector::checkNoArgsConstructor, UIDetector::toUINoArgsBuilder, UIDetector::UIWrapperSorter);
    public static final List<Wrapper<RegisterUI, IConfigurableWidget>> REGISTER_WIDGETS = scanClasses(RegisterUI.class, IConfigurableWidget.class, UIDetector::checkNoArgsConstructor, UIDetector::toUINoArgsBuilder, UIDetector::UIWrapperSorter);
    public static final List<Wrapper<RegisterUI, FloatViewWidget>> REGISTER_FLOAT_VIEWS = scanClasses(RegisterUI.class, FloatViewWidget.class, UIDetector::checkNoArgsConstructor, UIDetector::toUINoArgsBuilder, UIDetector::UIWrapperSorter);
    public static final List<Wrapper<RegisterUI, MenuTab>> REGISTER_MENU_TABS = scanClasses(RegisterUI.class, MenuTab.class, UIDetector::checkNoArgsConstructor, UIDetector::toUINoArgsBuilder, UIDetector::UIWrapperSorter);
    public static final List<Project> REGISTER_PROJECTS = scanClasses(RegisterUI.class, Project.class, UIDetector::checkNoArgsConstructor, UIDetector::createNoArgsInstance, (a, b) -> 0);

    public static void init() {

    }

    public static <A extends Annotation, T, C> List<C> scanClasses(Class<A> annotationClass, Class<T> baseClazz, BiPredicate<A, Class<? extends T>> predicate, Function<Class<? extends T>, C> mapping, Comparator<C> sorter) {
        List<C> result = new ArrayList<>();
        ReflectionUtils.findAnnotationClasses(annotationClass, clazz -> {
            if (baseClazz.isAssignableFrom(clazz)) {
                try {
                    Class<? extends T> realClass =  (Class<? extends T>) clazz;
                    if (predicate.test(clazz.getAnnotation(annotationClass), realClass)) {
                        result.add(mapping.apply(realClass));
                    }
                } catch (Throwable e) {
                    GTLib.LOGGER.error("failed to scan annotation {} + base class {} while handling class {} ", annotationClass, baseClazz, clazz, e);
                }
            }
        });
        result.sort(sorter);
        return result;
    }

    private static <A, T> boolean checkNoArgsConstructor(A annotation, Class<? extends T> clazz) {
        if (annotation instanceof RegisterUI registerUI) {
            if (!registerUI.modID().isEmpty() && !GTLib.isModLoaded(registerUI.modID())) {
                return false;
            }
        }
        try {
            clazz.getDeclaredConstructor();
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static <T> T createNoArgsInstance(Class<? extends T> clazz) {
        try {
            var constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> Wrapper<RegisterUI, T> toUINoArgsBuilder(Class<? extends T> clazz) {
        return new Wrapper<>(clazz.getAnnotation(RegisterUI.class), clazz, () -> createNoArgsInstance(clazz));
    }

    private static int UIWrapperSorter(Wrapper<RegisterUI, ?> a, Wrapper<RegisterUI, ?> b) {
        return b.annotation.priority() - a.annotation.priority();
    }

}
