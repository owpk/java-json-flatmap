package org.owpk.jsondataextruder;

import java.util.Stack;

public class DefinitionConfigBuilder {
    private final Stack<DefinitionConfig> stack;
    private DefinitionConfig current;

    public DefinitionConfigBuilder(Class<?> clazz) {
        current = new DefinitionConfig(clazz);
        stack = new Stack<>();
        stack.add(current);
    }

    public DefinitionConfigBuilder addFieldsToShow(String fieldName, String... more) {
        current.addFieldsToShow(fieldName, more);
        return this;
    }

    public DefinitionConfigBuilder addFilterBy(String fieldName, String... fieldValues) {
        current.addFilterByFields(fieldName, fieldValues);
        return this;
    }

    public DefinitionConfigBuilder addNewDefinitionConfig(Class<?> object) {
        DefinitionConfig definitionConfig = new DefinitionConfig(object);
        current.addEntitiesToShow(definitionConfig);
        stack.add(definitionConfig);
        current = definitionConfig;
        return this;
    }

    public DefinitionConfigBuilder back() {
        stack.pop();
        current = stack.peek();
        return this;
    }

    public DefinitionConfig build() {
        return (DefinitionConfig) stack.firstElement().clone();
    }
}
