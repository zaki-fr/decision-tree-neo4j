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

public class DecisionTreeTraverserExpressionTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Rule
    public final Neo4jRule neo4j = new Neo4jRule()
            .withFixture(MODEL_STATEMENT)
            .withProcedure(DecisionTreeTraverser.class);

    @Test
    public void testTraversal() throws Exception {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY1);
        int count = response.get("results").get(0).get("data").size();
        assertEquals(1, count);
        ArrayList<Map> path1 = mapper.convertValue(response.get("results").get(0).get("data").get(0).get("row").get(0), ArrayList.class);
        assertEquals("no", path1.get(path1.size() - 1).get("id"));
    }

    private static final Map QUERY1 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL fr.zaki.traverse.DecisionTreeExpression('bar entrance', {gender:'male', age:'20'}) yield path return path")));

    @Test
    public void testTraversalTwo() throws Exception {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY2);
        int count = response.get("results").get(0).get("data").size();
        assertEquals(1, count);
        ArrayList<Map> path1 = mapper.convertValue(response.get("results").get(0).get("data").get(0).get("row").get(0), ArrayList.class);
        assertEquals("yes", path1.get(path1.size() - 1).get("id"));
    }

    private static final Map QUERY2 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL fr.zaki.traverse.DecisionTreeExpression('bar entrance', {gender:'female', age:'19'}) yield path return path")));

    @Test
    public void testTraversalThree() throws Exception {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY3);
        int count = response.get("results").get(0).get("data").size();
        assertEquals(1, count);
        ArrayList<Map> path1 = mapper.convertValue(response.get("results").get(0).get("data").get(0).get("row").get(0), ArrayList.class);
        assertEquals("yes", path1.get(path1.size() - 1).get("id"));
    }

    private static final Map QUERY3 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL fr.zaki.traverse.DecisionTreeExpression('bar entrance', {gender:'male', age:'23'}) yield path return path")));


    private static final String MODEL_STATEMENT =
            "CREATE (root:Tree { id: 'bar entrance' })" +
                    "CREATE (over21_rule:Rule { parameters: 'age', types:'int', expression:'age >= 21' })" +
                    "CREATE (gender_rule:Rule { parameters: 'age,gender', types:'int,String', expression:'(age >= 18) && gender.equals(\"female\")' })" +
                    "CREATE (answer_yes:Node { id: 'yes' })" +
                    "CREATE (answer_no:Node { id: 'no' })" +
                    "CREATE (root)-[:HAS]->(over21_rule)" +
                    "CREATE (over21_rule)-[:IS_TRUE]->(answer_yes)" +
                    "CREATE (over21_rule)-[:IS_FALSE]->(gender_rule)" +
                    "CREATE (gender_rule)-[:IS_TRUE]->(answer_yes)" +
                    "CREATE (gender_rule)-[:IS_FALSE]->(answer_no)";
}
