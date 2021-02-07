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

    private List<String> fieldsToShow;

    private List<DefinitionConfig> entitiesToShow;

    private String objectName;

    private Map<String, List<String>> filterBy;

    private DefinitionConfig() {
        this.entitiesToShow = new ArrayList<>();
        this.fieldsToShow = new ArrayList<>();
        this.filterBy = new HashMap<>();
    }

    public DefinitionConfig(Class<?> clazz) {
        this();
        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof ObjectName) {
                objectName = ((ObjectName) annotation).name();
            }
        }
    }

    @SafeVarargs
    private  <T> void iterateAllToAdd(Consumer<T> consumer, T first, T... more) {
        consumer.accept(first);
        for (T f : more) {
            consumer.accept(f);
        }
    }

    public void addFieldsToShow(String field, String... more) {
        iterateAllToAdd(x -> fieldsToShow.add(x), field, more);
    }

    public void addEntitiesToShow(DefinitionConfig config, DefinitionConfig... more) {
        iterateAllToAdd(x -> entitiesToShow.add(x), config, more);
    }

    public final void addFilterByFields(String fieldName, String... values) {
        filterBy.put(fieldName, List.of(values));
    }

    @Override
    protected Object clone() {
        DefinitionConfig cfg = new DefinitionConfig();
        cfg.setFilterBy(filterBy);
        cfg.setFieldsToShow(fieldsToShow);
        cfg.setEntitiesToShow(entitiesToShow);
        cfg.setObjectName(objectName);
        return cfg;
    }
}
