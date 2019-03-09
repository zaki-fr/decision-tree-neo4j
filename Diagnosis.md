# STEP1: 

Initialize the decision tree schema
  
    CALL fr.zaki.schema.generate;

# STEP2: 

Put the dummy GHM data nodes into neo4j database

    CREATE (cmd08:Tree { id: 'CMD08', parameters: 'answer_cm08', types:'String' })
    CREATE (cmd08_rule:Rule { name: 'Acte op. de la CMD 08', parameters: 'answer_cm08', types:'String', script:'switch (answer_cm08) { case \"POSITIVE\": return \"PASSED\"; case \"NEGATIVE\": return \"FAILED\"; default: return \"UNKNOWN\"; }' })
    CREATE (dp007_rule:Rule { name: 'Infection ostéoarticulaire (D-077)', parameters: 'answer_dp007', types:'String', script:'switch (answer_dp007) { case \"POSITIVE\": return \"PASSED\"; case \"NEGATIVE\": return \"FAILED\"; default: return \"UNKNOWN\"; }' })
    CREATE (a368_rule:Rule { name: 'Itv. maj. pour infection ostéoarticulaire (A-368)', parameters: 'answer_a368', types:'String', script:'switch (answer_a368) { case \"POSITIVE\": return \"PASSED\"; case \"NEGATIVE\": return \"FAILED\"; default: return \"UNKNOWN\"; }' })
    CREATE (a289_rule:Rule { name: 'Traction continue ou réduction progressive : hanche ou fémur (A-289)', parameters: 'answer_a289', types:'String', script:'switch (answer_a289) { case \"POSITIVE\": return \"PASSED\"; case \"NEGATIVE\": return \"FAILED\"; default: return \"UNKNOWN\"; }' })
    CREATE (ghm_08C61:Node { id: 'GHM 08C61'})
    CREATE (ghm_08C62:Node { id: 'GHM 08C62'})
    CREATE (ghm_08K04:Node { id: 'GHM 08K04'})
    CREATE (answer_continue:Node { id: 'continue'})
    CREATE (answer_unknown:Node { id: 'unknown'})

    CREATE (dp007:Node { id: 'DP007', parameters: 'answer_dp007', types:'String' })
    CREATE (a368:Node { id: 'A368', parameters: 'answer_a368', types:'String' })
    CREATE (a289:Node { id: 'A289', parameters: 'answer_a289', types:'String'})

    CREATE (cmd08)-[:HAS]->(cmd08_rule)
    CREATE (dp007)-[:HAS]->(dp007_rule)
    CREATE (a368)-[:HAS]->(a368_rule)
    CREATE (a289)-[:HAS]->(a289_rule)

    CREATE (cmd08_rule)-[:PASSED]->(dp007)
    CREATE (cmd08_rule)-[:FAILED]->(a289)
    CREATE (cmd08_rule)-[:UNKNOWN]->(answer_unknown)

    CREATE (dp007_rule)-[:PASSED]->(a368)
    CREATE (dp007_rule)-[:FAILED]->(answer_inwait)
    CREATE (dp007_rule)-[:UNKNOWN]->(answer_unknown)

    CREATE (a368_rule)-[:PASSED]->(ghm_08C61)
    CREATE (a368_rule)-[:FAILED]->(ghm_08C62)
    CREATE (a368_rule)-[:UNKNOWN]->(answer_unknown)

    CREATE (a289_rule)-[:PASSED]->(ghm_08K04)
    CREATE (a289_rule)-[:FAILED]->(answer_continue)
    CREATE (a289_rule)-[:UNKNOWN]->(answer_unknown);

# STEP3: 

Doctor: make a decision test procedure CMD08 for a patient

    MATCH (cmd08:Tree { id: 'CMD08' })-[rels]-(nodes) return rels, cmd08, nodes

Return with the required parameters `answer_cm08` for the next applied rule:
    
    {
        "identity": {
          "low": 0,
          "high": 0
        },
        "labels": [
          "Tree"
        ],
        "properties": {
          "parameters": "answer_cm08",
          "types": "String",
          "id": "CMD08"
        }
    }


# STEP4: 

After patient inspected all the test procedure, doctor get the result as POSITIVE. He put the result of CMD08=POSTIVE in `answer_cm08` and get a suggession of the next procedure as `DP007` node.

    CALL fr.zaki.traverse.DecisionTreeScript('CMD08', {answer_cm08:'POSITIVE'}) yield path return path

# STEP5: 

Doctor get a DP007 code to examine the next procedure, after patient inspected all the test procedure of DP007, doctor get the result as POSITIVE. He put the result of DP007=POSTIVE in `answer_dp007` and get a suggession of the next procedure as `A368` node.

    CALL fr.zaki.traverse.DecisionTreeScript('CMD08', {answer_cm08:'POSITIVE', answer_dp007:'POSITIVE'}) yield path return path

# STEP5: 

Continue with A368, the doctor will reach the final diagnostic with the GHM08C61 code. He put the result of A368=POSTIVE in `answer_a368` and get a suggession of the final procedure as `GHM08C61` code.

    CALL fr.zaki.traverse.DecisionTreeScript('CMD08', {answer_cm08:'POSITIVE', answer_dp007:'POSITIVE', answer_a368:'POSITIVE'}) yield path return path

# Other posibility branches...

    CALL fr.zaki.traverse.DecisionTreeScript('CMD08', {answer_cm08:'POSITIVE', answer_dp007:'NEGATIVE'}) yield path return path
    CALL fr.zaki.traverse.DecisionTreeScript('CMD08', {answer_cm08:'NEGATIVE', answer_a289:'POSITIVE'}) yield path return path
    CALL fr.zaki.traverse.DecisionTreeScript('CMD08', {answer_cm08:'NEGATIVE', answer_a289:'NEGATIVE'}) yield path return path