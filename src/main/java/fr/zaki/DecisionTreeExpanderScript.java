package fr.zaki;

import fr.zaki.schema.Labels;
import fr.zaki.schema.RelationshipTypes;
import org.codehaus.janino.ScriptEvaluator;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.logging.Log;

import java.util.Collections;
import java.util.Map;

public class DecisionTreeExpanderScript implements PathExpander {
    private Map<String, String> facts;
    private Log log;
    ScriptEvaluator se = new ScriptEvaluator();

    public DecisionTreeExpanderScript(Map<String, String> facts, Log log) {
        this.facts = facts;
        this.log = log;
        se.setReturnType(String.class);
    }

    @Override
    public Iterable<Relationship> expand(Path path, BranchState branchState) {
        // If we get to an Answer or Transit, stop traversing, we found a valid path.
        if (path.endNode().hasLabel(Labels.Answer) || path.endNode().hasLabel(Labels.Transit)) {
            try {
                if (shouldEnd(path.endNode())) {
                    return Collections.emptyList();
                } 
            } catch (Exception e) {
                log.debug("Decision Tree Traversal failed", e);
                // Could not continue this way!
                return Collections.emptyList();
            }
        }

        // If we have Rules to evaluate, go do that.
        if (path.endNode().hasRelationship(Direction.OUTGOING, RelationshipTypes.HAS)) {
            return path.endNode().getRelationships(Direction.OUTGOING, RelationshipTypes.HAS);
        }

        if (path.endNode().hasLabel(Labels.Rule)) {
            try {
                return path.endNode().getRelationships(Direction.OUTGOING, choosePath(path.endNode()));
            } catch (Exception e) {
                log.debug("Decision Tree Traversal failed", e);
                // Could not continue this way!
                return Collections.emptyList();
            }
        }

        // Otherwise, not sure what to do really.
        return Collections.emptyList();
    }

    private RelationshipType choosePath(Node rule) throws Exception {
        // Get the properties of the rule stored in the node
        Map<String, Object> ruleProperties = rule.getAllProperties();
        String[] parameterNames = Magic.explode((String) ruleProperties.get("parameter_names"));
        Class<?>[] parameterTypes = Magic.stringToTypes((String) ruleProperties.get("parameter_types"));

        // Fill the arguments array with their corresponding values
        Object[] arguments = new Object[parameterNames.length];
        for (int j = 0; j < parameterNames.length; ++j) {
            arguments[j] = Magic.createObject(parameterTypes[j], facts.get(parameterNames[j]));
        }

        // Set our parameters with their matching types
        se.setParameters(parameterNames, parameterTypes);

        // And now we "cook" (scan, parse, compile and load) the script.
        se.cook((String)ruleProperties.get("script"));

        return RelationshipType.withName((String) se.evaluate(arguments));
    }

    private boolean shouldEnd(Node node) throws Exception {
        int argumentCount = 0;
        boolean hasNextParameter = true;
        Map<String, Object> nodeProperties = node.getAllProperties();
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

        return false;
    }

    @Override
    public PathExpander reverse() {
        return null;
    }
}
