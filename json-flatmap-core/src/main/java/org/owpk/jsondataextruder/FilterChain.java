package org.owpk.jsondataextruder;

import java.util.List;
import java.util.Map;

public class FilterChain extends ExecutorChain {
    private final Map<Object, Object> objGraph;

    public FilterChain(Map<Object, Object> objGraph) {
        this.objGraph = objGraph;
    }

    @Override
    public void execute(DefinitionConfig config) {
        var filter = config.getFilterBy();
        for (Map.Entry<String, List<String>> entry : filter.entrySet()) {
            if (objGraph.containsKey(entry.getKey())) {
                String field = objGraph.get(entry.getKey()).toString();
                if (!entry.getValue().contains(field)) {
                    return;
                }
            }
        }
        super.execute(config);
    }
}
