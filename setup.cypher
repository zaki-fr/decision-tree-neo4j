# STEP1: initialize the decision tree schema
CALL com.maxdemarzi.schema.generate;

# STEP2: put the dump data nodes

CREATE (tree:Tree { id: 'CMD08' })
CREATE (cmd08_rule:Rule { name: 'Acte op. de la CMD 08', parameter_names: 'answer_cm08', parameter_types:'String', script:'switch (answer_cm08) { case \"POSITIVE\": return \"PASSED\"; case \"NEGATIVE\": return \"FAILED\"; case \"PENDING\": return \"IN_WAIT\"; default: return \"UNKNOWN\"; }' })
CREATE (dp007_rule:Rule { name: 'Infection ostéoarticulaire (D-077)', parameter_names: 'answer_dp007', parameter_types:'String', script:'switch (answer_dp007) { case \"POSITIVE\": return \"PASSED\"; case \"NEGATIVE\": return \"FAILED\"; case \"PENDING\": return \"IN_WAIT\"; default: return \"UNKNOWN\"; }' })
CREATE (a368_rule:Rule { name: 'Itv. maj. pour infection ostéoarticulaire (A-368)', parameter_names: 'answer_a368', parameter_types:'String', script:'switch (answer_a368) { case \"POSITIVE\": return \"PASSED\"; case \"NEGATIVE\": return \"FAILED\"; case \"PENDING\": return \"IN_WAIT\"; default: return \"UNKNOWN\"; } ' })
CREATE (a289_rule:Rule { name: 'Traction continue ou réduction progressive : hanche ou fémur (A-289)', parameter_names: 'answer_a289', parameter_types:'String', script:'switch (answer_a289) { case \"POSITIVE\": return \"PASSED\"; case \"NEGATIVE\": return \"FAILED\"; case \"PENDING\": return \"IN_WAIT\"; default: return \"UNKNOWN\"; } ' })
CREATE (ghm_08C61:Answer { id: 'GHM 08C61'})
CREATE (ghm_08C62:Answer { id: 'GHM 08C62'})
CREATE (ghm_08K04:Answer { id: 'GHM 08K04'})
CREATE (dp_007:Answer { id: 'DP007'})

CREATE (answer_continue:Answer { id: 'continue'})
CREATE (answer_inwait:Answer { id: 'inwait'})
CREATE (answer_unknown:Answer { id: 'unknown'})
CREATE (tree)-[:HAS]->(cmd08_rule)
CREATE (dp_007)-[:HAS]->(dp007_rule)
CREATE (cmd08_rule)-[:PASSED]->(dp_007)
CREATE (cmd08_rule)-[:FAILED]->(a289_rule)
CREATE (cmd08_rule)-[:IN_WAIT]->(answer_inwait)
CREATE (cmd08_rule)-[:UNKNOWN]->(answer_unknown)

CREATE (dp007_rule)-[:PASSED]->(a368_rule)
CREATE (dp007_rule)-[:FAILED]->(answer_inwait)
CREATE (dp007_rule)-[:IN_WAIT]->(answer_inwait)
CREATE (dp007_rule)-[:UNKNOWN]->(answer_unknown)

CREATE (a368_rule)-[:PASSED]->(ghm_08C61)
CREATE (a368_rule)-[:FAILED]->(ghm_08C62)
CREATE (a368_rule)-[:IN_WAIT]->(answer_inwait)
CREATE (a368_rule)-[:UNKNOWN]->(answer_unknown)

CREATE (a289_rule)-[:PASSED]->(ghm_08K04)
CREATE (a289_rule)-[:FAILED]->(answer_continue)
CREATE (a289_rule)-[:IN_WAIT]->(answer_inwait)
CREATE (a289_rule)-[:UNKNOWN]->(answer_unknown);

# STEP3: docter: make a decision test procedure CMD08 for a patient
MATCH (tree:Tree { id: 'CMD08' })-[rels]-(nodes) return rels, tree, nodes

# STEP4: after patient inspected all the test procedure, doctor get the result as POSITIVE,
# He put the result of CMD08=POSTIVE and get a suggession of the next procedure as DP007.
CALL com.maxdemarzi.traverse.decision_tree_two('CMD08', {answer_cm08:'POSITIVE'}) yield path return path

# OTHERS: under test,...
MATCH (a:Answer { id: 'DP007' })-[rels]-(nodes) return rels, a, nodes
CALL com.maxdemarzi.traverse.decision_tree_two('CMD08', {answer_cm08:'POSITIVE', answer_dp007:'NEGATIVE'}) yield path return path
CALL com.maxdemarzi.traverse.decision_tree_two('CMD08', {answer_cm08:'POSITIVE', answer_dp007:'POSITIVE', answer_a368:'POSITIVE'}) yield path return path
CALL com.maxdemarzi.traverse.decision_tree_two('CMD08', {answer_cm08:'NEGATIVE', answer_a289:'POSITIVE'}) yield path return path
CALL com.maxdemarzi.traverse.decision_tree_two('CMD08', {answer_cm08:'NEGATIVE', answer_a289:'NEGATIVE'}) yield path return path
CALL com.maxdemarzi.traverse.decision_tree_two('CMD08', {answer_cm08:'NEGATIVE', answer_a289:'PENDING'}) yield path return path