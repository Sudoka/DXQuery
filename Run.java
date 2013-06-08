import XQuery.*;

import java.io.StringReader;
import java.lang.Integer;
import java.util.ArrayList;

import sun.util.logging.resources.logging;

class test {
	String str;
	ArrayList<ArrayList<String>> strMatrix;
}

public class Run implements XQueryParserTreeConstants {
	static String query = "(./ACT)";

	public static void main(String[] args)
	// --------------------------------------
	{

		String test = "<result>{"
				+ "for $a in document(\"j_caesar.xml\")//ACT,\n"
				+ "    $sc in $a//SCENE,\n"
				+ "    $sp in $sc/SPEECH\n"
				+ "where $sp/LINE/text() = \"Et tu, Brute! Then fall, Caesar.\"\n"
				+ "return <who>{$sp/SPEAKER/text()}</who>,\n"
				+ "       <when>{<act>{$a/TITLE/text()}</act>,\n"
				+ "             <scene>{$sc/TITLE/text()}</scene>}\n"
				+ "       </when>\n" + "}</result>\n";
		String testbib = "<result>{\n" + "for $a in doc(\"bib.xml\")//book,\n"
				+ "    $sc in $a//author,\n" + "    $sp in $sc/last,\n"
				+ "    $x in doc(\"bib.xml\")//reviews\n"
				+ "where $sp/text() = \"Lorant\"\n"
				+ "return <title>{$a//title/text()}</title>,\n"
				+ "        <YearPrice>{\n"
				+ "            <first>{$sc//first/text()}</first>,\n"
				+ "            <price>{$x//price/text()}</price>\n"
				+ "        }</YearPrice>\n" + "}</result>\n";

		String test2 = "for $s in document(\"j_caesar.xml\")//SPEAKER \n"
				+ "return <speaks>{<who>{$s/text()}</who>, \n"
				+ "                for $a in document(\"j_caesar.xml\")//ACT\n"
				+ "                where some $s1 in $a//SPEAKER satisfies $s1 eq $s\n"
				+ "                return <when>{$a/title/text()}</when>}\n"
				+ "</speaks>\n";

		String test3 = "for $A in document(\"bib.xml\")/AS\n"
				+ "where $A = \"test\"\n" + "return <a>{$A}</a>\n";

		String test4 = "<result>\n"
				+ "{\n"
				+ "for $a in document(\"j_caesar.xml\")//ACT,\n"
				+ "    $sc in $a//SCENE,\n"
				+ "    $sp in $sc/SPEECH\n"
				+ "where $sp/LINE/text() = \"Et tu, Brute! Then fall, Caesar.\"\n"
				+ "return <who>{$sp/SPEAKER/text()}</who>,\n"
				+ "       <when>{\n" + "	      <act>{$a/title/text()}</act>,\n"
				+ "              <scene>{$sc/title/text()}</scene>\n"
				+ "       	     } </when>\n" + "}\n" + "</result>\n";

		String test5 = "for $s in document(\"j_caesar.xml\")//SPEAKER\n"
				+ "return <speaks>{<who>{$s/text()}</who>,\n"
				+ "                for $a in document(\"j_caesar.xml\")//ACT\n"
				+ "                where some $s1 in $a//SPEAKER satisfies $s1 eq $s\n"
				+ "                return <when>{$a/title/text()}</when>}\n"
				+ "       </speaks>\n";
		String teststr = "\t\n";
		System.out.println(teststr.trim().length());

		String testAP = "for $b in doc(\"bib.xml\")/bib, $c in $b/book/year return <test>{$c}</test>";
		String testLet = "let $b := doc(\"bib.xml\")/bib, $c := $b/book for $d in $c/year return $d";

		String testCond1 = "for $b in doc(\"bib.xml\")/bib/book,\n $t in doc(\"bib.xml\")/bib/reviews,\n"
				+ "$tb in $b/title,\n $tt in $t//title\n"
				+ "where $tb/text() = $tt/text()\n"
				+ "return $b/price";
		String testCond2 = "for $b in doc(\"bib.xml\")/bib/book,\n $t in doc(\"bib.xml\")/bib/reviews,\n"
				+ "$tb in $b/title,\n $tt in $t//title\n"
				+ "where some $tx in $tb/text(), $ty in $tt/text() satisfies $tx=$ty\n"
				+ "return $tb/text()";
		String testCond3 = "for $b in doc(\"bib.xml\")/bib/book,\n $t in doc(\"bib.xml\")/bib/reviews,\n"
				+ "$tb in $b/title,\n $tt in $t//title\n"
				+ "where not($tb/text() = $tt/text())\n" + "return $tb";
		// DebugLogger.MasterRegularLog= false;
		new Run().runQuery(testCond1);
	}

	void runQuery(String queryStr)
	// ------------------------------
	{
		XQueryParser parser = new XQueryParser(new StringReader(queryStr));

		try {
			System.out.println("\nquery = " + queryStr + "\n");
			AST_Root root = parser.query();

			// dump the result tree to the console for debug purposes

			root.dump("");

			System.out.println();

			XQProcessVisitor visitor = new XQProcessVisitor();
			VariableKeeper res = (VariableKeeper) root.jjtAccept(visitor, null);
			System.out.println("=============final result=============");
			res.PrintAllVars();

			// here's where we actually walk the tree we've so painfully
			// constructed and derive an answer to our query

			int answer = eval((SimpleNode) root.jjtGetChild(0));

			System.out.println("\nanswer = " + answer);
		} catch (ParseException e) {
			System.out.print(e.getMessage());
			System.out.println();
		} catch (TokenMgrError tke) {
			System.out.print(tke.getMessage());
			System.out.println();
		}
	}

	int eval(SimpleNode node)
	// -------------------------
	{
		int result;

		// each node stores an id which uniquely identifies its type
		// jjtree names and stores these enums in an interface
		// named XQueryParserTreeConstants by JJTree
		int id = node.getId();
		String nodeName = node.toString();
		System.out.println(nodeName);

		return 0;
		// lhs = (SimpleNode) node.jjtGetChild(0);
		// rhs = (SimpleNode) node.jjtGetChild(1);

		// System.out.println( "eval(): evaluating AST nodetype "
		// + id + " [" + nodeName + "]" );

		/* switch( id ) */
		// {
		// case JJTPLUS : return eval( lhs ) + eval( rhs );
		// case JJTMINUS : return eval( lhs ) - eval( rhs );
		// case JJTMULT : return eval( lhs ) * eval( rhs );

		// case JJTINT : return Integer.parseInt( node.getText() );

		// default :

		// throw new java.lang.IllegalArgumentException(
		// "didn't understand the query, man!" );
		/* } */
	}
}
