package com.gregtechceu.gtlib.utils.forge;

import com.gregtechceu.gtlib.GTLib;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.lang.annotation.Annotation;
import java.util.function.Consumer;

public class ReflectionUtilsImpl {


    public static <A extends Annotation> void findAnnotationClasses(Class<A> annotationClass, Consumer<Class<?>> consumer) {
        org.objectweb.asm.Type annotationType = org.objectweb.asm.Type.getType(annotationClass);
        for (ModFileScanData data : ModList.get().getAllScanData()) {
            for (ModFileScanData.AnnotationData annotation : data.getAnnotations()) {
                if (annotationType.equals(annotation.annotationType())) {
                    try {
                        consumer.accept(Class.forName(annotation.memberName(), false, ReflectionUtilsImpl.class.getClassLoader()));
                    } catch (Throwable throwable) {
                        GTLib.LOGGER.error("Failed to load class for notation: " + annotation.memberName(), throwable);
                    }
                }
            }
        }
    }
}
