package org.owpk.jsondataextruder;

public class EmptyJsonObjectWrapperImpl extends JsonObjectWrapperImpl<Object> {

    public EmptyJsonObjectWrapperImpl() {
        super(new Object(), null);
    }
}
