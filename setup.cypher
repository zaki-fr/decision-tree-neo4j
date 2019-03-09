# STEP1: initialize the decision tree schema
CALL fr.zaki.schema.generate;

# STEP2: put the dump data nodes

CREATE (cmd08:Tree { id: 'CMD08' })
CREATE (cmd08_rule:Rule { name: 'Acte op. de la CMD 08', parameters: 'answer_cm08', types:'String', script:'switch (answer_cm08) { case \"POSITIVE\": return \"PASSED\"; case \"NEGATIVE\": return \"FAILED\"; case \"PENDING\": return \"IN_WAIT\"; default: return \"UNKNOWN\"; }' })
CREATE (dp007_rule:Rule { name: 'Infection ostéoarticulaire (D-077)', parameters: 'answer_dp007', types:'String', script:'switch (answer_dp007) { case \"POSITIVE\": return \"PASSED\"; case \"NEGATIVE\": return \"FAILED\"; case \"PENDING\": return \"IN_WAIT\"; default: return \"UNKNOWN\"; }' })
CREATE (a368_rule:Rule { name: 'Itv. maj. pour infection ostéoarticulaire (A-368)', parameters: 'answer_a368', types:'String', script:'switch (answer_a368) { case \"POSITIVE\": return \"PASSED\"; case \"NEGATIVE\": return \"FAILED\"; case \"PENDING\": return \"IN_WAIT\"; default: return \"UNKNOWN\"; } ' })
CREATE (a289_rule:Rule { name: 'Traction continue ou réduction progressive : hanche ou fémur (A-289)', parameters: 'answer_a289', types:'String', script:'switch (answer_a289) { case \"POSITIVE\": return \"PASSED\"; case \"NEGATIVE\": return \"FAILED\"; case \"PENDING\": return \"IN_WAIT\"; default: return \"UNKNOWN\"; } ' })
CREATE (ghm_08C61:Node { id: 'GHM 08C61'})
CREATE (ghm_08C62:Node { id: 'GHM 08C62'})
CREATE (ghm_08K04:Node { id: 'GHM 08K04'})
CREATE (answer_continue:Node { id: 'continue'})
CREATE (answer_inwait:Node { id: 'inwait'})
CREATE (answer_unknown:Node { id: 'unknown'})

CREATE (dp007:Transit { id: 'DP007'})
CREATE (a289:Transit { id: 'A289'})


CREATE (cmd08)-[:HAS]->(cmd08_rule)
CREATE (dp007)-[:HAS]->(dp007_rule)

CREATE (cmd08_rule)-[:PASSED]->(dp007)
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
MATCH (cmd08:Tree { id: 'CMD08' })-[rels]-(nodes) return rels, cmd08, nodes

# STEP4: after patient inspected all the test procedure, doctor get the result as POSITIVE,
# He put the result of CMD08=POSTIVE and get a suggession of the next procedure as DP007.
CALL fr.zaki.traverse.DecisionTreeScript('CMD08', {answer_cm08:'POSITIVE'}) yield path return path

# OTHERS: under test,...
MATCH (a:Node { id: 'DP007' })-[rels]-(nodes) return rels, a, nodes
CALL fr.zaki.traverse.DecisionTreeScript('CMD08', {answer_cm08:'POSITIVE', answer_dp007:'NEGATIVE'}) yield path return path
CALL fr.zaki.traverse.DecisionTreeScript('CMD08', {answer_cm08:'POSITIVE', answer_dp007:'POSITIVE', answer_a368:'POSITIVE'}) yield path return path
CALL fr.zaki.traverse.DecisionTreeScript('CMD08', {answer_cm08:'NEGATIVE', answer_a289:'POSITIVE'}) yield path return path
CALL fr.zaki.traverse.DecisionTreeScript('CMD08', {answer_cm08:'NEGATIVE', answer_a289:'NEGATIVE'}) yield path return path
CALL fr.zaki.traverse.DecisionTreeScript('CMD08', {answer_cm08:'NEGATIVE', answer_a289:'PENDING'}) yield path return path