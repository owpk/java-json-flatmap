package org.owpk.jsondataextruder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Multimap;
import lombok.Getter;
import org.owpk.objectname.ObjectName;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

@Getter
public class JsonObjectWrapperImpl<T> extends ExecutorChain implements JsonObjectWrapper {
    protected static final ObjectMapper jsonMapper = new ObjectMapper();
    private final ObjectNameDataExtruder<T> dataExtruder;
    private final T object;
    private final Map<Object, Object> objectGraph;
    private Multimap<String, String> dataCollector;
    private boolean disableFilter;

    public JsonObjectWrapperImpl(T object, Multimap<String, String> dataCollector) {
        this(object);
        this.dataCollector = dataCollector;
    }

    @SuppressWarnings("unchecked")
    public JsonObjectWrapperImpl(T object) {
        this.object = object;
        this.objectGraph = jsonMapper.convertValue(object, LinkedHashMap.class);
        dataExtruder = new ObjectNameDataExtruder<>(object);
    }

    public void disableFilter(boolean filter) {
        disableFilter = filter;
    }

    @Override
    public void executeNext(DefinitionConfig definitionConfig) {
        if (!disableFilter) {
            var filterChain = new FilterChain(objectGraph);
            filterChain.setNext(this);
            filterChain.execute(definitionConfig);
        } else
            execute(definitionConfig);
    }

    @Override
    public void execute(DefinitionConfig config) {
        collectObjectFields(config);
        dataExtruder.getAnnotatedFieldMap()
                .forEach((k, v) -> {
                    try {
                        executeEntities(config, getFieldName(v, k), k);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Getter
    private static class ObjectNameDataExtruder<T> {
        private final Map<Field, ObjectName> annotatedFieldMap;
        private final T object;
        private Field[] fields;

        public ObjectNameDataExtruder(T object) {
            this.object = object;
            annotatedFieldMap = new LinkedHashMap<>();
            extrudeAllData();
        }

        private void extrudeAllData() {
            fields = object.getClass().getDeclaredFields();
            extrudeAnnotatedFields();
        }

        private void extrudeAnnotatedFields() {
            for (Field field : fields) {
                field.setAccessible(true);
                Annotation[] annotations = field.getDeclaredAnnotations();
                for (Annotation annotation : annotations) {
                    if (annotation instanceof ObjectName) {
                        ObjectName objectNameAnnotation = (ObjectName) annotation;
                        annotatedFieldMap.put(field, objectNameAnnotation);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void executeEntities(DefinitionConfig definitionConfig,
                                   String fieldName, Field field)
            throws IllegalAccessException {
        if (definitionConfig.getEntitiesToShow().isEmpty()) {
            Object o = field.get(object);
            Class<?> clazz = o.getClass();
            DefinitionConfig cfg = new DefinitionConfig(clazz);
            convertAndExecute(o, cfg);
        } else
            for (DefinitionConfig cfg : definitionConfig.getEntitiesToShow()) {
                if (cfg.getObjectName().equals(fieldName)) {
                    Object o = field.get(object);
                    convertAndExecute(o, cfg);
                }
            }
    }

    private void convertAndExecute(Object o, DefinitionConfig cfg) throws IllegalAccessException {
        List<JsonObjectWrapper> wrappers = new ArrayList<>();
        if (Collection.class.isAssignableFrom(o.getClass())) {
            List<Object> valueList = (List) o;
            wrappers.addAll(ReflectWrapperUtils.convertToWrappers(valueList, dataCollector));
        } else wrappers.add(ReflectWrapperUtils.convertToWrapper(o, dataCollector));
        for (JsonObjectWrapper wrapper : wrappers) {
            executeWrapper(wrapper, cfg);
        }
    }

    protected String getFieldName(ObjectName annotation, Field field) {
        String value = annotation.name();
        if (value.isBlank()) {
            value = field.getName();
        }
        return value;
    }

    protected void executeWrapper(JsonObjectWrapper wrapper, DefinitionConfig config) throws IllegalAccessException {
        wrapper.disableFilter(disableFilter);
        wrapper.executeNext(config);
    }

    protected void collectObjectFields(DefinitionConfig definitionConfig) {
        List<String> list = definitionConfig.getFieldsToShow();
        objectGraph.forEach((k, v) -> {
            if (!list.isEmpty() && list.contains(k.toString()))
                dataCollector.put(k.toString(), v.toString());
            else if (list.isEmpty()) {
                dataCollector.put(k.toString(), v.toString());
            }
        });
    }
}
