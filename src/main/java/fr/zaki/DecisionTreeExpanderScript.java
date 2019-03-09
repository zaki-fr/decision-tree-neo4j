package fr.zaki;

import java.util.Collections;
import java.util.Map;

import org.codehaus.janino.ScriptEvaluator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.traversal.BranchState;

import fr.zaki.schema.Labels;
import fr.zaki.schema.RelationshipTypes;

public class DecisionTreeExpanderScript extends DecisionTreeBase implements PathExpander<Object> {
    ScriptEvaluator se = new ScriptEvaluator();

    public DecisionTreeExpanderScript() {
        se.setReturnType(String.class);
    }

    @Override
    public Iterable<Relationship> expand(Path path, BranchState<Object> branchState) {
        // If we get to an Node or Transit, stop traversing, we found a valid path.
        if (path.endNode().hasLabel(Labels.Node)) {
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
        String[] parameterNames = Magic.explode((String) ruleProperties.get("parameters"));
        Class<?>[] parameterTypes = Magic.stringToTypes((String) ruleProperties.get("types"));

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

    @Override
    public PathExpander<Object> reverse() {
        return null;
    }
}
