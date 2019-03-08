package fr.zaki;

import fr.zaki.schema.Labels;
import fr.zaki.schema.RelationshipTypes;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.PathEvaluator;

import java.util.Collections;
import java.util.Map;

public class DecisionTreeEvaluator implements PathEvaluator {
    private Map<String, String> facts;
    public void setParameters(Map<String, String> facts) {
        this.facts = facts;
    }

    @Override
    public Evaluation evaluate(Path path, BranchState branchState) {
        // If we get to an Answer or Transit, stop traversing, we found a valid path.
        if (path.endNode().hasLabel(Labels.Answer) || path.endNode().hasLabel(Labels.Transit)) {
            //return Evaluation.INCLUDE_AND_PRUNE;
            try {
                if (shouldEnd(path.endNode())) {
                    return Evaluation.INCLUDE_AND_PRUNE;
                }
            } catch (Exception e) {
                // Could not continue this way!
                return Evaluation.EXCLUDE_AND_CONTINUE;
            }
            return Evaluation.EXCLUDE_AND_CONTINUE;
        } else {
            // If not, continue down this path if there is anything else to find.
            return Evaluation.EXCLUDE_AND_CONTINUE;
        }
    }

    private boolean shouldEnd(Node node) throws Exception {
        int argumentCount = 0;
        boolean hasNextParameter = true;
        Map<String, Object> nodeProperties = node.getAllProperties();
        System.out.println("shouldEnd::nodeProperties:" + nodeProperties + " facts:" + facts);
        String[] parameterNames = Magic.explode((String) nodeProperties.get("parameter_names"));
        Class<?>[] parameterTypes = Magic.stringToTypes((String) nodeProperties.get("parameter_types"));
        // Fill the arguments array with their corresponding values
        Object[] arguments = new Object[parameterNames.length];
        for (int j = 0; j < parameterNames.length; ++j) {
            String value = facts.get(parameterNames[j]);
            if (value != null) {
                arguments[j] = Magic.createObject(parameterTypes[j], value);
                argumentCount++;
            }
        } 

        if (node.hasRelationship(Direction.OUTGOING, RelationshipTypes.HAS)) {
            if (argumentCount > 0) {
                hasNextParameter = false;
            }
        }

        return hasNextParameter;
    }

    @Override
    public Evaluation evaluate(Path path) {
        return null;
    }
}
