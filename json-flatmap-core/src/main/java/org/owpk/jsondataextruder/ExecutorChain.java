package org.owpk.jsondataextruder;

import lombok.Setter;

@Setter
public abstract class ExecutorChain {
    private ExecutorChain next;

    public void execute(DefinitionConfig config) {
        if (next != null) {
            next.execute(config);
        }
    }

}
