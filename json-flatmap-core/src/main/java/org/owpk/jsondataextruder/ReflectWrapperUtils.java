package org.owpk.jsondataextruder;

import com.google.common.collect.Multimap;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ReflectWrapperUtils {

    public static JsonObjectWrapper convertToWrapper(Object value, Multimap<String, String> collector) {
        try {
            Class<?> clazz = value.getClass();
            Method[] methods = clazz.getDeclaredMethods();
            Field[] field = clazz.getDeclaredFields();
            Constructor<?> constructor = clazz.getConstructor();
            Object o = constructor.newInstance();
            for (Field f : field) {
                for (Method method : methods) {
                    String methodName = method.getName();
                    if (methodName.length() > "set".length() && methodName.startsWith("set")) {
                        String sub = methodName.substring("set".length());
                        String low = sub.substring(0, 1).toLowerCase() + sub.substring(1);
                        if (low.equals(f.getName())) {
                            f.setAccessible(true);
                            method.setAccessible(true);
                            method.invoke(o, f.get(value));
                        }
                    }
                }
            }
            return new JsonObjectWrapperImpl<>(o, collector) {};
        } catch (NoSuchMethodException | IllegalAccessException
                | InstantiationException | InvocationTargetException e) {
            // ignoring
        }
        return new EmptyJsonObjectWrapperImpl();
    }

    public static List<JsonObjectWrapper> convertToWrappers(List<Object> list, Multimap<String, String> collector) {
        List<JsonObjectWrapper> wrappersList = new ArrayList<>();
        list.forEach(x -> {
            JsonObjectWrapper wrapper = convertToWrapper(x, collector);
            wrappersList.add(wrapper);
        });
        return wrappersList;
    }

}
