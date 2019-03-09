package fr.zaki;

import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.PathEvaluator;

import fr.zaki.schema.Labels;

public class DecisionTreeEvaluator extends DecisionTreeBase implements PathEvaluator<Object> {
	@Override
	public Evaluation evaluate(Path path, BranchState<Object> branchState) {
		// If we get to an Answer or Transit, stop traversing, we found a valid path.
		if (path.endNode().hasLabel(Labels.Answer)) {
			try {
				if (shouldEnd(path.endNode())) {
					return Evaluation.INCLUDE_AND_PRUNE;
				}
			} catch (Exception e) {
				System.out.println("[DecisionTreeEvaluator] " + e.toString());
			}
			return Evaluation.EXCLUDE_AND_CONTINUE;
		} else {
			// If not, continue down this path if there is anything else to find.
			return Evaluation.EXCLUDE_AND_CONTINUE;
		}
	}

	@Override
	public Evaluation evaluate(Path path) {
		return null;
	}
}
