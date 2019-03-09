package fr.zaki;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static junit.framework.TestCase.assertEquals;

public class DecisionTreeTraverserScriptTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Rule
    public final Neo4jRule neo4j = new Neo4jRule()
            .withFixture(MODEL_STATEMENT)
            .withProcedure(DecisionTreeTraverser.class);

    @Test
    public void testTraversalWithStoppedCondition() throws Exception {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY1);
        int count = response.get("results").get(0).get("data").size();
        assertEquals(1, count);
        ArrayList<Map> path1 = mapper.convertValue(response.get("results").get(0).get("data").get(0).get("row").get(0), ArrayList.class);
        assertEquals("stop", path1.get(path1.size() - 1).get("id"));
    }

    private static final Map QUERY1 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL fr.zaki.traverse.DecisionTreeScript('funeral', {answer_1:'yeah', answer_2:'yeah'}) yield path return path")));

    @Test
    public void testTraversalWithContinueRuleAfterStoppedUnknown() throws Exception {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY2);
        int count = response.get("results").get(0).get("data").size();
        assertEquals(1, count);
        ArrayList<Map> path1 = mapper.convertValue(response.get("results").get(0).get("data").get(0).get("row").get(0), ArrayList.class);
        assertEquals("unknown", path1.get(path1.size() - 1).get("id"));
    }

    private static final Map QUERY2 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL fr.zaki.traverse.DecisionTreeScript('funeral', {answer_1:'what', answer_2:'', answer_3:''}) yield path return path")));

    @Test
    public void testTraversalWithContinueRuleAfterStoppedCorrect() throws Exception {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY3);
        int count = response.get("results").get(0).get("data").size();
        assertEquals(1, count);
        ArrayList<Map> path1 = mapper.convertValue(response.get("results").get(0).get("data").get(0).get("row").get(0), ArrayList.class);
        assertEquals("correct", path1.get(path1.size() - 1).get("id"));
    }

    private static final Map QUERY3 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL fr.zaki.traverse.DecisionTreeScript('funeral', {answer_1:'yeah', answer_2:'yeah', answer_3:'okay', answer_4:'okay'}) yield path return path")));

    @Test
    public void testTraversalWithContinueRuleAfterStoppedIncorrect() throws Exception {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY4);
        int count = response.get("results").get(0).get("data").size();
        assertEquals(1, count);
        ArrayList<Map> path1 = mapper.convertValue(response.get("results").get(0).get("data").get(0).get("row").get(0), ArrayList.class);
        assertEquals("incorrect", path1.get(path1.size() - 1).get("id"));
    }

    private static final Map QUERY4 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL fr.zaki.traverse.DecisionTreeScript('funeral', {answer_1:'yeah', answer_2:'yeah', answer_3:'okay', answer_4:'yeah'}) yield path return path")));

    private static final String MODEL_STATEMENT =
            "CREATE (tree:Tree { id: 'funeral' })" +
                    "CREATE (good_man_rule:Rule { name: 'Was Lil Jon a good man?', parameters: 'answer_1', types:'String', script:'switch (answer_1) { case \"yeah\": return \"OPTION_1\"; case \"what\": return \"OPTION_2\"; case \"okay\": return \"OPTION_3\"; default: return \"UNKNOWN\"; }' })" +
                    "CREATE (good_man_two_rule:Rule { name: 'I said, was he a good man?', parameters: 'answer_2', types:'String', script:'switch (answer_2) { case \"yeah\": return \"OPTION_1\"; case \"what\": return \"OPTION_2\"; case \"okay\": return \"OPTION_3\"; default: return \"UNKNOWN\"; }' })" +
                    "CREATE (rest_in_peace_rule:Rule { name: 'May he rest in peace', parameters: 'answer_3', types:'String', script:'switch (answer_3) { case \"yeah\": return \"OPTION_1\"; case \"what\": return \"OPTION_2\"; case \"okay\": return \"OPTION_3\"; default: return \"UNKNOWN\"; } ' })" +
                    "CREATE (another_rule:Rule { name: 'Yet another rule', parameters: 'answer_4', types:'String', script:'switch (answer_4) { case \"yeah\": return \"OPTION_1\"; case \"what\": return \"OPTION_2\"; case \"okay\": return \"OPTION_3\"; default: return \"UNKNOWN\"; } ' })" +
                    
                    "CREATE (answer_correct:Node { id: 'correct', parameters: 'answer_2', types:'String'})" +
                    "CREATE (answer_incorrect:Node { id: 'incorrect' })" +
                    "CREATE (answer_stop:Node { id: 'stop', parameters: 'answer_4', types:'String' })" +
                    "CREATE (answer_unknown:Node { id: 'unknown'})" +
                    
                    "CREATE (tree)-[:HAS]->(good_man_rule)" +
                    "CREATE (answer_stop)-[:HAS]->(another_rule)" +

                    "CREATE (good_man_rule)-[:OPTION_1]->(answer_stop)" +
                    "CREATE (good_man_rule)-[:OPTION_2]->(good_man_two_rule)" +
                    "CREATE (good_man_rule)-[:OPTION_3]->(answer_incorrect)" +
                    "CREATE (good_man_rule)-[:UNKNOWN]->(answer_unknown)" +

                    "CREATE (good_man_two_rule)-[:OPTION_1]->(rest_in_peace_rule)" +
                    "CREATE (good_man_two_rule)-[:OPTION_2]->(answer_incorrect)" +
                    "CREATE (good_man_two_rule)-[:OPTION_3]->(answer_incorrect)" +
                    "CREATE (good_man_two_rule)-[:UNKNOWN]->(answer_unknown)" +

                    "CREATE (rest_in_peace_rule)-[:OPTION_1]->(answer_incorrect)" +
                    "CREATE (rest_in_peace_rule)-[:OPTION_2]->(answer_incorrect)" +
                    "CREATE (rest_in_peace_rule)-[:OPTION_3]->(answer_correct)" +
                    "CREATE (rest_in_peace_rule)-[:UNKNOWN]->(answer_unknown)" +

                    "CREATE (another_rule)-[:OPTION_1]->(answer_incorrect)" +
                    "CREATE (another_rule)-[:OPTION_2]->(answer_incorrect)" +
                    "CREATE (another_rule)-[:OPTION_3]->(answer_correct)" +
                    "CREATE (another_rule)-[:UNKNOWN]->(answer_unknown)";
}
