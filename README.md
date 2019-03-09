# Decision Trees With Rules

[![Build Status](https://travis-ci.org/zaki-fr/decision-tree-core.svg?branch=master)](https://travis-ci.org/zaki-fr/decision-tree-core)

***This project is branched from https://github.com/maxdemarzi/decision_trees_with_rules***

Decision Tree traverser with rules by expression and script

Instructions
------------ 

This project uses maven, to build a jar-file with the procedure in this project, simply package the project with maven:

    mvn clean install

This will produce a jar-file, `target/decision-tree-core-1.0.1-SNAPSHOT.jar`,

Start neo4j server and its plugins using docker

```shellscript
$ docker ps
$ bash run-neo4j-with-plugin.sh
```

Open the neo4j web admin `http://localhost:7474`

Create the Schema by running this stored procedure:

    CALL fr.zaki.schema.generate
    
Create some test data:

    CREATE (tree:Tree { id: 'bar entrance' })
    CREATE (over21_rule:Rule { name: 'Over 21?', parameters: 'age', types:'int', expression:'age >= 21' })
    CREATE (gender_rule:Rule { name: 'Over 18 and female', parameters: 'age,gender', types:'int,String', expression:'(age >= 18) && gender.equals(\"female\")' })
    CREATE (answer_yes:Node { id: 'yes'})
    CREATE (answer_no:Node { id: 'no'})
    CREATE (tree)-[:HAS]->(over21_rule)
    CREATE (over21_rule)-[:IS_TRUE]->(answer_yes)
    CREATE (over21_rule)-[:IS_FALSE]->(gender_rule)
    CREATE (gender_rule)-[:IS_TRUE]->(answer_yes)
    CREATE (gender_rule)-[:IS_FALSE]->(answer_no)
    
Try it:

    CALL fr.zaki.traverse.DecisionTreeExpression('bar entrance', {gender:'male', age:'20'}) yield path return path;
    CALL fr.zaki.traverse.DecisionTreeExpression('bar entrance', {gender:'female', age:'19'}) yield path return path;
    CALL fr.zaki.traverse.DecisionTreeExpression('bar entrance', {gender:'male', age:'23'}) yield path return path;     
    
    
***Evaluating Scripts instead of expressions.***

Create some test data with stopped condition node: A stoppable Node is a Node with `parameters` field indicates to passthrough it if its `values` are presented.

If the `answer_4` is not set, the path will be stopped at the `answer_stop:Node`, if set, the next `another_rule` rule will be executed. It's necessary to declare `answer_stop:Node`-[:HAS]-`another_rule`.

    CREATE (tree:Tree { id: 'funeral' })
    CREATE (good_man_rule:Rule { name: 'Was Lil Jon a good man?', parameters: 'answer_1', types:'String', script:'switch (answer_1) { case \"yeah\": return \"OPTION_1\"; case \"what\": return \"OPTION_2\"; case \"okay\": return \"OPTION_3\"; default: return \"UNKNOWN\"; }' })
    CREATE (good_man_two_rule:Rule { name: 'I said, was he a good man?', parameters: 'answer_2', types:'String', script:'switch (answer_2) { case \"yeah\": return \"OPTION_1\"; case \"what\": return \"OPTION_2\"; case \"okay\": return \"OPTION_3\"; default: return \"UNKNOWN\"; }' })
    CREATE (rest_in_peace_rule:Rule { name: 'May he rest in peace', parameters: 'answer_3', types:'String', script:'switch (answer_3) { case \"yeah\": return \"OPTION_1\"; case \"what\": return \"OPTION_2\"; case \"okay\": return \"OPTION_3\"; default: return \"UNKNOWN\"; } ' })
    CREATE (another_rule:Rule { name: 'Yet another rule', parameters: 'answer_4', types:'String', script:'switch (answer_4) { case \"yeah\": return \"OPTION_1\"; case \"what\": return \"OPTION_2\"; case \"okay\": return \"OPTION_3\"; default: return \"UNKNOWN\"; } ' })
    
    CREATE (answer_correct:Node { id: 'correct', parameters: 'answer_2', types:'String'})
    CREATE (answer_incorrect:Node { id: 'incorrect' })
    CREATE (answer_stop:Node { id: 'stop', parameters: 'answer_4', types:'String' })
    CREATE (answer_unknown:Node { id: 'unknown'})
    
    CREATE (tree)-[:HAS]->(good_man_rule)
    CREATE (answer_stop)-[:HAS]->(another_rule)

    CREATE (good_man_rule)-[:OPTION_1]->(answer_stop)
    CREATE (good_man_rule)-[:OPTION_2]->(good_man_two_rule)
    CREATE (good_man_rule)-[:OPTION_3]->(answer_incorrect)
    CREATE (good_man_rule)-[:UNKNOWN]->(answer_unknown)

    CREATE (good_man_two_rule)-[:OPTION_1]->(rest_in_peace_rule)
    CREATE (good_man_two_rule)-[:OPTION_2]->(answer_incorrect)
    CREATE (good_man_two_rule)-[:OPTION_3]->(answer_incorrect)
    CREATE (good_man_two_rule)-[:UNKNOWN]->(answer_unknown)

    CREATE (rest_in_peace_rule)-[:OPTION_1]->(answer_incorrect)
    CREATE (rest_in_peace_rule)-[:OPTION_2]->(answer_incorrect)
    CREATE (rest_in_peace_rule)-[:OPTION_3]->(answer_correct)
    CREATE (rest_in_peace_rule)-[:UNKNOWN]->(answer_unknown)

    CREATE (another_rule)-[:OPTION_1]->(answer_incorrect)
    CREATE (another_rule)-[:OPTION_2]->(answer_incorrect)
    CREATE (another_rule)-[:OPTION_3]->(answer_correct)
    CREATE (another_rule)-[:UNKNOWN]->(answer_unknown);  

Test Traversal With Stopped Condition: it will end at the node with a `stop` node:
    
    CALL fr.zaki.traverse.DecisionTreeScript('funeral', {answer_1:'yeah', answer_2:'yeah'}) yield path return path;

Test Traversal With Continue Rule After Stopped resulting an `unknown` node:

    CALL fr.zaki.traverse.DecisionTreeScript('funeral', {answer_1:'what', answer_2:'', answer_3:''}) yield path return path;

Test Traversal With Continue Rule After Stopped with a `correct` node:

    CALL fr.zaki.traverse.DecisionTreeScript('funeral', {answer_1:'yeah', answer_2:'yeah', answer_3:'okay', answer_4:'okay'}) yield path return path;

Test Traversal With Continue Rule After Stopped resulting an `incorrect` node:

    CALL fr.zaki.traverse.DecisionTreeScript('funeral', {answer_1:'yeah', answer_2:'yeah', answer_3:'okay', answer_4:'yeah'}) yield path return path;