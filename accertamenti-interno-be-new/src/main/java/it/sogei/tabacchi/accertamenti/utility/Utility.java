package it.sogei.tabacchi.accertamenti.utility;

import jakarta.ws.rs.container.ResourceInfo;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
public class Utility {

    public static boolean isAnnotationPresent(ResourceInfo resourceInfo, Class<? extends Annotation> annotationClass) {
        try {
            Class<?> resourceClass = resourceInfo.getResourceClass();
            Method resourceMethod = resourceInfo.getResourceMethod();

            return resourceMethod.isAnnotationPresent(annotationClass) || resourceClass.isAnnotationPresent(annotationClass);
        } catch (Exception e) {
            return false;
        }
    }

    public static <T> T getOrDefault(T obj, T defaultValue) {
        return Objects.isNull(obj) ? defaultValue : obj;
    }

    public static String bytesToString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

}
