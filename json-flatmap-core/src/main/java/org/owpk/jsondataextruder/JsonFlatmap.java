package org.owpk.jsondataextruder;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JsonFlatmap {

    public static <T> Multimap<String, String> flatmap(List<T> jsonObjects, DefinitionConfig config) {
        Multimap<String, String> data = LinkedListMultimap.create();
        flatmap(jsonObjects, data, config);
        return data;
    }

    @SuppressWarnings("unchecked")
    public static <T> Multimap<String, String> flatmap(T jsonObject, DefinitionConfig config) {
        Multimap<String, String> data = LinkedListMultimap.create();
        if (Object[].class.isAssignableFrom(jsonObject.getClass())) {
            List<T> list = new ArrayList<>();
            T[] arr = (T[]) jsonObject;
            Collections.addAll(list, arr);
            flatmap(list, data, config);
        } else
            flatmap(jsonObject, data, config);
        return data;
    }

    private static <T> void flatmap(List<T> jsonObjects, Multimap<String, String> map, DefinitionConfig config) {
        jsonObjects.forEach(x -> flatmap(x, map, config));
    }

    private static <T> void flatmap(T jsonObjects, Multimap<String, String> map, DefinitionConfig config) {
        try {
            JsonObjectWrapper wrapper = new JsonObjectWrapperImpl<>(jsonObjects, map);
            wrapper.executeNext(config);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
