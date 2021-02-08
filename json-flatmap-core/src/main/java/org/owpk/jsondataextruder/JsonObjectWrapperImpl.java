package org.owpk.jsondataextruder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Multimap;
import lombok.Getter;
import org.owpk.objectname.ObjectName;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

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
        private final List<String> unannotatedFiledList;
        private Field[] fields;

        public ObjectNameDataExtruder(T object) {
            fields = object.getClass().getDeclaredFields();
            annotatedFieldMap = new LinkedHashMap<>();
            unannotatedFiledList = new ArrayList<>();
            extrudeFields();
        }

        private void extrudeFields() {
            for (Field field : fields) {
                field.setAccessible(true);
                List<Annotation> present =
                        Arrays.stream(field.getDeclaredAnnotations())
                                .filter(x -> x instanceof ObjectName)
                                .collect(Collectors.toList());

                List<JsonProperty> jackson =
                        Arrays.stream(field.getDeclaredAnnotations())
                                .filter(x -> x instanceof JsonProperty)
                                .map(x -> (JsonProperty) x)
                                .collect(Collectors.toList());

                if (present.isEmpty()) {
                    if (jackson.isEmpty()) {
                        unannotatedFiledList.add(field.getName());
                    } else {
                        unannotatedFiledList.add((jackson.get(0).value()));
                    }
                } else {
                    ObjectName objectNameAnnotation = (ObjectName) present.get(0);
                    annotatedFieldMap.put(field, objectNameAnnotation);
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
            if (o != null)
                convertAndExecute(o);
        } else
            for (DefinitionConfig cfg : definitionConfig.getEntitiesToShow()) {
                if (cfg.getObjectName().equals(fieldName)) {
                    Object o = field.get(object);
                    convertAndExecute(o);
                }
            }
    }

    private void convertAndExecute(Object o) throws IllegalAccessException {
        List<JsonObjectWrapper> wrappers = new ArrayList<>();
        if (Collection.class.isAssignableFrom(o.getClass())) {
            List<Object> valueList = (List) o;
            wrappers.addAll(ReflectWrapperUtils.convertToWrappers(valueList, dataCollector));
        } else wrappers.add(ReflectWrapperUtils.convertToWrapper(o, dataCollector));
        for (JsonObjectWrapper wrapper : wrappers) {
            executeWrapper(wrapper, DefinitionConfig.DEFAULT);
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
        if (!list.isEmpty()) {
            objectGraph.forEach((k, v) -> {
                if (!list.isEmpty() && list.contains(k.toString()))
                    dataCollector.put(k.toString(), v != null ? v.toString() : "null");
            });
        } else {
            List<String> unnAnnotated = dataExtruder.unannotatedFiledList;
            if (!unnAnnotated.isEmpty()) {
                objectGraph.forEach((k, v) -> {
                    if (unnAnnotated.contains(k.toString()))
                        dataCollector.put(k.toString(), v != null ? v.toString() : "null");
                });
            }
        }
    }
}
