package org.owpk.jsondataextruder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.Stack;

public class DefinitionConfigBuilder {
    private final Stack<DefinitionConfig> stack;
    private DefinitionConfig current;

    @Getter
    @Setter
    @RequiredArgsConstructor
    private static class Node {
        private final Stack<DefinitionConfigBuilder> configs;
        private Node prev;
        private Node next;
    }

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

//    TODO I guess it has to wait for better times.

//    public DefinitionConfigBuilder next() {
//        return this;
//    }
//
//    public DefinitionConfigBuilder lookUp() {
//        return this;
//    }
//
//    public DefinitionConfigBuilder lookDown() {
//        return this;
//    }
//
//    public DefinitionConfigBuilder getForObject(Class<?> clazz) {
//        return this;
//    }

    public DefinitionConfig build() {
        return (DefinitionConfig) stack.firstElement().clone();
    }
}
