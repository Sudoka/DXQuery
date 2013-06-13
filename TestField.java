import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Node;

import XQuery.AST_Root;
import XQuery.ParseException;
import XQuery.VariableKeeper;
import XQuery.XQProcessVisitor;
import XQuery.XQueryParser;

public class TestField {

	/**
	 * @param args
	 * @throws ParseException
	 */
	public static void main(String[] args) throws ParseException {

		Set<String> test1 = new HashSet<String>();
		String a = "a";
		test1.add(a);
		String b = new String("b");
		test1.add(b);
		test1.add("c");
		Set<String> test2 = new HashSet<String>();
		String b2 = new String("b");
		test2.add(b2);
		test2.add("d");
		test2.addAll(test1);
		System.out.println(test2.size());

	}

	public static void test1() throws ParseException {
		String testbib = "<result>{\n" + "for $a in doc(\"bib.xml\")//book,\n"
				+ "    $sc in $a//author,\n" + "    $sp in $sc/last\n"
				+ "where $sp/text() = \"Lorant\"\n"
				+ "return <title>{$a//title/text()}</title>,\n"
				+ "        <YearPrice>{\n"
				+ "            <year>{$sc//first/text()}</year>,\n"
				+ "            <price>{$sp/text()}</price>\n"
				+ "        }</YearPrice>\n" + "}</result>\n";
		XQueryParser parser = new XQueryParser(new StringReader(testbib));

		System.out.println("\nquery = " + testbib + "\n");
		AST_Root root = parser.query();

		// dump the result tree to the console for debug purposes

		root.dump("");

		System.out.println();

		XQProcessVisitor visitor = new XQProcessVisitor();
		VariableKeeper res = (VariableKeeper) root.jjtAccept(visitor, null);
		// res.PrintAllVars();
		Node testNode = (Node) res.GetNodes().toArray()[0];
		HashMap<Node, String> testMap1 = new HashMap<Node, String>();
		testMap1.put((Node) testNode, "root");
		Node testCopy = testNode;
		HashMap<Node, String> testMap2 = new HashMap<Node, String>();
		testMap2.put(testCopy, "TestCopy");
		for (Node node : testMap1.keySet()) {
			testMap2.put(node, "root2");
			testMap1.remove(node);
		}
		testMap1.put(testCopy, "testCopy 1");

	}

}
