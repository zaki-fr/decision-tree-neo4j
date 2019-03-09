package fr.zaki;

import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.logging.Log;

import fr.zaki.schema.RelationshipTypes;

public class DecisionTreeBase {
	protected Map<String, String> facts;
	protected Log log;

	public DecisionTreeBase() {
		super();
	}

	public void setParameters(Map<String, String> facts, Log log) {
		this.facts = facts;
		this.log = log;
	}

	protected boolean shouldEnd(Node node) throws Exception {
	    int argumentCount = 0;
	    boolean shouldEndTraversing = true;
	    Map<String, Object> nodeProperties = node.getAllProperties();
	    String[] parameterNames = Magic.explode((String) nodeProperties.get("parameters"));
	    Class<?>[] parameterTypes = Magic.stringToTypes((String) nodeProperties.get("types"));
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
	            shouldEndTraversing = false;
	        }
	    }
	
	    return shouldEndTraversing;
	}

}