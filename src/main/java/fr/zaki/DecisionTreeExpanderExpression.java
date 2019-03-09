package fr.zaki;

import fr.zaki.schema.Labels;
import fr.zaki.schema.RelationshipTypes;
import org.codehaus.janino.ExpressionEvaluator;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;

import java.util.Collections;
import java.util.Map;

public class DecisionTreeExpanderExpression extends DecisionTreeBase implements PathExpander<Object> {
    private ExpressionEvaluator ee = new ExpressionEvaluator();

    public DecisionTreeExpanderExpression() {
        ee.setExpressionType(boolean.class);
    }

    @Override
    public Iterable<Relationship> expand(Path path, BranchState<Object> branchState) {
        // If we get to an Answer or Transit, stop traversing, we found a valid path.
        if (path.endNode().hasLabel(Labels.Answer)) {
            return Collections.emptyList();
        }

        // If we have Rules to evaluate, go do that.
        if (path.endNode().hasRelationship(Direction.OUTGOING, RelationshipTypes.HAS)) {
            return path.endNode().getRelationships(Direction.OUTGOING, RelationshipTypes.HAS);
        }

        if (path.endNode().hasLabel(Labels.Rule)) {
            try {
                if (isTrue(path.endNode())) {
                    return path.endNode().getRelationships(Direction.OUTGOING, RelationshipTypes.IS_TRUE);
                } else {
                    return path.endNode().getRelationships(Direction.OUTGOING, RelationshipTypes.IS_FALSE);
                }
            } catch (Exception e) {
                // Could not continue this way!
                return Collections.emptyList();
            }
        }

        // Otherwise, not sure what to do really.
        return Collections.emptyList();
    }

    private boolean isTrue(Node rule) throws Exception {
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
            ee.setParameters(parameterNames, parameterTypes);

            // And now we "cook" (scan, parse, compile and load) the expression.
            ee.cook((String)ruleProperties.get("expression"));

            return (boolean) ee.evaluate(arguments);
    }

    @Override
    public PathExpander<Object> reverse() {
        return null;
    }
}
