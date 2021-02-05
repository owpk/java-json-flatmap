package org.owpk.jsondataextruder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Multimap;
import lombok.Getter;
import org.owpk.objectname.ObjectName;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class JsonObjectWrapperImpl<T> extends ExecutorChain implements JsonObjectWrapper {
    protected static final ObjectMapper jsonMapper = new ObjectMapper();
    private final ObjectNameDataExtruder<T> dataExtruder;
    private final T object;
    private Multimap<String, String> dataCollector;
    private Map<Object, Object> objectGraph;
    private boolean disableFilter;

    @SuppressWarnings("unchecked")
    public JsonObjectWrapperImpl(T object, Multimap<String, String> dataCollector) {
        this(object);
        this.dataCollector = dataCollector;
        this.objectGraph = jsonMapper.convertValue(this.object, LinkedHashMap.class);
    }

    public JsonObjectWrapperImpl(T object) {
        this.object = object;
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
                .forEach((k,v) -> {
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
        for (DefinitionConfig cfg : definitionConfig.getEntitiesToShow()) {
            if (cfg.getObjectName().equals(fieldName)) {
                Object o = field.get(object);
                if (Collection.class.isAssignableFrom(o.getClass())) {
                    List<Object> valueList = (List) o;
                    List<JsonObjectWrapper> list = ReflectWrapperUtils.convertToWrappers(valueList, dataCollector);
                    for (JsonObjectWrapper wrapper : list) {
                        executeWrapper(wrapper, cfg);
                    }
                } else {
                    JsonObjectWrapper wrapper = ReflectWrapperUtils.convertToWrapper(o, dataCollector);
                    executeWrapper(wrapper, cfg);
                }
            }
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
