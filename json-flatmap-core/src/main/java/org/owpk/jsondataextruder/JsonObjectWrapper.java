package org.owpk.jsondataextruder;

public interface JsonObjectWrapper {
    void executeNext(DefinitionConfig definitionConfig) throws IllegalAccessException;
    void disableFilter(boolean filter);
}
