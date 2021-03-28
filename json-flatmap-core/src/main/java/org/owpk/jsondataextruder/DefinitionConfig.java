package org.owpk.jsondataextruder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.owpk.objectname.ObjectName;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Data
@ToString
@EqualsAndHashCode
public class DefinitionConfig {
    public static final DefinitionConfig DEFAULT = new DefinitionConfig();

    private String name;

    private List<String> fields;

    private List<DefinitionConfig> objects;

    private Map<String, List<String>> filter;

    private DefinitionConfig() {
        this.objects = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.filter = new HashMap<>();
    }

    public DefinitionConfig(Class<?> clazz) {
        this();
        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof ObjectName) {
                name = ((ObjectName) annotation).name();
            }
        }
    }

    public DefinitionConfig(String fieldNameForClass) {
        this();
        name = fieldNameForClass;
    }

    @SafeVarargs
    private  <T> void iterateAllToAdd(Consumer<T> consumer, T first, T... more) {
        consumer.accept(first);
        for (T f : more) {
            consumer.accept(f);
        }
    }

    public void addFieldsToShow(String field, String... more) {
        iterateAllToAdd(x -> fields.add(x), field, more);
    }

    public void addEntitiesToShow(DefinitionConfig config, DefinitionConfig... more) {
        iterateAllToAdd(x -> objects.add(x), config, more);
    }

    public final void addFilterByFields(String fieldName, String... values) {
        filter.put(fieldName, List.of(values));
    }

    @Override
    protected Object clone() {
        DefinitionConfig cfg = new DefinitionConfig();
        cfg.setFilter(filter);
        cfg.setFields(fields);
        cfg.setObjects(objects);
        cfg.setName(name);
        return cfg;
    }
}
